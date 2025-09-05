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
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
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
				SSLSession session = socket.getSession();
				Certificate[] localCerts = session.getLocalCertificates();
				return localCerts != null && localCerts.length > 0;
			}
		} catch (Throwable e) {
			return false;
		}
	}

	public Map<String, X509Certificate[]> loadClientCertChains(Logger log, String keystorePath, String keystorePassword) {
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			try (FileInputStream fis = new FileInputStream(keystorePath)) {
				ks.load(fis, keystorePassword.toCharArray());
			}

			Map<String, X509Certificate[]> certChains = new HashMap<>();
			for (Enumeration<String> e = ks.aliases(); e.hasMoreElements();) {
				String alias = e.nextElement();
				if (ks.isKeyEntry(alias)) {
					Certificate[] chain = ks.getCertificateChain(alias);
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

	// ====== Custom smart X509 KeyManager ======
	public class CustomX509ExtendedKeyManager extends X509ExtendedKeyManager {
		private final X509ExtendedKeyManager baseKeyManager;
		private final Map<String, X509Certificate[]> certChains;

		public CustomX509ExtendedKeyManager(X509ExtendedKeyManager baseKeyManager, Map<String, X509Certificate[]> certChains) {
			this.baseKeyManager = baseKeyManager;
			this.certChains = certChains;
		}

		@Override
		public String[] getClientAliases(String keyType, Principal[] issuers) {
			return baseKeyManager.getClientAliases(keyType, issuers);
		}

		@Override
		public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
			if (issuers != null && issuers.length > 0) {
				for (Principal issuer : issuers) {
					for (Map.Entry<String, X509Certificate[]> entry : certChains.entrySet()) {
						X509Certificate cert = entry.getValue()[0];
						if (cert.getIssuerX500Principal().equals(issuer)) {
							return entry.getKey();
						}
					}
				}
			}
			// Fallback: default alias selection
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

	// ====== Create SSLContext with smart cert selection ======
	public SSLContext createSSLContext(Logger log, String keystorePath, String keystorePassword, String truststorePath, String truststorePassword, boolean onlyTrustManager) {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");//TLS is general name, which version to pickup is depend on JVM setting
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	        var is = KeyStore.getDefaultType().getClass().getResourceAsStream(truststorePath);
	        ks.load(is, truststorePassword.toCharArray());
	        tmf.init(ks);
			if(onlyTrustManager) {
		        sslContext.init(null, tmf.getTrustManagers(), null);
				return sslContext;
			}
			ks = KeyStore.getInstance("JKS");
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

			Map<String, X509Certificate[]> certChains = loadClientCertChains(log, keystorePath, keystorePassword);
			CustomX509ExtendedKeyManager smartKm = new CustomX509ExtendedKeyManager(defaultKm, certChains);

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
