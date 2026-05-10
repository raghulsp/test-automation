package com.framework.api.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features/api/smoke",
    glue = {"com.framework.api.hooks", "com.framework.api.steps"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/api-smoke-report.html",
        "json:target/cucumber-reports/api-smoke-report.json"
    },
    tags = "@api-smoke",
    monochrome = true
)
public class ApiSmokeRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
