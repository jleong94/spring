package com.pojo.bucket4j;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;

import java.util.concurrent.atomic.AtomicReference;

public class CustomBucket {

	private final AtomicReference<Bucket> bucket = new AtomicReference<>();
	
	public CustomBucket(Bandwidth bandwidth) {
        this.bucket.set(Bucket.builder().addLimit(bandwidth).build());
    }
	
	public boolean tryConsume(long tokens) {
        return this.bucket.get().tryConsume(tokens);
    }

    public long getAvailableTokens() {
        return this.bucket.get().getAvailableTokens();
    }

    // Swap bucket atomically when limits change
    public void updateBandwidth(Bandwidth bandwidth) {
    	this.bucket.set(Bucket.builder().addLimit(bandwidth).build());
    }
}
