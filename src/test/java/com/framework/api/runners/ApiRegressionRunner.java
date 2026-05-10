package com.framework.api.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features/api/regression",
    glue = {"com.framework.api.hooks", "com.framework.api.steps"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/api-regression-report.html",
        "json:target/cucumber-reports/api-regression-report.json"
    },
    tags = "@api-regression",
    monochrome = true
)
public class ApiRegressionRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
