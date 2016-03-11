package com.github.onsdigital.perkin.transform;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Batch number starts at 30000, increments up to 39999 then back to 30000.
 */
@Slf4j
public class BatchNumberService {

    private static final long START = 30000;
    private static final long END   = 39999;

    private static AtomicLong batchId = new AtomicLong(START);

    //TODO: persist the batch number and continue where we left off

    public long getNext() {
        long batch = batchId.getAndIncrement();
        if (batch > END) {
            batchId.set(START);
            batch = START;
        }

        log.debug("BATCH|next: {}", batch);

        return batch;
    }
}
