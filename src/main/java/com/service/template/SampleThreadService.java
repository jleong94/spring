package com.service.template;
import com.utilities.LogUtil;

import java.math.BigDecimal;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;

import org.jboss.logging.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.github.javafaker.Faker;
import com.pojo.template.Pojo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SampleThreadService {

	private static final Faker faker = new Faker();

	@Async
	public void processRecords(UUID xRequestId, int thread_no, Deque<Pojo> deque) {
		MDC.put("X-Request-ID", xRequestId);
		log.info("Process dummy record at thread no. " + thread_no + " start.");
		try {
			Pojo pojo;
			while ((pojo = deque.pollLast()) != null) {
				log.info("ID: " + pojo.getId());
				log.info("name: " + pojo.getName());
				log.info("Acc balance: " + pojo.getAccount_balance());

			}
		} catch (Throwable e) {
			LogUtil.logError(log, e);
		} finally {
			log.info("Process dummy record at thread no. " + thread_no + " END.");
		}
	}

	public Deque<Pojo> addDummyRecord(int count) {
		Deque<Pojo> result = new ConcurrentLinkedDeque<>();
		for (int i = 1; i <= count; i++) {
			result.addLast(Pojo.builder()
					.id(i)
					.name(faker.name().fullName())
					.account_balance(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0, 999)))
					.build());
		}
		return result;
	}
}
