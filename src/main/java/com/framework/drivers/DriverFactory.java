package com.framework.drivers;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;

import org.openqa.selenium.WebDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.framework.config.ConfigManager;

import io.github.bonigarcia.wdm.WebDriverManager;

public class DriverFactory {

    public static WebDriver createDriver() {
        Device device = DeviceManager.acquireDevice();

        return switch (device.getPlatform()) {
            case "android" -> createAndroidDriver(device);
            case "ios"     -> createIOSDriver(device);
            default        -> createWebDriver(device);
        };
    }

    private static WebDriver createWebDriver(Device device) {
        boolean headless = ConfigManager.getBoolean("headless", false);

        WebDriver driver = switch (device.getBrowser()) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions opts = new FirefoxOptions();
                if (headless) opts.addArguments("--headless");
                yield new FirefoxDriver(opts);
            }
            default -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                if (headless) opts.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
                yield new ChromeDriver(opts);
            }
        };

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getInt("implicit.wait", 10)));
        return driver;
    }


    private static WebDriver createAndroidDriver(Device device) {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setUdid(device.getUdid());
        options.setPlatformVersion(device.getPlatformVersion());
        options.setAppPackage(ConfigManager.get("app.package"));
        options.setAppActivity(ConfigManager.get("app.activity"));
        options.setNoReset(ConfigManager.getBoolean("no.reset", true));

        try {
            return new AndroidDriver(URI.create(device.getAppiumUrl()).toURL(), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium URL for device: " + device, e);
        }
    }

    private static WebDriver createIOSDriver(Device device) {
        XCUITestOptions options = new XCUITestOptions();
        options.setUdid(device.getUdid());
        options.setPlatformVersion(device.getPlatformVersion());
        options.setBundleId(ConfigManager.get("bundle.id"));
        options.setNoReset(ConfigManager.getBoolean("no.reset", false));

        try {
            return new IOSDriver(URI.create(device.getAppiumUrl()).toURL(), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium URL for device: " + device, e);
        }
    }




    
}
