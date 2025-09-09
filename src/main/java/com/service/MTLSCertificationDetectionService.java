package com.service;

import java.io.FileInputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
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

@Service
public class MTLSCertificationDetectionService {

	// ====== Detection Utils ======
	public boolean isMTLSActive(String host, int port) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
				socket.startHandshake();
				return false; // Handshake worked without client cert → not mTLS
			}
		} catch (SSLHandshakeException e) {
			// Typical message: "Received fatal alert: handshake_failure"
			return true; // Server expected client cert → mTLS required
		} catch (Throwable e) {
			return false;
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
			// Initialize keystore of the given type (e.g., JKS, PKCS12)
			KeyStore ks = KeyStore.getInstance(keystoreType);
			try (FileInputStream fis = new FileInputStream(keystorePath)) {
				ks.load(fis, keystorePassword.toCharArray());
			}

			// Iterate over all aliases in the keystore
			Map<String, X509Certificate[]> certChains = new HashMap<>();
			for (Enumeration<String> e = ks.aliases(); e.hasMoreElements();) {
				String alias = e.nextElement();
				// Only consider entries that contain private keys (i.e., client certs)
				if (ks.isKeyEntry(alias)) {
					Certificate[] chain = ks.getCertificateChain(alias);
					// If the entry has a valid X509 certificate chain, add it to the map
					if (chain != null && chain.length > 0 && chain[0] instanceof X509Certificate) {
						certChains.put(alias, Arrays.copyOf(chain, chain.length, X509Certificate[].class));
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
	 * Custom implementation of X509ExtendedKeyManager that enhances
	 * the default certificate selection process for mutual TLS (mTLS).
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
		 */
		public CustomX509ExtendedKeyManager(X509ExtendedKeyManager baseKeyManager, Map<String, X509Certificate[]> certChains, List<String> preferredSubjects) {
			this.baseKeyManager = baseKeyManager;
			this.certChains = certChains;
			this.preferredSubjects = preferredSubjects;
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
			// =========================================================
			// Step 1: Try to match against preferred subjects
			// =========================================================
			// - If a list of preferred subjects (Subject DN or SAN patterns) 
			//   is provided in configuration, check each client certificate 
			//   chain in the keystore.
			// - If the Subject DN contains one of the preferred strings, 
			//   return that alias immediately.
			// - This allows fine-grained control (e.g., always use CN=BankA-Client1).
			if (preferredSubjects != null && !preferredSubjects.isEmpty()) {
				for (String preferred : preferredSubjects) {
					for (Map.Entry<String, X509Certificate[]> entry : certChains.entrySet()) {
						X509Certificate cert = entry.getValue()[0];
						String subjectDN = cert.getSubjectX500Principal().getName();
						if (subjectDN.contains(preferred)) {
							return entry.getKey(); // Found a cert with matching Subject DN
						}
					}
				}
			}

			// =========================================================
			// Step 2: Try to match against server-requested issuers
			// =========================================================
			// - During the TLS handshake, the server can request that the 
			//   client present a certificate issued by one of a set of CAs.
			// - If any of our cert chains were issued by one of those CAs 
			//   (Issuer DN matches), return the corresponding alias.
			if (issuers != null && issuers.length > 0) {
				for (Principal issuer : issuers) {
					for (Map.Entry<String, X509Certificate[]> entry : certChains.entrySet()) {
						X509Certificate cert = entry.getValue()[0];
						if (cert.getIssuerX500Principal().equals(issuer)) {
							return entry.getKey(); // Found a cert issued by requested CA
						}
					}
				}
			}

			// Step 3: Fallback → delegate to default KeyManager
			return baseKeyManager.chooseClientAlias(keyTypes, issuers, socket);
		}

		/**
		 * Returns the list of available server aliases.
		 * (Usually relevant for TLS servers, not clients.)
		 * Delegates to the base KeyManager.
		 */
		@Override
		public String[] getServerAliases(String keyType, Principal[] issuers) {
			return baseKeyManager.getServerAliases(keyType, issuers);
		}

		/**
		 * Chooses which server alias to use during handshake (server-side TLS).
		 * Delegates to the base KeyManager.
		 */
		@Override
		public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
			return baseKeyManager.chooseServerAlias(keyType, issuers, socket);
		}

		/**
		 * Returns the certificate chain for a given alias.
		 * Delegates to the base KeyManager.
		 */
		@Override
		public X509Certificate[] getCertificateChain(String alias) {
			return baseKeyManager.getCertificateChain(alias);
		}

		/**
		 * Returns the private key for a given alias.
		 * Delegates to the base KeyManager.
		 */
		@Override
		public PrivateKey getPrivateKey(String alias) {
			return baseKeyManager.getPrivateKey(alias);
		}
	}

	// ====== Create SSLContext with smart cert selection ======
	public SSLContext createSSLContext(Logger log, String server_ssl_protocol, String keystorePath, String keystorePassword, String keystoreType, String truststorePath, String truststorePassword, String truststoreType, boolean onlyTrustManager, List<String> preferredSubjects) {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance(server_ssl_protocol);//TLS is general name, which version to pickup is depend on JVM setting
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore ks = KeyStore.getInstance(truststoreType);
			var is = KeyStore.getDefaultType().getClass().getResourceAsStream(truststorePath);
			ks.load(is, truststorePassword.toCharArray());
			tmf.init(ks);
			if(onlyTrustManager) {
				sslContext.init(null, tmf.getTrustManagers(), null);
				return sslContext;
			}
			ks = KeyStore.getInstance(keystoreType);
			try (FileInputStream fis = new FileInputStream(keystorePath)) {
				ks.load(fis, keystorePassword.toCharArray());
			}

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, keystorePassword.toCharArray());

			X509ExtendedKeyManager defaultKm = null;
			for (KeyManager km : kmf.getKeyManagers()) {
				if (km instanceof X509ExtendedKeyManager) {
					defaultKm = (X509ExtendedKeyManager) km;
					break;
				}
			}
			if (defaultKm == null) {
				throw new IllegalStateException("No X509ExtendedKeyManager found");
			}

			Map<String, X509Certificate[]> certChains = loadClientCertChains(log, keystorePath, keystorePassword, keystoreType);
			CustomX509ExtendedKeyManager smartKm = new CustomX509ExtendedKeyManager(defaultKm, certChains, preferredSubjects);

			sslContext.init(new KeyManager[]{smartKm}, tmf.getTrustManagers(), null);
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
