package com.github.onsdigital.perkin.transform;

import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Batch number starts at 30000, increments up to 39999 then back to 30000.
 */
@Slf4j
public class NumberService {

    private final String name;
    private final long start;
    private final long end;

    private AtomicLong number;

    //TODO: persist the sequence number and continue where we left off - shutdown hook?

    public NumberService(String name, long start, long end) {
        this.name = name;
        this.start = start;
        this.end = end;

        //TODO: see if file exists for initial value
        number = new AtomicLong(start);
    }

    public long getNext() {
        long next = number.getAndIncrement();
        if (next > end) {
            number.set(start);
            next = start;
        }

        log.debug("SEQUENCE|next '{}': {}", name, next);

        return next;
    }

    public void save() {
        try {
            FileOutputStream out = new FileOutputStream("./" + name + ".sequence");

            Properties properties = new Properties();
            properties.put(name, "" + number.get());

            properties.store(out, "saved on shutdown");
            out.close();
        } catch (IOException e) {
            log.error("SEQUENCE|problem saving sequence: {} value: {}", name, number.get());
        }
    }
}
