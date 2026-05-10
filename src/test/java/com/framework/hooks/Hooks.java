package com.framework.hooks;


import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.framework.drivers.DeviceManager;
import com.framework.drivers.DriverFactory;
import com.framework.drivers.DriverManager;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.testng.SkipException;

public class Hooks {

    private static final Logger log = LoggerFactory.getLogger(Hooks.class);

    @Before
    public void setUp(Scenario scenario){
        log.info("Starting scenario: {}", scenario.getName());
        DriverManager.setDriver(DriverFactory.createDriver());
    } 

    @After
    public void tearDown(Scenario scenario){
        if(scenario.isFailed()){
            try{
                byte[] screenshot = ((TakesScreenshot)DriverManager.getDriver())
                                            .getScreenshotAs(OutputType.BYTES);
                                            scenario.attach(screenshot, "image/png", "Failure Screenshot");
            } catch(Exception e){
                log.warn("Could not capture screenshot: {}", e.getMessage());
            }
        }

        log.info("Finished scenario: {} - {}", scenario.getName(), scenario.getStatus());
        DriverManager.quitDriver();
        DeviceManager.releaseDevice();
    }

    @Before(value = "@mobile-only", order = 999)
    public void skipIfWeb() {
        if (DeviceManager.currentDevice().getPlatform().equals("web")) {
            throw new SkipException("Skipped: scenario is mobile-only");
        }
    }
}
