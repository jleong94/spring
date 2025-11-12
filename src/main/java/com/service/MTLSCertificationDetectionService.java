package com.service;

import java.io.FileInputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import lombok.Cleanup;

@Service
public class MTLSCertificationDetectionService {

	/**
	 * Detects if MTLS is active on the target server
	 * @param host Target hostname
	 * @param port Target port
	 * @return true if MTLS is required, false otherwise
	 */
	public boolean isMTLSActive(String host, int port) {
		SSLSocketFactory factory = null;
		SSLSocket socket = null;
		try {
			factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			socket = (SSLSocket) factory.createSocket(host, port);
			socket.startHandshake();
			return false; // Handshake worked without client cert → not MTLS
		} catch (SSLHandshakeException e) {
			// Typical message: "Received fatal alert: handshake_failure"
			return true; // Server expected client cert → mTLS required
		} catch (Throwable e) {
			return false;
		} finally {
			try {
				if(socket != null) {socket.close();}
			} catch (Throwable e) {}
		}
	}

	/**
	 * Loads all client certificate chains from a given keystore.
	 *
	 * @param log              Logger for error reporting
	 * @param keystorePath     Path to the keystore file (e.g., .jks or .p12)
	 * @param keystorePassword Password for the keystore
	 * @param keystoreType     Type of the keystore (e.g., "JKS", "PKCS12")
	 * @return Map of alias -> certificate chain (X509Certificate[]), or empty map if error occurs
	 */
	public Map<String, X509Certificate[]> loadClientCertChains(Logger log, String keystorePath, String keystorePassword, String keystoreType) {
		try {
			@Cleanup FileInputStream fis = new FileInputStream(keystorePath);
			KeyStore ks = KeyStore.getInstance(keystoreType);
			ks.load(fis, keystorePassword.toCharArray());

			// Iterate over all aliases in the keystore
			Map<String, X509Certificate[]> certChains = new HashMap<>();
			for (Enumeration<String> e = ks.aliases(); e.hasMoreElements();) {
				String alias = e.nextElement();
				// Only consider entries that contain private keys (i.e., client certs)
				if (ks.isKeyEntry(alias)) {
					Certificate[] chain = ks.getCertificateChain(alias);
					// Validate and convert to X509Certificate array
					if (chain != null && chain.length > 0) {
						X509Certificate[] x509Chain = convertToX509Chain(log, chain, alias);
						if (x509Chain != null && x509Chain.length > 0) {
							certChains.put(alias, x509Chain);
						}
					}
				}
			}
			return certChains;
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
			return Collections.emptyMap();
		}
	}
	
	/**
	 * Safely converts Certificate[] to X509Certificate[] with validation
	 */
	private X509Certificate[] convertToX509Chain(Logger log, Certificate[] chain, String alias) {
		List<X509Certificate> result = new ArrayList<>();
		
		for (int i = 0; i < chain.length; i++) {
			if (chain[i] instanceof X509Certificate) {
				result.add((X509Certificate) chain[i]);
			} else {
				log.warn("Certificate at index {} in chain for alias '{}' is not X509Certificate, type: {}", 
				         i, alias, chain[i].getClass().getName());
			}
		}
		
		if (result.isEmpty()) {
			log.warn("No valid X509 certificates found in chain for alias '{}'", alias);
			return null;
		}
		
		return result.toArray(new X509Certificate[0]);
	}

	/**
	 * Custom implementation of X509ExtendedKeyManager that enhances
	 * the default certificate selection process for mutual TLS (MTLS).
	 *
	 * Use case:
	 * - When multiple client certificates exist in the keystore.
	 * - Allows smart selection of the correct certificate alias
	 *   based on the server's requested issuer(s).
	 *
	 * Fallback:
	 * - If no match is found against requested issuers, delegate back
	 *   to the base (default) KeyManager implementation.
	 */
	public class CustomX509ExtendedKeyManager extends X509ExtendedKeyManager {
		private final X509ExtendedKeyManager baseKeyManager;
		private final Map<String, X509Certificate[]> certChains;
		private final List<String> preferredSubjects; // optional config e.g. ["CN=BankA-Client1"]

		/**
		 * @param baseKeyManager The default system KeyManager (delegated for fallback).
		 * @param certChains     Map of keystore aliases to their certificate chains.
		 * @param preferredSubjects Optional list of preferred subject DN patterns
		 */
		public CustomX509ExtendedKeyManager(X509ExtendedKeyManager baseKeyManager, Map<String, X509Certificate[]> certChains, List<String> preferredSubjects) {
			this.baseKeyManager = baseKeyManager;
			this.certChains = certChains;
			this.preferredSubjects = preferredSubjects != null && !preferredSubjects.isEmpty() ? preferredSubjects : Collections.emptyList();
		}

