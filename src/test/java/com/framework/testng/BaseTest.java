package com.framework.testng;

import com.framework.drivers.DeviceManager;
import com.framework.drivers.DriverFactory;
import com.framework.drivers.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * TestNG lifecycle order (outermost → innermost):
 *   @BeforeSuite  → @BeforeClass → @BeforeMethod → @Test → @AfterMethod → @AfterClass → @AfterSuite
 *
 * alwaysRun=true ensures teardown still runs when a test or setup step fails.
 */
public class BaseTest {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    protected WebDriver driver;

    // Runs ONCE before the very first test in the entire suite
    @BeforeSuite(alwaysRun = true)
    public void suiteSetUp() {
        log.info("=== Suite started ===");
    }

    // Runs ONCE after the very last test in the suite
    @AfterSuite(alwaysRun = true)
    public void suiteTearDown() {
        log.info("=== Suite finished ===");
    }

    // Runs ONCE before the first @Test in this class
    @BeforeClass(alwaysRun = true)
    public void classSetUp() {
        log.info(">> Class setup: {}", getClass().getSimpleName());
    }

    // Runs ONCE after the last @Test in this class
    @AfterClass(alwaysRun = true)
    public void classTearDown() {
        log.info(">> Class teardown: {}", getClass().getSimpleName());
    }

    // Runs before EACH @Test method — creates a fresh browser session
    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.createDriver();
        DriverManager.setDriver(driver);
    }

    // Runs after EACH @Test method — ITestResult carries pass/fail/skip status
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            takeScreenshot(result.getName());
        }
        DriverManager.quitDriver();
        DeviceManager.releaseDevice();
    }

    private void takeScreenshot(String testName) {
        try {
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            String dir = "target/screenshots/";
            Files.createDirectories(Paths.get(dir));
            Files.write(Paths.get(dir + testName + "_FAILED.png"), bytes);
            log.info("Screenshot saved: {}{}_FAILED.png", dir, testName);
        } catch (IOException e) {
            log.warn("Could not save screenshot for {}: {}", testName, e.getMessage());
        }
    }
}
