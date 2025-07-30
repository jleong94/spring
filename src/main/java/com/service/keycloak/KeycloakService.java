package com.service.keycloak;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pojo.Property;
import com.pojo.keycloak.User;

import lombok.Cleanup;

@Service
public class KeycloakService {
	
	@Autowired
	Property property;
	
	@Cacheable("keycloak-admin-token")
	private String requestAdminToken(Logger log, String logFolder) throws Exception {
		String result = "";
		String URL = "";
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		try {
			URL = URL.concat(property.getKeycloak_base_url()).concat("/realms/").concat(property.getKeycloak_realm()).concat("/protocol/openid-connect/token");
			log.info("URL: " + URL);
			if(URL != null && !URL.isBlank()){
				List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("grant_type", "password")); //authorization_code, client_credentials, password, refresh_token
				params.add(new BasicNameValuePair("client_id", property.getKeycloak_client_id()));
				params.add(new BasicNameValuePair("username", property.getKeycloak_username()));
				params.add(new BasicNameValuePair("password", property.getKeycloak_password()));
				RequestConfig requestConfig = RequestConfig.custom()
		                .setConnectTimeout(5 * 1000)//in miliseconds
		                .setSocketTimeout(5 * 1000)//in miliseconds
		                .setConnectionRequestTimeout(5 * 1000)//in miliseconds
		                .build();
				@Cleanup CloseableHttpClient httpClient = HttpClients.custom()
		                .setDefaultRequestConfig(requestConfig)
		                .build();
				HttpPost httpRequest = new HttpPost(URL);
				// HttpGet httpRequest = new HttpGet(URL);
				URI uri = new URIBuilder(httpRequest.getURI())
						.addParameters(params)
						.build();
				httpRequest.setURI(uri);
				//HttpPut httpRequest = new HttpPut(URL);
				//HttpDelete httpRequest = new HttpDelete(URL);
				httpRequest.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
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
		return result;
	}

	public User userCreation(Logger log, String logFolder, User user) throws Exception {
		String URL = "", token = "";
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		try {
			token = requestAdminToken(log, logFolder);
			URL = URL.concat(property.getKeycloak_base_url()).concat("/admin/realms/").concat(property.getKeycloak_realm()).concat("/users");
			log.info("URL: " + URL);
			log.info("Request: " + objectMapper.writeValueAsString(user));
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
				httpRequest.setEntity(new StringEntity(objectMapper.writeValueAsString(user)));
				httpRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
				httpRequest.setHeader("Authorization", "Bearer " + token);
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
						user = objectMapper.readValue(responseString, User.class);
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

	public User userMaintenance(Logger log, String logFolder, User user, String token) throws Exception {
		String URL = "";
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		try {
			token = token == null || token.isBlank() ? requestAdminToken(log, logFolder) : token;
			log.info("URL: " + URL);
			log.info("Request: " + objectMapper.writeValueAsString(user));
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
				httpRequest.setEntity(new StringEntity(objectMapper.writeValueAsString(user)));
				httpRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
				httpRequest.setHeader("Authorization", "Bearer " + token);
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
						user = objectMapper.readValue(responseString, User.class);
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

	public User getUserDetailByUsername(Logger log, String logFolder, User user, String token) throws Exception {
		String URL = "";
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		try {
			token = token == null || token.isBlank() ? requestAdminToken(log, logFolder) : token;
			URL = URL.concat(property.getKeycloak_base_url()).concat("/admin/realms/").concat(property.getKeycloak_realm()).concat("/users");
			log.info("URL: " + URL);
			log.info("Request: " + objectMapper.writeValueAsString(user));
			if(URL != null && !URL.isBlank()){
				List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("username", user.getUsername()));
				RequestConfig requestConfig = RequestConfig.custom()
		                .setConnectTimeout(5 * 1000)//in miliseconds
		                .setSocketTimeout(5 * 1000)//in miliseconds
		                .setConnectionRequestTimeout(5 * 1000)//in miliseconds
		                .build();
				@Cleanup CloseableHttpClient httpClient = HttpClients.custom()
		                .setDefaultRequestConfig(requestConfig)
		                .build();
				// HttpPost httpRequest = new HttpPost(URL);
				HttpGet httpRequest = new HttpGet(URL);
				URI uri = new URIBuilder(httpRequest.getURI())
						.addParameters(params)
						.build();
				httpRequest.setURI(uri);
				log.info("Full URL: " + httpRequest.getURI());
				//HttpPut httpRequest = new HttpPut(URL);
				//HttpDelete httpRequest = new HttpDelete(URL);
				//httpRequest.setEntity(new StringEntity(objectMapper.writeValueAsString(user)));
				httpRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
				httpRequest.setHeader("Authorization", "Bearer " + token);
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
						user = objectMapper.readValue(responseString, User.class);
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
