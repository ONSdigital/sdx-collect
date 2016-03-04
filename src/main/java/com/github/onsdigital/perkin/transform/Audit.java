package com.github.onsdigital.perkin.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Audit {

    private Map<String, AtomicLong> counters;

    public Audit() {
        counters = new HashMap<>();
    }

    public void increment(String key) {
        AtomicLong current = counters.get(key);
        if (current == null) {
            counters.put(key, new AtomicLong(1));
        } else {
            current.getAndIncrement();
            //counters.put(key, current);
        }
    }

    public Map<String, AtomicLong> getInfo() {
        return counters;
    }
}
