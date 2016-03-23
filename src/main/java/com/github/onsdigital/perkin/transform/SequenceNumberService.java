package com.github.onsdigital.perkin.transform;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Batch number starts at 30000, increments up to 39999 then back to 30000.
 */
@Slf4j
public class SequenceNumberService {

    private static final long START = 1000;
    private static final long END   = 99999;

    private static AtomicLong sequence = new AtomicLong(START);

    //TODO: persist the sequence number and continue where we left off

    public long getNext() {
        long next = sequence.getAndIncrement();
        if (next > END) {
            sequence.set(START);
            next = START;
        }

        log.debug("SEQUENCE|next: {}", next);

        return next;
    }
}
