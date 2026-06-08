package com.framework.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DbConfig {

    private static final Properties PROPS = load();

    private DbConfig() {}

    private static Properties load() {
        Properties p = new Properties();
        String env = System.getProperty("db.env", "default");
        String path = "default".equals(env) ? "db.properties" : "db/" + env + ".properties";
        try (InputStream is = DbConfig.class.getClassLoader().getResourceAsStream(path)) {
            if (is != null) p.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + path, e);
        }
        return p;
    }

    public static String get(String key) {
        return System.getProperty(key, PROPS.getProperty(key, ""));
    }

    public static String get(String key, String defaultValue) {
        return System.getProperty(key, PROPS.getProperty(key, defaultValue));
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
