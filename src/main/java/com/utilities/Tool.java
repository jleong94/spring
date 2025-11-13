package com.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
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
	
	/** 
	 * @param log   Logger instance used for error reporting
	 * @param classpath   String representing the class path to scan
	 * 
	 * @return List<Path> containing paths to all regular files in the directory.
	 *         Returns an empty list if the resource size is 0.
	 * 
	 * @throws Throwable if any error occurs during file system operations. The error
	 *         is logged with detailed stack trace information (including class name,
	 *         line number, and error message) before being re-thrown.
	 */
	public List<Path> loadFileListFromClasspath(Logger log, String classpath) throws Throwable {
		try {
			ClassLoader classLoader = getClass().getClassLoader();
	        URL resourceUrl = classLoader.getResource(classpath);
	        
	        if (resourceUrl == null) {
	            log.error("Resource path not found: {}", classpath);
	            return Collections.emptyList();
	        }
	        
	        Path directoryPath = Paths.get(resourceUrl.toURI());
	        
	        return Files.walk(directoryPath, 1)
	                .filter(Files::isRegularFile)
	                .collect(Collectors.toList());
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
			throw e;
		}
	}
	
	/**
	 * Reads the entire content of a file using BufferedReader with proper resource management.
	 * 
	 * This method reads a file line by line and concatenates all lines into a single string.
	 * Note: Line breaks are NOT preserved in the returned string. If line breaks are needed,
	 * consider appending System.lineSeparator() or "\n" after each line.
	 * 
	 * @param log the Logger instance for error logging
	 * @param filePath the absolute or relative path to the file to be read
	 * @return the complete file content as a single string (without line breaks)
	 * @throws Throwable if any I/O error occurs during file reading, including:
	 *                   - FileNotFoundException if the file doesn't exist
	 *                   - AccessDeniedException if the file is not readable
	 *                   - IOException for other I/O errors
	 * 
	 * @implNote Uses Lombok's @Cleanup annotation to ensure BufferedReader is properly closed
	 * @implNote For large files, consider using streaming approach to avoid memory issues
	 * @implNote Character encoding is fixed to UTF-8
	 */
	public String readFileWithBufferedReader(Logger log, String filePath) throws Throwable {
		try {
			// Convert file path string to Path object for NIO operations
			Path path = Paths.get(filePath);
			
			// Create BufferedReader with UTF-8 encoding
			// @Cleanup ensures reader.close() is called automatically when exiting the try block
			@Cleanup BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
			
			// Initialize variables for line-by-line reading
			String line;
			StringBuilder lines = new StringBuilder(); // Thread-safe but synchronized; consider StringBuilder for single-threaded
			
			// Read file line by line until EOF (null)
			while ((line = reader.readLine()) != null) {
				// Append each line to buffer (line breaks are stripped by readLine())
				lines.append(line);
			}
			
			// Return concatenated content
			return lines.toString();
		} catch (Throwable e) {
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
			throw e;
		}
	}
}
