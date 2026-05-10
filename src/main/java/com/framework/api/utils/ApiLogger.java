package com.framework.api.utils;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiLogger {

    private static final Logger log = LoggerFactory.getLogger(ApiLogger.class);

    public static void logResponse(Response response) {
        log.info("<<< Response | Status: {} | Time: {}ms\n{}",
            response.getStatusCode(), response.getTime(), response.getBody().asPrettyString());
    }

    public static void logRequest(String method, String url, Object body) {
        log.info(">>> {} {} | Body: {}", method.toUpperCase(), url, body);
    }

    public static void logStep(String message) {
        log.info("[API] {}", message);
    }
}
