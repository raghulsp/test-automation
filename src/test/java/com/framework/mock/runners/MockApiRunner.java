package com.framework.mock.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features/api/mock",
    glue = {
        "com.framework.mock.hooks",
        "com.framework.mock.steps",
        "com.framework.api.steps"       // reuse When/Then steps from real service step files
    },
    plugin = {
        "pretty",
        "html:target/cucumber-reports/api-mock-report.html",
        "json:target/cucumber-reports/api-mock-report.json"
    },
    tags = "@mock",
    monochrome = true
)
public class MockApiRunner extends AbstractTestNGCucumberTests {

    // data-provider-thread-count=1 — WireMock stub registry is shared;
    // parallel mock scenarios would interfere with each other's stubs.
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
