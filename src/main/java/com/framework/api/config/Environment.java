package com.framework.api.config;

public enum Environment {
    DEV, QA, STAGING, PROD, MOCK;

    public static Environment fromString(String value) {
        for (Environment env : values()) {
            if (env.name().equalsIgnoreCase(value)) return env;
        }
        return QA;
    }
}
