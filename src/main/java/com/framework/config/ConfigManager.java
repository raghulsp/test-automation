package com.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigManager {

    private static final ConfigManager INSTANCE = new ConfigManager();

    private final Properties properties = new Properties();

    private ConfigManager() {
        try (InputStream is = ConfigManager.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public static String get(String key) {
        return getInstance().getValue(key, "");
    }

    public static String get(String key, String defaultValue) {
        return getInstance().getValue(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        return val.isEmpty() ? defaultValue : Boolean.parseBoolean(val);
    }

    private String getValue(String key, String defaultValue) {
        return System.getProperty(key, properties.getProperty(key, defaultValue));
    }
}
