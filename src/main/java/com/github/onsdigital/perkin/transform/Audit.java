package com.github.onsdigital.perkin.transform;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class Audit {

    private Map<String, AtomicLong> counters;
    private List<String> messages;

    public Audit() {
        counters = new HashMap<>();
        messages = new ArrayList<>();
    }

    public void increment(String key) {
        String message = LocalDate.now() + " " + key;
        addMessage(message);

        AtomicLong current = counters.get(key);
        if (current == null) {
            counters.put(key, new AtomicLong(1));
        } else {
            current.getAndIncrement();
        }
    }

    private void addMessage(String message) {
        log.info("AUDIT|" + message);
        messages.add(message);
    }
}
