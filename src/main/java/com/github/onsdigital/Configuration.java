package com.github.onsdigital;

import org.apache.commons.lang3.StringUtils;

/**
 * Convenience class to get configuration values from {@link System#getProperty(String)} or gracefully fall back to {@link System#getenv()}.
 */
public abstract class Configuration {

    /**
     * Gets a configuration value from {@link System#getProperty(String)}, falling back to {@link System#getenv()}
     * if the property comes back blank.
     *
     * @param key The configuration value key.
     * @return A system property or, if that comes back blank, an environment value.
     */
    public static String get(String key) {
        String value = StringUtils.defaultIfBlank(System.getProperty(key), System.getenv(key));
        System.out.println("configuration " + key + " value: " + value);
        return value;
    }

    public static int getInt(String key, int defaultValue) {
        int i = defaultValue;
        String value = System.getProperty(key);
        if (value != null) {
            i = Integer.parseInt(value);
        }
        System.out.println("configuration " + key + " default: " + defaultValue + " value: " + i);
        return i;
    }

    /**
     * Gets a configuration value from {@link System#getProperty(String)}, falling back to {@link System#getenv()}
     * if the property comes back blank, then falling back to the default value.
     *
     * @param key          The configuration value key.
     * @param defaultValue The default to use if neither a property nor an environment value are present.
     * @return The result of {@link #get(String)}, or <code>defaultValue</code> if that result is blank.
     */
    public static String get(String key, String defaultValue) {
        String value = StringUtils.defaultIfBlank(get(key), defaultValue);
        System.out.println("configuration " + key + " default: " + defaultValue + " value: " + value);
        return value;
    }

    public static String set(String key, String value) {
        return System.setProperty(key, value);
    }

    public static String set(String key, int value) {
        return System.setProperty(key, "" + value);
    }
}
