package com.framework.api.utils;

import io.restassured.response.Response;
import org.testng.Assert;

public class ResponseValidator {

    public static void assertStatusCode(Response response, int expected) {
        int actual = response.getStatusCode();
        Assert.assertEquals(actual, expected,
            "Status mismatch — expected " + expected + " but got " + actual + ". Body: " + response.getBody().asString());
    }

    public static void assertFieldNotNull(Response response, String jsonPath) {
        Object actual = response.jsonPath().get(jsonPath);
        Assert.assertNotNull(actual, "Field '" + jsonPath + "' should be present but was null");
    }

    public static void assertFieldEquals(Response response, String jsonPath, Object expected) {
        Object actual = response.jsonPath().get(jsonPath);
        Assert.assertEquals(actual, expected,
            "Field '" + jsonPath + "' expected '" + expected + "' but was '" + actual + "'");
    }

    public static void assertFieldContains(Response response, String jsonPath, String substring) {
        String actual = response.jsonPath().getString(jsonPath);
        Assert.assertNotNull(actual, "Field '" + jsonPath + "' is null");
        Assert.assertTrue(actual.contains(substring),
            "Field '" + jsonPath + "' expected to contain '" + substring + "' but was '" + actual + "'");
    }

    public static void assertBodyContains(Response response, String text) {
        String body = response.getBody().asString();
        Assert.assertTrue(body.contains(text),
            "Response body does not contain: '" + text + "'");
    }

    public static void assertResponseTimeBelow(Response response, long maxMs) {
        long actual = response.getTime();
        Assert.assertTrue(actual < maxMs,
            "Response time " + actual + "ms exceeded threshold of " + maxMs + "ms");
    }

    public static void assertListNotEmpty(Response response) {
        java.util.List<?> list = response.jsonPath().getList("$");
        Assert.assertNotNull(list, "Response list is null");
        Assert.assertFalse(list.isEmpty(), "Response list should not be empty");
    }
}
