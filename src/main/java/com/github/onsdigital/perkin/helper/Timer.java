package com.github.onsdigital.perkin.helper;

import lombok.Getter;

import java.util.Date;

public class Timer {

    @Getter
    private String name;
    private String info;
    private long start;
    private long stop;
    @Getter
    private long duration;
    private Throwable t;

    public Timer(String name) {
        this.name = name;
        start = new Date().getTime();
    }

    public void stopStatus(int status) {
        stop();
        name += status;
    }

    public void stopStatus(int status, Throwable t) {
        stop();
        name += status;
        this.t = t;
    }

    private void stop() {
        stop = new Date().getTime();
        duration = stop - start;
    }

    public void addInfo(String info) {
        if (info == null) {
            this.info = info;
        } else {
            this.info += " " + info;
        }
    }

    private String getInfo() {
        if (info == null) {
            return "";
        } else {
            return " " + info;
        }
    }

    private String getThrowable() {
        if (t == null) {
            return "";
        } else {
            return " " + t.getClass().getName() + " " + t.getMessage() + getCause();
        }
    }

    private String getCause() {
        String cause = "";
        if (t.getCause() != null) {
            cause = " caused by " + t.getCause().getClass().getName() + " " + t.getCause().getMessage();
        }

        return cause;
    }

    @Override
    public String toString() {
        return duration + "ms " + name + getInfo() + getThrowable();
    }
}
