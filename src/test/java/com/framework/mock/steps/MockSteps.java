package com.framework.mock.steps;

import com.framework.api.mock.StubFactory;
import io.cucumber.java.en.Given;

/**
 * Step definitions for setting up WireMock stubs.
 * These are "Given" steps that run before the "When" steps from the real service step files.
 * The mock runner includes both this package and com.framework.api.steps in its glue.
 */
public class MockSteps {

    @Given("the GET {string} endpoint is stubbed to return {int} with body:")
    public void stubGet(String url, int status, String body) {
        StubFactory.stubGet(url, status, body);
    }

    @Given("the POST {string} endpoint is stubbed to return {int} with body:")
    public void stubPost(String url, int status, String body) {
        StubFactory.stubPost(url, status, body);
    }

    @Given("the PUT {string} endpoint is stubbed to return {int} with body:")
    public void stubPut(String url, int status, String body) {
        StubFactory.stubPut(url, status, body);
    }

    @Given("the PATCH {string} endpoint is stubbed to return {int} with body:")
    public void stubPatch(String url, int status, String body) {
        StubFactory.stubPatch(url, status, body);
    }

    @Given("the DELETE {string} endpoint is stubbed to return {int}")
    public void stubDelete(String url, int status) {
        StubFactory.stubDelete(url, status);
    }

    @Given("the {string} endpoint is stubbed to return a 500 server error")
    public void stubServerError(String url) {
        StubFactory.stubServerError(url);
    }

    @Given("the {string} endpoint is stubbed to return a 401 unauthorized")
    public void stubUnauthorized(String url) {
        StubFactory.stubUnauthorized(url);
    }

    @Given("the {string} endpoint is stubbed to respond after {int}ms delay")
    public void stubTimeout(String url, int delayMs) {
        StubFactory.stubTimeout(url, delayMs);
    }

    @Given("the POST {string} endpoint containing {string} in request body is stubbed to return {int} with body:")
    public void stubPostWithBody(String url, String requestFragment, int status, String responseBody) {
        StubFactory.stubPostWithRequestBody(url, requestFragment, status, responseBody);
    }
}
