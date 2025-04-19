package com.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Connection;

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
import org.json.JSONObject;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.impl.SimpleTraceGenerator;

public class JavaAPICall {

	public String httpClientApi(Connection connDB, Logger log, String logFolder) {
		String result = "";
		String URL = "";
		JSONObject requestJson = null, responseJson = null;
		boolean wasNull = true;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		try {
			if(connDB == null) {
				//connDB = ; wasNull = false;
			}           

			log.info("URL: " + URL);
			if(requestJson != null) {log.info("Request: " + requestJson.toString());}
			if(!URL.isEmpty()){
				/*List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("", ));*/
				RequestConfig requestConfig = RequestConfig.custom()
		                .setConnectTimeout(5 * 1000)//in miliseconds
		                .setSocketTimeout(5 * 1000)//in miliseconds
		                .setConnectionRequestTimeout(5 * 1000)//in miliseconds
		                .build();
				CloseableHttpClient httpClient = HttpClients.custom()
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
				httpRequest.setEntity(new StringEntity(objectMapper.writeValueAsString(requestJson)));
				httpRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
				for(Header header : httpRequest.getAllHeaders()) {
					log.info(header.getName() + "(Request): " + header.getValue());
				}
				CloseableHttpResponse httpResponse = httpClient.execute(httpRequest);
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
//						objectMapper.readerForUpdating(Object.class).readValue(responseString);
						responseJson = new JSONObject(responseString);
					}
					httpClient.close(); httpResponse.close();
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
				if(!wasNull && connDB != null){connDB.close(); connDB = null;}
			}catch(Exception e) {}
		}
		return result;
	}
	
	public String iso8583Api(Connection connDB, Logger log, String logFolder) {
		String result = "";
		String ip = "";
		int port = 0;
		boolean wasNull = true;
		Socket socket;
		OutputStream outputStream;
		InputStream inputStream;
		try {
			if(connDB == null) {
				//connDB = ; wasNull = false;
			}           
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
				if(!wasNull && connDB != null){connDB.close(); connDB = null;}
			}catch(Exception e) {}
		}
		return result;
	}
}
