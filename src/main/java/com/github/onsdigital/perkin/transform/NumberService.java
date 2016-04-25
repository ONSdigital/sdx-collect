package com.github.onsdigital.perkin.transform;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
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

        number = new AtomicLong(start);
        load(start, end);
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
        try (FileOutputStream out = new FileOutputStream("./" + name + ".sequence")) {

            Properties properties = new Properties();
            properties.put(name, "" + number.get());

            properties.store(out, "saved on shutdown");
        } catch (IOException e) {
            log.error("SEQUENCE|problem saving sequence: {} value: {}", name, number.get());
        }
    }

    /**
     * Gracefully loads a saved sequence value, ensuring it is in the start/end range.
     *
     * @param start The minimum value.
     * @param end   The maximum value.
     */
    public void load(long start, long end) {
        String value = null;
        File file = new File("./" + name + ".sequence");
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {

                Properties properties = new Properties();
                properties.load(in);
                value = properties.getProperty(name, String.valueOf(number.get()));
                long sequence = Math.max(Long.parseLong(value), start);
                sequence = Math.min(sequence, end);
                number.set(sequence);

            } catch (IOException e) {
                log.error("SEQUENCE|problem loading sequence: {} value: {}", name, number.get());
            } catch (NumberFormatException e) {
                log.error("SEQUENCE|problem loading sequence: {} value could not be parsed as a number: {}", name, value);
            }
        }
    }
}
