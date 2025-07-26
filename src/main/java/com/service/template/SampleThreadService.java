package com.service.template;

import java.util.UUID;

import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.pojo.template.SampleRecord;
import com.pojo.template.SampleSharedStack;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SampleThreadService {

	@Autowired
    private SampleSharedStack sampleSharedStack;
	
	@Async
	public void processRecords(UUID mdcId, String thread_name) {
		MDC.put("mdcId", mdcId);
		log.info(thread_name.concat(" start."));
		try {
			SampleRecord sampleRecord;
			while ((sampleRecord = sampleSharedStack.pollRecord()) != null) {
				log.info("Row no: " + sampleRecord.getRow_no());
				log.info("Txn id: " + sampleRecord.getTxn_id());
				log.info("Txn amount: " + sampleRecord.getTxn_amount());
				
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
			log.info(thread_name.concat(" end."));
		}
	}
}
