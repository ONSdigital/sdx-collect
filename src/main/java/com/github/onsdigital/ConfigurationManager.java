package com.github.onsdigital;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

@Slf4j
public class ConfigurationManager {

    private static ConfigurationManager INSTANCE = new ConfigurationManager();

    private Map<String, String> configuration;
    private Map<String, String> safeConfiguration;

    private ConfigurationManager() {
        //use getInstance()

        configuration = new TreeMap<>();
        safeConfiguration = new TreeMap<>();
        loadConfiguration();
    }

    public static ConfigurationManager getInstance() {
        return INSTANCE;
    }

    public void loadConfiguration() {
        InputStream in = getClass().getClassLoader().getResourceAsStream("env.properties");
        if (in != null) {
            Properties properties = new Properties();
            try {
                properties.load(in);
            } catch (IOException e) {
                log.error("CONFIGURATION|problem loading configuration from env.properties", e);
            }

            //check for overrides
            for (String key : properties.stringPropertyNames()) {
                String defaultValue = properties.getProperty(key);
                String value = Configuration.get(key, defaultValue);
                add(key, value);
            }

            log.info("CONFIGURATION|final configuration: {}", safeConfiguration);
        }
    }

    /**
     * Mask the value of any keys containing 'pass' with * characters ignoring case.
     *
     * @param key
     * @param value
     */
    private void add(String key, String value) {
        configuration.put(key, value);
        safeConfiguration.put(key, createSafeValue(key, value));
    }

    public static String get(String key) {
        return getInstance().getValue(key);
    }

    /**
     * Allow setting within configuration manager to facilitate tests
     *
     * @param key
     * @param value
     */
    public static void set(String key, String value) {
        getInstance().add(key, value);
    }

    public static int getInt(String key) {
        int i = -1;
        String value = get(key);
        if (value != null) {
            i = Integer.parseInt(value);
        }
        return i;
    }

    /**
     * @param key
     * @return value masked by * chars if key contains "pass" with case ignored
     */
    public static String getSafe(String key) {
        return getInstance().getSafeValue(key);
    }

    public String getValue(String key) {
        return configuration.get(key);
    }

    public String getSafeValue(String key) {
        return safeConfiguration.get(key);
    }

    public Map<String,String> getSafeValues() {
        return safeConfiguration;
    }

    public static String createSafeValue(String key, String value) {
        String safeValue = value;

        if (key != null && value != null) {
            if (key.toLowerCase().contains("pass")) {
                safeValue = value.replaceAll(".", "*");
            }
        }

        return safeValue;
    }
}
