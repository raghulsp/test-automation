package com.framework.hooks;

import com.framework.context.ScenarioContext;
import com.framework.drivers.DeviceManager;
import com.framework.drivers.DriverFactory;
import com.framework.drivers.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hooks {

    private static final Logger log = LoggerFactory.getLogger(Hooks.class);

    /**
     * Scenarios tagged {@code @api} are pure REST API tests — no browser or
     * Appium session is needed.  All other scenarios get a driver as usual.
     */
    @Before
    public void setUp(Scenario scenario) {
        log.info("Starting scenario: {}", scenario.getName());
        if (!isApiOnly(scenario)) {
            DriverManager.setDriver(DriverFactory.createDriver());
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed() && !isApiOnly(scenario)) {
            try {
                byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver())
                        .getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failure Screenshot");
            } catch (Exception e) {
                log.warn("Could not capture screenshot: {}", e.getMessage());
            }
        }

        log.info("Finished scenario: {} - {}", scenario.getName(), scenario.getStatus());

        if (!isApiOnly(scenario)) {
            DriverManager.quitDriver();
            DeviceManager.releaseDevice();
        }

        // Always clear the scenario-scoped context regardless of test type
        ScenarioContext.clear();
    }

    private boolean isApiOnly(Scenario scenario) {
        return scenario.getSourceTagNames().contains("@api");
    }
}
