package com.service.template;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;

import org.jboss.logging.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.pojo.template.SampleRecord;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SampleThreadService {
	
	@Async
	public void processRecords(UUID mdcId, int thread_nno, Deque<SampleRecord> deque) {
		MDC.put("mdcId", mdcId);
		log.info("Process dummy record at thread no. " + thread_nno + " start.");
		try {
			SampleRecord sampleRecord;
			while ((sampleRecord = deque.pollLast()) != null) {
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
			log.info("Process dummy record at thread no. " + thread_nno + " END.");
		}
	}
	
	public Deque<SampleRecord> addDummyRecord(int count) {
		Deque<SampleRecord> result = new ConcurrentLinkedDeque<>();
		double randomDouble = ThreadLocalRandom.current().nextDouble(0.00, 9999999.99);
        for (int i = 1; i <= count; i++) {
        	result.addLast(SampleRecord.builder()
            		.row_no(i)
            		.txn_id(UUID.randomUUID().toString())
            		.txn_amount(new BigDecimal(randomDouble).setScale(2, RoundingMode.HALF_UP))
            		.build());
        }
        return result;
    }
}