		/**
		 * Returns the list of available client aliases for a given key type and issuers.
		 * Delegates to the base KeyManager.
		 */
		@Override
		public String[] getClientAliases(String keyType, Principal[] issuers) {
			return baseKeyManager.getClientAliases(keyType, issuers);
		}

		@Override
		public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
			// Step 1: Try to match against preferred subjects
			if (preferredSubjects != null && !preferredSubjects.isEmpty()) {
				for (String preferred : preferredSubjects) {
					for (Map.Entry<String, X509Certificate[]> entry : certChains.entrySet()) {
						if (entry.getValue() != null && entry.getValue().length > 0) {
							X509Certificate cert = entry.getValue()[0];
							String subjectDN = cert.getSubjectX500Principal().getName();
							if (subjectDN.contains(preferred)) {
								return entry.getKey(); // Found a cert with matching Subject DN
							}
						}
					}
				}
			}

			// Step 2: Try to match against server-requested issuers
			if (issuers != null && issuers.length > 0) {
				for (Principal issuer : issuers) {
					for (Map.Entry<String, X509Certificate[]> entry : certChains.entrySet()) {
						if (entry.getValue() != null && entry.getValue().length > 0) {
							X509Certificate cert = entry.getValue()[0];
							if (cert.getIssuerX500Principal().equals(issuer)) {
								return entry.getKey(); // Found a cert issued by requested CA
							}
						}
					}
				}
			}

			// Step 3: Fallback → delegate to default KeyManager
			return baseKeyManager.chooseClientAlias(keyTypes, issuers, socket);
		}

		@Override
		public String[] getServerAliases(String keyType, Principal[] issuers) {
			return baseKeyManager.getServerAliases(keyType, issuers);
		}

		@Override
		public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
			return baseKeyManager.chooseServerAlias(keyType, issuers, socket);
		}

		@Override
		public X509Certificate[] getCertificateChain(String alias) {
			return baseKeyManager.getCertificateChain(alias);
		}

		@Override
		public PrivateKey getPrivateKey(String alias) {
			return baseKeyManager.getPrivateKey(alias);
		}
	}

	/**
	 * Creates an SSLContext with optional smart certificate selection
	 * 
	 * @param log Logger instance
	 * @param server_ssl_protocol SSL protocol (e.g., "TLS")
	 * @param keystorePath Path to keystore
	 * @param keystorePassword Keystore password
	 * @param keystoreType Keystore type (e.g., "JKS", "PKCS12")
	 * @param truststorePath Path to truststore
	 * @param truststorePassword Truststore password
	 * @param truststoreType Truststore type
	 * @param onlyTrustManager If true, only initialize with TrustManager (no client certs)
	 * @param preferredSubjects Optional list of preferred certificate subjects for smart selection
	 * @return Configured SSLContext or null if error occurs
	 */
	public SSLContext createSSLContext(Logger log, String server_ssl_protocol, String keystorePath, String keystorePassword, String keystoreType, String truststorePath, String truststorePassword, String truststoreType, boolean onlyTrustManager, List<String> preferredSubjects) {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance(server_ssl_protocol);//TLS is general name, which version to pickup is depend on JVM setting
			// Initialize TrustManager
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore trustStore = KeyStore.getInstance(truststoreType);
			// Load truststore from file
			@Cleanup FileInputStream fis = new FileInputStream(truststorePath);
			trustStore.load(fis, truststorePassword.toCharArray());
			tmf.init(trustStore);
			// If only TrustManager is needed (no client cert)
			if (onlyTrustManager) {
				sslContext.init(null, tmf.getTrustManagers(), null);
				log.info("SSLContext initialized with TrustManager only (no client certificates)");
				return sslContext;
			}
			// Initialize KeyManager with client certificates
			KeyStore keyStore = KeyStore.getInstance(keystoreType);
			fis = new FileInputStream(keystorePath);
			keyStore.load(fis, keystorePassword.toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, keystorePassword.toCharArray());
			// Find the default X509ExtendedKeyManager
			X509ExtendedKeyManager defaultKm = null;
			for (KeyManager km : kmf.getKeyManagers()) {
				if (km instanceof X509ExtendedKeyManager) {
					defaultKm = (X509ExtendedKeyManager) km;
					break;
				}
			}
			if (defaultKm == null) {
				throw new IllegalStateException("No X509ExtendedKeyManager found in KeyManagerFactory");
			}
			// Create custom KeyManager with smart certificate selection
			Map<String, X509Certificate[]> certChains = loadClientCertChains(log, keystorePath, keystorePassword, keystoreType);
			CustomX509ExtendedKeyManager smartKm = new CustomX509ExtendedKeyManager(defaultKm, certChains, preferredSubjects);
			sslContext.init(new KeyManager[]{smartKm}, tmf.getTrustManagers(), null);
			log.info("SSLContext initialized with custom KeyManager (smart certificate selection enabled)");			
			return sslContext;
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
			return sslContext;
		}
	}
}
