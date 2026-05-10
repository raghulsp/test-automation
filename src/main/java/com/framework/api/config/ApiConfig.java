package com.framework.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApiConfig {

    private static final Properties props = new Properties();
    private static final Environment environment;

    static {
        environment = Environment.fromString(System.getProperty("api.env", "qa"));
        String file = "api/environments/" + environment.name().toLowerCase() + ".properties";
        try (InputStream is = ApiConfig.class.getClassLoader().getResourceAsStream(file)) {
            if (is == null) throw new RuntimeException("API environment config not found: " + file);
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load API config: " + file, e);
        }
    }

    public static String get(String key) {
        return System.getProperty(key, props.getProperty(key, ""));
    }

    public static String get(String key, String defaultValue) {
        return System.getProperty(key, props.getProperty(key, defaultValue));
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Environment getEnvironment() {
        return environment;
    }

    // Shortcut: reads api.base.url.<service> from the active env properties
    public static String getBaseUrl(String service) {
        return get("api.base.url." + service.toLowerCase());
    }
}
