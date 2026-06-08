package com.framework.mock.hooks;

import com.framework.api.mock.MockServerManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockApiHooks {

    private static final Logger log = LoggerFactory.getLogger(MockApiHooks.class);

    @Before(value = "@mock", order = 0)
    public void startMockServer(Scenario scenario) {
        MockServerManager.start();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        log.info("[Mock] Starting: '{}'", scenario.getName());
    }

    @After(value = "@mock", order = 0)
    public void resetMockServer(Scenario scenario) {
        log.info("[Mock] Finished: '{}' | Status: {}", scenario.getName(), scenario.getStatus());
        MockServerManager.reset();
    }
}
