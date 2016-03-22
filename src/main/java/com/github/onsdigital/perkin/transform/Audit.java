package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.perkin.json.SurveyParser;
import com.github.onsdigital.perkin.helper.Timer;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class Audit {

    private static final Audit INSTANCE = new Audit();

    private Map<String, AtomicLong> counters;
    private List<String> messages;

    private Audit() {
        counters = new ConcurrentSkipListMap<>();
        messages = new CopyOnWriteArrayList<>();
    }

    public static Audit getInstance() {
        return INSTANCE;
    }

    public void increment(Timer timer) {
        if (timer != null) {
            String message = createMessage(timer);
            increment(timer.getName() + ".count", 1, message);
            increment(timer.getName() + ".duration", timer.getDuration());
            setAverage(timer.getName());
        }
    }

    private void setAverage(String key) {
        long count = counters.get(key + ".count").get();
        long duration = counters.get(key + ".duration").get();
        double average = 0;
        if (duration > 0) {
            average = (double) duration / count;
        }

        long av = (long) average;
        log.debug("***** timer name: {} count: {} duration: {} average: {}", key, count, duration, av);

        AtomicLong averageAl = counters.get(key + ".average");
        if (averageAl == null) {
            counters.put(key + ".average", new AtomicLong(av));
        } else {
            averageAl.set(av);
        }
    }

    public void increment(String key) {
        increment(key, 1);
    }

    public void increment(String key, int size) {
        String message = createMessage(key);
        increment(key, size, message);
    }

    public void increment(Timer timer, DataFile file) {
        String message = createMessage(timer) + getDataFileMessage(file);
        increment(timer.getName() + ".count", 1, message);
        increment(timer.getName() + ".duration", timer.getDuration());
        setAverage(timer.getName());
    }

    protected String getExceptionMessage(Exception e) {
        return " " + e.getClass().getName() + " " + e.getMessage() + getCause(e);
    }

    private String getCause(Exception e) {
        String cause = "";
        if (e.getCause() != null) {
            cause = " caused by " + e.getCause().getClass().getName() + " " + e.getCause().getMessage();
        }

        return cause;
    }

    private String getDataFileMessage(DataFile file) {
        return " " + file.getFilename() + " (" + file.getSize() + " bytes)";
    }

    private void increment(String key, long size) {
        increment(key, size, null);
    }

    private void increment(String key, long size, String message) {
        if (message != null) {
            addMessage(message);
        }

        AtomicLong current = counters.get(key);
        if (current == null) {
            counters.put(key, new AtomicLong(size));
        } else {
            current.getAndAdd(size);
        }
    }

    private void addMessage(String message) {
        log.info("AUDIT|" + message);
        messages.add(message);
    }

    private String createMessage(String key) {
        return new SimpleDateFormat(SurveyParser.ISO8601).format(new Date()) + " " + key;
    }

    private String createMessage(Timer timer) {
        return new SimpleDateFormat(SurveyParser.ISO8601).format(new Date()) + " " + timer;
    }

    public List<String> getMessages() {
        return Lists.reverse(messages);
    }

    public Map<String, String> getCounters() {
        Map<String, String> result = new TreeMap<>();

        for (String key : counters.keySet()) {
            result.put(key, String.valueOf(counters.get(key).longValue()));
        }

        return result;
    }
}
