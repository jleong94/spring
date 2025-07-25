package com.service.template;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.pojo.template.SampleRecord;
import com.pojo.template.SampleSharedStack;

@Service
public class SampleThreadService {

	@Autowired
    private SampleSharedStack sampleSharedStack;
	
	@Async
	public void processRecords(Logger log, String thread_name) {
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
