package com.pojo.bucket4j;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class CustomBucket {

    private final AtomicReference<Bucket> bucket = new AtomicReference<>();

    public CustomBucket(Bandwidth bandwidth) {
        this.bucket.set(Bucket.builder().addLimit(bandwidth).build());
    }

    public boolean tryConsume(long tokens) {
        return this.bucket.get().tryConsume(tokens);
    }

    /**
     * Tries to consume {@code tokens}, parking the calling thread for up to
     * {@code maxWait} for the bucket to refill before giving up. This is the
     * "auto retry" path: a burst that momentarily exhausts the bucket waits for
     * a token instead of being rejected outright. Returns {@code false} only if
     * a token still isn't available within {@code maxWait}.
     */
    public boolean tryConsume(long tokens, Duration maxWait) throws InterruptedException {
        return this.bucket.get().asBlocking().tryConsume(tokens, maxWait);
    }

    public long getAvailableTokens() {
        return this.bucket.get().getAvailableTokens();
    }

    // Swap bucket atomically when limits change
    public void updateBandwidth(Bandwidth bandwidth) {
        this.bucket.set(Bucket.builder().addLimit(bandwidth).build());
    }
}
