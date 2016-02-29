package com.github.onsdigital.perkin.helpers;

import org.apache.commons.lang3.StringUtils;

/**
 * Convenience class to get configuration values from {@link System#getProperty(String)} or gracefully fall back to {@link System#getenv()}.
 */
public class Configuration {

    public static final String FTP_HOST = "ftp.host";
    public static final String FTP_PORT = "ftp.port";
    public static final String FTP_USER = "ftp.user";
    public static final String FTP_PASSWORD = "ftp.password";
    public static final String FTP_PATH = "ftp.path";

    /**
     * Gets a configuration value from {@link System#getProperty(String)}, falling back to {@link System#getenv()}
     * if the property comes back blank.
     *
     * @param key The configuration value key.
     * @return A system property or, if that comes back blank, an environment value.
     */
    public static String get(String key) {
        return StringUtils.defaultIfBlank(System.getProperty(key), System.getenv(key));
    }

    public static int getInt(String key, int defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
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
        return StringUtils.defaultIfBlank(get(key), defaultValue);
    }

    public static String set(String key, String value) {
        return System.setProperty(key, value);
    }

    public static String set(String key, int value) {
        return System.setProperty(key, "" + value);
    }
}
