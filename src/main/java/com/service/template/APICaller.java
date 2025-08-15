package com.service.template;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pojo.Property;
import com.service.MTLSCertificationDetectionService;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.impl.SimpleTraceGenerator;

import lombok.Cleanup;

@Service
public class APICaller {
	
	@Autowired
	MTLSCertificationDetectionService mTlsCertificationDetectionService;
	
	@Autowired
	Property property;

	protected String httpClientApi(Logger log, String logFolder) {
		String result = "";
		String URL = "";
		Object object = new Object();
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		try {
			log.info("URL: " + URL);
			log.info("Request: " + objectMapper.writeValueAsString(object));
			if(URL != null && !URL.isBlank()){
				URI uri = URI.create(URL);
				String host = uri.getHost();
		        int port = uri.getPort() == -1 ? 443 : uri.getPort();
		        boolean mtls = mTlsCertificationDetectionService.isMTLSActive(host, port);
		        Map<String, X509Certificate[]> certChains = mTlsCertificationDetectionService.loadClientCertChains(log, property.getServer_ssl_key_store(), property.getServer_ssl_key_store_password());
		        SSLContext sslContext = SSLContext.getInstance("TLS");//TLS is general name, which version to pickup is depend on JVM setting
		        if (mtls && certChains.size() > 1) {
		            log.info("mTLS active and multiple certs found — enabling smart selection");
		            sslContext = mTlsCertificationDetectionService.createSSLContext(log, property.getServer_ssl_key_store(), property.getServer_ssl_key_store_password(), property.getServer_ssl_trust_store(), property.getServer_ssl_trust_store_password(), false);
		        } else {sslContext = mTlsCertificationDetectionService.createSSLContext(log, property.getServer_ssl_key_store(), property.getServer_ssl_key_store_password(), property.getServer_ssl_trust_store(), property.getServer_ssl_trust_store_password(), true);}
		        /*List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("", ));*/
				RequestConfig requestConfig = RequestConfig.custom()
		                .setConnectTimeout(5 * 1000)//in miliseconds
		                .setSocketTimeout(5 * 1000)//in miliseconds
		                .setConnectionRequestTimeout(5 * 1000)//in miliseconds
		                .build();
				@Cleanup CloseableHttpClient httpClient = HttpClients.custom()
						.setSSLContext(sslContext)
		                .setDefaultRequestConfig(requestConfig)
		                .build();
				HttpPost httpRequest = new HttpPost(URL);
				/*HttpGet httpRequest = new HttpGet(URL);
				URI uri = new URIBuilder(httpRequest.getURI())
						.addParameters(params)
						.build();
				httpRequest.setURI(uri);*/
				//HttpPut httpRequest = new HttpPut(URL);
				//HttpDelete httpRequest = new HttpDelete(URL);
				httpRequest.setEntity(new StringEntity(objectMapper.writeValueAsString(object)));
				httpRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
				for(Header header : httpRequest.getAllHeaders()) {
					log.info(header.getName() + "(Request): " + header.getValue());
				}
				@Cleanup CloseableHttpResponse httpResponse = httpClient.execute(httpRequest);
				for(Header header : httpResponse.getAllHeaders()) {
					log.info(header.getName() + "(Response): " + header.getValue());
				}
				HttpEntity entity = httpResponse.getEntity();
				log.info("HTTP Response code: " + httpResponse.getStatusLine().getStatusCode());
				try {
					if(entity != null) {
						String responseString = EntityUtils.toString(entity);
						log.info("Response: " + responseString);
//						Read & update the response JSON parameter value into Object
						object = objectMapper.readValue(responseString, Object.class);
					}
				} catch(Exception e) {
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
		} catch(SocketTimeoutException | ConnectTimeoutException e) {
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
		} catch(Exception e) {
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
		} finally {
			try {
				
			}catch(Exception e) {}
		}
		return result;
	}

	protected String httpClientApiAsync(Logger log, String logFolder) {
		String result = "";
		String URL = "";
		Object object = new Object();
		CompletableFuture<Object> futureObject = new CompletableFuture<>();
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		try {
			log.info("URL: " + URL);
			log.info("Request: " + objectMapper.writeValueAsString(object));
			if(URL != null && !URL.isBlank()){
				URI uri = URI.create(URL);
				String host = uri.getHost();
		        int port = uri.getPort() == -1 ? 443 : uri.getPort();
		        boolean mtls = mTlsCertificationDetectionService.isMTLSActive(host, port);
		        Map<String, X509Certificate[]> certChains = mTlsCertificationDetectionService.loadClientCertChains(log, property.getServer_ssl_key_store(), property.getServer_ssl_key_store_password());
		        SSLContext sslContext = SSLContext.getInstance("TLS");//TLS is general name, which version to pickup is depend on JVM setting
		        if (mtls && certChains.size() > 1) {
		            log.info("mTLS active and multiple certs found — enabling smart selection");
		            sslContext = mTlsCertificationDetectionService.createSSLContext(log, property.getServer_ssl_key_store(), property.getServer_ssl_key_store_password(), property.getServer_ssl_trust_store(), property.getServer_ssl_trust_store_password(), false);
		        } else {sslContext = mTlsCertificationDetectionService.createSSLContext(log, property.getServer_ssl_key_store(), property.getServer_ssl_key_store_password(), property.getServer_ssl_trust_store(), property.getServer_ssl_trust_store_password(), true);}
				/*List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("", ));*/
				RequestConfig requestConfig = RequestConfig.custom()
		                .setConnectTimeout(5 * 1000)//in miliseconds
		                .setSocketTimeout(5 * 1000)//in miliseconds
		                .setConnectionRequestTimeout(5 * 1000)//in miliseconds
		                .build();
				@Cleanup CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom()
						.setSSLContext(sslContext)
		                .setDefaultRequestConfig(requestConfig)
		                .build();
                httpClient.start();
				HttpPost httpRequest = new HttpPost(URL);
				/*HttpGet httpRequest = new HttpGet(URL);
				URI uri = new URIBuilder(httpRequest.getURI())
						.addParameters(params)
						.build();
				httpRequest.setURI(uri);*/
				//HttpPut httpRequest = new HttpPut(URL);
				//HttpDelete httpRequest = new HttpDelete(URL);
				httpRequest.setEntity(new StringEntity(objectMapper.writeValueAsString(object)));
				httpRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
				for(Header header : httpRequest.getAllHeaders()) {
					log.info(header.getName() + "(Request): " + header.getValue());
				}
				httpClient.execute(httpRequest, new FutureCallback<HttpResponse>() {
	                @Override
	                public void completed(HttpResponse httpResponse) {
	                    try {
	                    	for(Header header : httpResponse.getAllHeaders()) {
	        					log.info(header.getName() + "(Response): " + header.getValue());
	        				}
	        				log.info("HTTP Response code: " + httpResponse.getStatusLine().getStatusCode());
	                    	HttpEntity entity = httpResponse.getEntity();
	        				String responseString = EntityUtils.toString(entity);
							log.info("Response: " + responseString);
//							Read & update the response JSON parameter value into Object
							Object object = objectMapper.readValue(responseString, Object.class);
	                        futureObject.complete(object);
	                    } catch (Exception e) {
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
	                        futureObject.completeExceptionally(e);
	                    }
	                }

	                @Override
	                public void failed(Exception e) {
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
						futureObject.completeExceptionally(e);
	                }

	                @Override
	                public void cancelled() {
	                    log.warn("Async HTTP request cancelled");
	                    futureObject.cancel(true);
	                }
	            });
				// Option A: Throws checked exceptions
				object = futureObject.get();  // blocks until completed
				// Option B: Throws unchecked exceptions
				object = futureObject.join();  // same as get(), but no checked exception
				// Option C: You don't "get" it immediately, but instead you handle it via callback
				futureObject.thenAccept(obj -> {
				    // Do something with the result
				    
				});
			}
		} catch(SocketTimeoutException | ConnectTimeoutException e) {
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
		} catch(Exception e) {
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
		} finally {
			try {
				
			}catch(Exception e) {}
		}
		return result;
	}
	
	public String iso8583Api(Logger log, String logFolder) {
		String result = "";
		String ip = "";
		int port = 0;
		Socket socket;
		OutputStream outputStream;
		InputStream inputStream;
		try {           
			log.info("IP: " + ip);
			log.info("PORT: " + port);
			// Create a new MessageFactory
            MessageFactory<IsoMessage> requestFactory = new MessageFactory<>();
            requestFactory.setUseBinaryMessages(true); // Use binary messages for efficiency
            requestFactory.setAssignDate(true);
            requestFactory.setTraceNumberGenerator(new SimpleTraceGenerator((int) (System.currentTimeMillis() % 100000)));
            
            // Create a new ISO 8583 message
            IsoMessage request = requestFactory.newMessage(0x200);
            request.setValue(3, "000000", IsoType.NUMERIC, 6); // Processing Code (Sale)
            request.setValue(4, "10000", IsoType.NUMERIC, 12); // Transaction Amount (in cents)
            request.setValue(7, "0729075811", IsoType.NUMERIC, 10); // Transmission Date and Time (MMDDhhmmss)
            request.setValue(11, "123456", IsoType.NUMERIC, 6); // System Trace Audit Number
            request.setValue(41, "12345678", IsoType.ALPHA, 8); // Card Acceptor Terminal ID
            request.setValue(42, "EXTIOTECH", IsoType.ALPHA, 12); // Card Acceptor ID
            
            // Calculate and set the Message Authentication Code (MAC) if needed

            // Convert the message to byte array for transmission over the network
            log.info("Request: " + request.debugString());
            byte[] messageBytes = request.writeData();
            
            // Send the ISO 8583 message to the server
            socket = new Socket(ip, port);
            outputStream = socket.getOutputStream();
            outputStream.write(messageBytes);
            outputStream.flush();
            
            //Read the iso8583 response & process it
            inputStream = socket.getInputStream();
            byte[] responseBytes = new byte[inputStream.available()]; 
            int bytesRead = inputStream.read(responseBytes);
            if (bytesRead > 0) {
            	MessageFactory<IsoMessage> responseFactory = new MessageFactory<>();
            	responseFactory.setUseBinaryMessages(true);
            	IsoMessage response = responseFactory.parseMessage(responseBytes, 0);
            	log.info("Response: " + response.debugString());
            	/*String temp = response.hasField() ? response.getField().toString() : "";*/
            }
		} catch(SocketTimeoutException | ConnectTimeoutException | SocketException e) {
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
		} catch(Exception e) {
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
		} finally {
			try {
				
			}catch(Exception e) {}
		}
		return result;
	}
	
	//Async api call using webflux client
	protected String webClient(Logger log, String logFolder) {
		String result = "";
		String URL = "";
		Object object = new Object();
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		try {
			log.info("URL: " + URL);
			log.info("Request: " + objectMapper.writeValueAsString(object));
			if(URL != null && !URL.isBlank()){
				WebClient webClient = WebClient.builder()
						// Optional: Define base URL
						// .baseUrl("https://your-api-base-url.com")
						.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.build();
				RequestHeadersSpec<?> requestHeadersSpec = webClient
						.post() // ⬅️ Change HTTP verb here
						// .get()
						// .put()
						// .delete()
						.uri(uriBuilder -> uriBuilder
								.path(URL)
								// .queryParam("paramName", "paramValue") // <-- Uncomment for query params
								.build()
								)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.bodyValue(objectMapper.writeValueAsString(object));
				// Optional: Add custom headers
				// requestHeadersSpec = requestHeadersSpec.header("X-Custom-Header", "value");
				String responseString = requestHeadersSpec //Change data type from String to Mono<String> if async & vice versa
						.retrieve()
						.onStatus(HttpStatusCode::isError, clientResponse ->
						clientResponse.bodyToMono(String.class)
						.map(errorBody -> {
							log.error("HTTP Error: {}", errorBody);
							return new RuntimeException("Error Response: " + errorBody);
						})
								)
						.bodyToMono(String.class)
						.timeout(Duration.ofSeconds(5))
						.doOnSuccess(body -> log.info("Response: {}", body))
						.block();// Remove .block() if want to be aysnc
				// Read & update the response JSON parameter value into Object & vice versa
				object = objectMapper.readValue(responseString, Object.class);
			}
		} catch(Exception e) {
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
		} finally {
			try {

			}catch(Exception e) {}
		}
		return result;
	}
}
