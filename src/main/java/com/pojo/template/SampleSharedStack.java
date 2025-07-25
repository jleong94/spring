package com.pojo.template;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class SampleSharedStack {

	private Deque<SampleRecord> stack = new ConcurrentLinkedDeque<>();
	
	public void addDummyRecord(int count) {
		double randomDouble = ThreadLocalRandom.current().nextDouble(0.00, 9999999.99);
        for (int i = 1; i <= count; i++) {
            stack.addLast(SampleRecord.builder()
            		.row_no(i)
            		.txn_id(UUID.randomUUID().toString())
            		.txn_amount(new BigDecimal(randomDouble).setScale(2, RoundingMode.HALF_UP))
            		.build());
        }
    }
	
	public SampleRecord pollRecord() {
        return stack.pollLast(); // null-safe
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
