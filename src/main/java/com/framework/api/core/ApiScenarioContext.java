package com.framework.api.core;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * PicoContainer-injected shared context for a single Cucumber scenario.
 * Passed via constructor injection to all API step definition classes that need it.
 */
public class ApiScenarioContext {

    private Response lastResponse;
    private final Map<String, Object> store = new HashMap<>();

    public void setResponse(Response response) {
        this.lastResponse = response;
    }

    public Response getResponse() {
        return lastResponse;
    }

    public void store(String key, Object value) {
        store.put(key, value);
    }

    public Object retrieve(String key) {
        return store.get(key);
    }

    public String retrieveAsString(String key) {
        return String.valueOf(store.get(key));
    }

    public int retrieveAsInt(String key) {
        return Integer.parseInt(retrieveAsString(key));
    }
}
