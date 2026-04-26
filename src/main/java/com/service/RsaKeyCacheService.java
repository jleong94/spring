package com.service;

import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pojo.Property;
import com.utilities.Tool;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Service that caches RSA public and private keys in memory.
 * Keys are loaded on application startup and refreshed every 5 minutes.
 */
@Slf4j
@Service
public class RsaKeyCacheService {

	private final Tool tool;
	private final Property property;

	// Thread-safe maps to store cached keys
	private final Map<String, PublicKey> publicKeyCache = new ConcurrentHashMap<>();
	private final Map<String, PrivateKey> privateKeyCache = new ConcurrentHashMap<>();

	public RsaKeyCacheService(Tool tool, Property property) {
		this.tool = tool;
		this.property = property;
	}

	/**
	 * Loads all RSA keys on application startup
	 */
	@PostConstruct
	public void init() {
		log.info("=== Initializing RSA Key Cache ===");
		loadAllKeys();
		log.info("=== RSA Key Cache Initialization Complete ===");
	}

	/**
	 * Refreshes all RSA keys every 5 minutes (300000 milliseconds)
	 */
	@Scheduled(fixedRate = 300000)
	public void refreshKeys() {
		log.info("=== Refreshing RSA Key Cache ===");
		loadAllKeys();
		log.info("=== RSA Key Cache Refresh Complete ===");
	}

	/**
	 * Loads all RSA public and private keys from the classpath
	 */
	private void loadAllKeys() {
		try {
			String classpath = property.getSpring_application_api_key();
			log.info("Loading RSA keys from classpath: {}", classpath);

			// Clear existing caches
			publicKeyCache.clear();
			privateKeyCache.clear();

			// Load all files from the specified classpath
			List<Path> paths = tool.loadFileListFromClasspath(log, classpath);

			int publicKeyCount = 0;
			int privateKeyCount = 0;

			// Iterate through files and load RSA keys
			for (Path path : paths) {
				String fileName = path.getFileName().toString();

				// Load public keys
				if (fileName.contains("rsa-public")) {
					try {
						PublicKey publicKey = loadPublicKey(classpath, fileName);
						String keyId = extractKeyId(fileName, "rsa-public");
						publicKeyCache.put(keyId, publicKey);
						publicKeyCount++;
						log.info("Loaded public key: {} -> Key ID: {}", fileName, keyId);
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
					}
				}

				// Load private keys
				if (fileName.contains("rsa-private")) {
					try {
						PrivateKey privateKey = loadPrivateKey(classpath, fileName);
						String keyId = extractKeyId(fileName, "rsa-private");
						privateKeyCache.put(keyId, privateKey);
						privateKeyCount++;
						log.info("Loaded private key: {} -> Key ID: {}", fileName, keyId);
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
					}
				}
			}

			log.info("Successfully loaded {} public key(s) and {} private key(s)", publicKeyCount, privateKeyCount);
			log.info("Public key IDs: {}", publicKeyCache.keySet());
			log.info("Private key IDs: {}", privateKeyCache.keySet());

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
		}
	}

	/**
	 * Extracts the key ID from the filename
	 * Example: "spring-rsa-public.pem" -> "spring"
	 */
	private String extractKeyId(String fileName, String keyType) {
		String keyId = fileName;

		// Remove file extension
		int dotIndex = keyId.lastIndexOf('.');
		if (dotIndex > 0) {
			keyId = keyId.substring(0, dotIndex);
		}

		// Remove the key type suffix
		String suffix = "-" + keyType;
		if (keyId.endsWith(suffix)) {
			keyId = keyId.substring(0, keyId.length() - suffix.length());
		}

		return keyId;
	}

	/**
	 * Loads a public key from file
	 */
	private PublicKey loadPublicKey(String classpath, String fileName) throws Throwable {
		// Read the public key file content
		String strPk = tool.readFileWithBufferedReader(log, classpath.concat("/").concat(fileName));

		// Remove PEM headers/footers and whitespace to get raw Base64 data
		String realPK = strPk.replace("-----BEGIN PUBLIC KEY-----", "")
				.replace("-----END PUBLIC KEY-----", "")
				.replaceAll("\\s", "");

		// Decode the Base64-encoded public key
		byte[] encodedPublicKey = Base64.decodeBase64(realPK);

		// Create X.509 key specification (standard format for public keys)
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedPublicKey);

		// Generate the PublicKey object
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(keySpec);
	}

	/**
	 * Loads a private key from file
	 */
	private PrivateKey loadPrivateKey(String classpath, String fileName) throws Throwable {
		// Read the private key file content
		String strPk = tool.readFileWithBufferedReader(log, classpath.concat("/").concat(fileName));

		// Remove PEM headers/footers and whitespace to get raw Base64 data
		String realPK = strPk.replace("-----BEGIN RSA PRIVATE KEY-----", "")
				.replace("-----END RSA PRIVATE KEY-----", "")
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s", "");

		// Decode the Base64-encoded private key
		byte[] encodedPrivateKey = Base64.decodeBase64(realPK);

		// Detect format and extract PKCS#1 data
		byte[] pkcs1Data;
		if (tool.isPKCS8Format(encodedPrivateKey)) {
			log.debug("Detected PKCS#8 format for {}, extracting PKCS#1 data", fileName);
			pkcs1Data = tool.extractPKCS1FromPKCS8(encodedPrivateKey);
		} else {
			log.debug("Detected PKCS#1 format for {}", fileName);
			pkcs1Data = encodedPrivateKey;
		}

		// Parse PKCS#1 data to get key specification
		RSAPrivateCrtKeySpec keySpec = tool.parsePKCS1(pkcs1Data);

		// Generate the PrivateKey object
		KeyFactory factory = KeyFactory.getInstance("RSA");
		return factory.generatePrivate(keySpec);
	}

	/**
	 * Retrieves a cached public key by key ID
	 * 
	 * @param keyId The key identifier
	 * @return PublicKey if found, null otherwise
	 */
	public PublicKey getPublicKey(String keyId) {
		PublicKey key = publicKeyCache.get(keyId);
		if (key == null) {
			log.warn("Public key not found in cache for key ID: {}", keyId);
		}
		return key;
	}

	/**
	 * Retrieves a cached private key by key ID
	 * 
	 * @param keyId The key identifier
	 * @return PrivateKey if found, null otherwise
	 */
	public PrivateKey getPrivateKey(String keyId) {
		PrivateKey key = privateKeyCache.get(keyId);
		if (key == null) {
			log.warn("Private key not found in cache for key ID: {}", keyId);
		}
		return key;
	}

	/**
	 * Checks if a public key exists in the cache
	 */
	public boolean hasPublicKey(String keyId) {
		return publicKeyCache.containsKey(keyId);
	}

	/**
	 * Checks if a private key exists in the cache
	 */
	public boolean hasPrivateKey(String keyId) {
		return privateKeyCache.containsKey(keyId);
	}

	/**
	 * Gets the count of cached public keys
	 */
	public int getPublicKeyCount() {
		return publicKeyCache.size();
	}

	/**
	 * Gets the count of cached private keys
	 */
	public int getPrivateKeyCount() {
		return privateKeyCache.size();
	}
}
