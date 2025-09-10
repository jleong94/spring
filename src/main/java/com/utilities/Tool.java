package com.utilities;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.Cleanup;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

@Component
public class Tool {

	public List<String> downloadFileFromSftp(Logger log, String host, String username, String password, String remote_path, String local_path, String knownHostsFilePath, String fingerprint) throws Throwable {
		List<String> downloadedFiles = new ArrayList<>();
		try {
			long todayEpoch = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toEpochSecond();

			@Cleanup SSHClient sshClient = new SSHClient();//@Cleanup - automatically clean up resources when a method exits
			if(knownHostsFilePath != null && !knownHostsFilePath.isBlank()) {
				File knownHostsFile = new File(knownHostsFilePath);
				if (knownHostsFile.isAbsolute() && knownHostsFile.exists() && knownHostsFile.isFile()) {
					sshClient.loadKnownHosts(knownHostsFile);
					log.info("Using known hosts file as verifier.");
				}
			} else if(fingerprint != null && !fingerprint.isBlank()) {
				sshClient.addHostKeyVerifier(new HostKeyVerifier() {
	                @Override
	                public boolean verify(String hostname, int port, PublicKey key) {
	                	String actual = SecurityUtils.getFingerprint(key); // e.g. "aa:bb:cc:...".  
	                    // Normalize both sides and compare
	                    String actualNorm = normalizeHexFingerprint(actual);
	                    return actualNorm.equalsIgnoreCase(normalizeHexFingerprint(fingerprint));
	                }

					@Override
					public List<String> findExistingAlgorithms(String hostname, int port) {
						return Collections.emptyList();
					}
	            });
			} else {log.info("Not using any verifier.");}
			String[] host_split = host.split(":");
			if(host_split.length > 1) {
				sshClient.connect(host_split[0], Integer.parseInt(host_split[1]));
			} else {sshClient.connect(host);}
			sshClient.authPassword(username, password);
			@Cleanup SFTPClient sftpClient = sshClient.newSFTPClient();
			remote_path = Paths.get(remote_path).normalize().toString().replace("\\", "/");
			local_path = Paths.get(local_path).normalize().toString().replace("\\", "/");
			List<RemoteResourceInfo> remote_files = sftpClient.ls(remote_path);
	        for (RemoteResourceInfo remote_resource : remote_files) {
	            if (!remote_resource.isDirectory()) {
	                String remoteFilePath = remote_path.concat("/").concat(remote_resource.getName());
	                String safeFileName = Paths.get(remote_resource.getName()).getFileName().toString();
	                File localFile = new File(local_path, safeFileName);
	                if (remote_resource.getAttributes().getMtime() >= todayEpoch) {
	                    log.info("Downloading file: {} (size={} bytes)", remoteFilePath, remote_resource.getAttributes().getSize());
	                    sftpClient.get(remoteFilePath, localFile.getAbsolutePath());
	                    downloadedFiles.add(localFile.getAbsolutePath());
	                } else {
	                    log.debug("Skipped old file: {}", remoteFilePath);
	                }
	            }
	        }
		} catch(Throwable e) {
			// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
		}
		return downloadedFiles;
	}
	
	/** Normalize colon-separated hex fingerprint to lower-case, no spaces (e.g. "aa:bb" -> "aa:bb") */
	private static String normalizeHexFingerprint(String f) {
		if (f == null || f.isBlank()) return "";
		return f.trim()
				.replaceAll("(?i)^sha256:", "")   // remove SHA256: if user mistakenly provided that
				.replaceAll("[^0-9a-fA-F:]", "")  // drop whitespace, padding, etc.
				.toLowerCase();
	}

	public String getTodayDateTimeInString() {
		String result = "";
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.appendPattern("yyyy-MM-dd")  // Date part
				.optionalStart()
				.appendLiteral('T')           // Try parsing with 'T'
				.optionalEnd()
				.optionalStart()
				.appendLiteral(' ')           // Try parsing with space
				.optionalEnd()
				.optionalStart()
				.appendPattern("HH:mm:ss")    // Time part
				.optionalEnd()
				.optionalStart()
				.appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, true)  // Handle 1-3 digit milliseconds
				.optionalEnd()
				.toFormatter();
		LocalDateTime now = LocalDateTime.now();
		result = formatter.format(now);
		result = result.contains("T") ? result.replace("T", "") : result;
		return result;
	}

	public List<String> saveUploadFileToPath(Logger log, List<MultipartFile> multipartFiles, String upload_path) throws Throwable {
		List<String> savedFiles = new ArrayList<>();
		try {
			if(multipartFiles != null && !multipartFiles.isEmpty() && upload_path != null && !upload_path.isBlank()) {
				Path uploadDir = Paths.get(upload_path);
				Files.createDirectories(uploadDir);
				for(MultipartFile multipartFile : multipartFiles) {
					if(multipartFile != null && !multipartFile.isEmpty()) {
						String originalFilename = Path.of(multipartFile.getOriginalFilename()).getFileName().toString(); // sanitize path
						Path destinationFile = uploadDir.resolve(originalFilename).normalize().toAbsolutePath();
						// Prevent path traversal attack
						if (destinationFile.getParent().equals(uploadDir.toAbsolutePath())) {
							Files.copy(multipartFile.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
							savedFiles.add(destinationFile.toString());
						} else {
							log.info("Cannot store file outside, {} the target directory, {}.", destinationFile.getParent(), uploadDir.toAbsolutePath());
						}
					}
				}
			} else {log.info("No files to upload or upload path is invalid.");}
		} catch(Throwable e) {
			// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
		}
		return savedFiles;
	}

	public String maskJson(Logger log, Set<String> jsonKey, String inputJson) {
		String result = "";
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode root = objectMapper.readTree(inputJson);
			maskNode(jsonKey, root);
			result = objectMapper.writeValueAsString(root);
		} catch(Throwable e) {
			// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
		}
		return result;
	}

	private void maskNode(Set<String> jsonKey, JsonNode node) {
		if (node.isObject()) {
			ObjectNode object = (ObjectNode) node;
			object.fieldNames().forEachRemaining(field -> {
				JsonNode child = object.get(field);
				if (jsonKey.contains(field)) {
					if (child.isTextual()) {
						object.put(field, maskValue(child.asText()));
					} else if (child.isArray()) {
						ArrayNode array = (ArrayNode) child;
						for (int i = 0; i < array.size(); i++) {
							JsonNode item = array.get(i);
							if (item.isTextual()) {
								array.set(i, TextNode.valueOf(maskValue(item.asText())));
							} else {
								maskNode(jsonKey, item); // array of objects
							}
						}
					} else {
						maskNode(jsonKey, child);
					}
				} else {
					maskNode(jsonKey, child);  // recurse
				}
			});
		} else if (node.isArray()) {
			for (JsonNode item : node) {
				maskNode(jsonKey, item);
			}
		}
	}

	public String maskValue(String plainValue) {
		if (plainValue == null || plainValue.isBlank()) return "";
		int length = plainValue.length();
		if (length <= 10) return "*".repeat(length); // not enough room to show 6 + 4
		String front = plainValue.substring(0, 6);
		String end = plainValue.substring(length - 4);
		String masked = "*".repeat(length - 10);
		return front.concat(masked).concat(end);
	}
}
