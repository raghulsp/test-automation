package com.framework.base;

import com.framework.drivers.DeviceManager;
import com.framework.drivers.DriverManager;
import com.framework.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class BasePage {

    protected WebElement find(By locator) {
        return WaitUtils.waitForVisible(locator);
    }

    protected void click(By locator) {
        WaitUtils.waitForClickable(locator).click();
    }

    protected void type(By locator, String text) {
        find(locator).clear();
        find(locator).sendKeys(text);
    }

    protected String getText(By locator) {
        return find(locator).getText();
    }

    protected void open(String url) {
        DriverManager.getDriver().get(url);
    }

    protected String currentUrl() {
        return DriverManager.getDriver().getCurrentUrl();
    }

    protected String platform() {
        return DeviceManager.currentDevice().getPlatform();
    }
}
