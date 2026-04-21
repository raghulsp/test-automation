package com.framework.runners;

import org.testng.annotations.DataProvider;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.framework.hooks","com.framework.steps"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/report.html",
        "json:target/cucumber-reports/report.json"
    },
    tags = "not @wip",
    monochrome = true
)

public class TestRunner extends AbstractTestNGCucumberTests{
    
    @Override
    @DataProvider(parallel = true)
    public Object [][] scenarios(){
        return super.scenarios();
    }

}