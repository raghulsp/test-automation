package com.framework.api.hooks;

import com.framework.api.config.ApiConfig;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiHooks {

    private static final Logger log = LoggerFactory.getLogger(ApiHooks.class);

    @Before("@api-smoke or @api-regression")
    public void beforeApiScenario(Scenario scenario) {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        log.info("[API] Starting: '{}' | Environment: {}", scenario.getName(), ApiConfig.getEnvironment());
    }

    @After("@api-smoke or @api-regression")
    public void afterApiScenario(Scenario scenario) {
        log.info("[API] Finished: '{}' | Status: {}", scenario.getName(), scenario.getStatus());
    }
}
