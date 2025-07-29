package com.service.keycloak;

import java.net.SocketTimeoutException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pojo.keycloak.User;

import lombok.Cleanup;

@Service
public class KeycloakService {

	public User userCreation(Logger log, String logFolder, User user) throws Exception {
		String URL = "";
		Object object = new Object();
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		try {
			log.info("URL: " + URL);
			log.info("Request: " + objectMapper.writeValueAsString(object));
			if(URL != null && !URL.isBlank()){
				/*List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("", ));*/
				RequestConfig requestConfig = RequestConfig.custom()
		                .setConnectTimeout(5 * 1000)//in miliseconds
		                .setSocketTimeout(5 * 1000)//in miliseconds
		                .setConnectionRequestTimeout(5 * 1000)//in miliseconds
		                .build();
				@Cleanup CloseableHttpClient httpClient = HttpClients.custom()
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
					throw e;
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
			throw e;
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
			throw e;
		} finally {
			try {
				
			}catch(Exception e) {}
		}
		return user;
	}

	public User userMaintenance(Logger log, String logFolder, User user) throws Exception {
		String URL = "";
		Object object = new Object();
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		try {
			log.info("URL: " + URL);
			log.info("Request: " + objectMapper.writeValueAsString(object));
			if(URL != null && !URL.isBlank()){
				/*List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("", ));*/
				RequestConfig requestConfig = RequestConfig.custom()
		                .setConnectTimeout(5 * 1000)//in miliseconds
		                .setSocketTimeout(5 * 1000)//in miliseconds
		                .setConnectionRequestTimeout(5 * 1000)//in miliseconds
		                .build();
				@Cleanup CloseableHttpClient httpClient = HttpClients.custom()
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
					throw e;
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
			throw e;
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
			throw e;
		} finally {
			try {
				
			}catch(Exception e) {}
		}
		return user;
	}

	public User checkStatus(Logger log, String logFolder, User user) throws Exception {
		String URL = "";
		Object object = new Object();
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		try {
			log.info("URL: " + URL);
			log.info("Request: " + objectMapper.writeValueAsString(object));
			if(URL != null && !URL.isBlank()){
				/*List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("", ));*/
				RequestConfig requestConfig = RequestConfig.custom()
		                .setConnectTimeout(5 * 1000)//in miliseconds
		                .setSocketTimeout(5 * 1000)//in miliseconds
		                .setConnectionRequestTimeout(5 * 1000)//in miliseconds
		                .build();
				@Cleanup CloseableHttpClient httpClient = HttpClients.custom()
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
					throw e;
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
			throw e;
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
			throw e;
		} finally {
			try {
				
			}catch(Exception e) {}
		}
		return user;
	}
}
