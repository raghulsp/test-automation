package com.framework.runners;

import org.testng.annotations.DataProvider;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.framework.hooks", "com.framework.steps"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/smoke-report.html",
        "json:target/cucumber-reports/smoke-report.json"
    },
    tags = "@smoke",
    monochrome = true
)
public class SmokeTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
