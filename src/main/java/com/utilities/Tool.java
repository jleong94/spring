package com.utilities;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.Cleanup;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

@Component
public class Tool {

	public boolean syncFileFromSftp(Logger log, String host, String username, String password, String remote_path, String local_path) throws Exception {
		@Cleanup SSHClient sshClient = new SSHClient();//@Cleanup - automatically clean up resources when a method exits
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date today = calendar.getTime();
		long todayInLong = today.getTime() / 1000;

		remote_path = Paths.get(remote_path).normalize().toString().replace("\\", "/");
		local_path = Paths.get(local_path).normalize().toString().replace("\\", "/");
		sshClient.addHostKeyVerifier(new HostKeyVerifier() {
			@Override
			public boolean verify(String hostname, int port, PublicKey key) {
				return true;
			}

			@Override
			public List<String> findExistingAlgorithms(String arg0, int arg1) {
				return Collections.emptyList();
			}
		});
		String[] host_split = host.split(":");
		if(host_split.length > 1) {
			sshClient.connect(host_split[0], Integer.parseInt(host_split[1]));
		} else {sshClient.connect(host);}
		sshClient.authPassword(username, password);
		SFTPClient sftpClient = sshClient.newSFTPClient();
		List<RemoteResourceInfo> remoteFiles = sftpClient.ls(remote_path);
		for (RemoteResourceInfo remoteResource : remoteFiles) {
			if (!remoteResource.isDirectory()) {
				String remoteFilePath = remote_path + "/" + remoteResource.getName();
				File localFile = new File(local_path, remoteResource.getName());
				if(remoteResource.getAttributes().getMtime() >= todayInLong) {
					log.info("Downloading file: " + remoteFilePath);
					sftpClient.get(remoteFilePath, localFile.getAbsolutePath());
				} else {
					log.info("Skip download file: " + remoteFilePath);
				}
			}
		}
		return true;
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
				.optionalStart()
				.optionalEnd()
				.appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, true)  // Handle 1-3 digit milliseconds
				.optionalEnd()
				.toFormatter();
		LocalDateTime now = LocalDateTime.now();
		result = formatter.format(now);
		result = result.contains("T") ? result.replace("T", "") : result;
		return result;
	}

	public String saveUploadFileToPath(MultipartFile multipartFile, String upload_path) throws Exception {
		Path uploadDir = Paths.get(upload_path);
		Files.createDirectories(uploadDir);
		String filename = Optional.ofNullable(multipartFile.getOriginalFilename())
				.orElseThrow(() -> new IllegalArgumentException("Filename is blank."));
		Path targetPath = uploadDir.resolve(filename);
		Files.copy(multipartFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
		return targetPath.toAbsolutePath().toString();
	}
	
	public String generatePassword(int length) {
		final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	    final String LOWER = "abcdefghijklmnopqrstuvwxyz";
	    final String DIGITS = "0123456789";
	    final String SPECIAL = "!@#$%^&*()-_=+[]{}";
	    final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL;
	    final SecureRandom random = new SecureRandom();
        // Ensure at least one of each type
        List<Character> passwordChars = new ArrayList<>();
        passwordChars.add(getRandomChar(random, UPPER));
        passwordChars.add(getRandomChar(random, LOWER));
        passwordChars.add(getRandomChar(random, DIGITS));
        passwordChars.add(getRandomChar(random, SPECIAL));
        // Fill remaining characters
        while (passwordChars.size() < length) {
            char nextChar = getRandomChar(random, ALL_CHARS);
            // Avoid repeating adjacent characters
            if (passwordChars.isEmpty() || passwordChars.get(passwordChars.size() - 1) != nextChar) {
                passwordChars.add(nextChar);
            }
        }
        // Shuffle to ensure randomness of the guaranteed characters
        Collections.shuffle(passwordChars);
        // Check again for adjacent repetitions
        for (int i = 1; i < passwordChars.size(); i++) {
            if (passwordChars.get(i).equals(passwordChars.get(i - 1))) {
                // regenerate
                return generatePassword(length); // Recursive retry
            }
        }
        // Convert to string
        StringBuilder password = new StringBuilder();
        for (char ch : passwordChars) {
            password.append(ch);
        }
        return password.toString();
    }

    private static char getRandomChar(SecureRandom random, String chars) {
        return chars.charAt(random.nextInt(chars.length()));
    }
}
