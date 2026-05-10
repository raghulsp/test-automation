package com.framework.utils;

import com.framework.drivers.DeviceManager;
import com.framework.drivers.DriverManager;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class NotificationUtils {

    private static final int NOTIFICATION_TIMEOUT = 5;

    public static void openNotificationTray() {
        requireAndroid();
        ((AndroidDriver) DriverManager.getDriver()).openNotifications();
    }

    public static void tapNotificationByText(String text) {
        openNotificationTray();
        By locator = By.xpath("//*[@text='" + text + "']");
        WaitUtils.waitForClickable(locator, NOTIFICATION_TIMEOUT).click();
    }

    public static void tapNotificationByTextContains(String text) {
        openNotificationTray();
        By locator = By.xpath("//*[contains(@text, '" + text + "')]");
        WaitUtils.waitForClickable(locator, NOTIFICATION_TIMEOUT).click();
    }

    private static void requireAndroid() {
        if (!DeviceManager.currentDevice().getPlatform().equals("android")) {
            throw new UnsupportedOperationException("Notification tray methods are Android-only");
        }
    }

    public static void tapNotificationByUiSelector(String text) {
        openNotificationTray();
        // By locator = AppiumBy.androidUIAutomator("new UiSelector().text(\"" +text+  "\")");
        By locator = AppiumBy.androidUIAutomator("""
                new UiSelector().text("%s")
                """.formatted(text));

        WaitUtils.waitForClickable(locator, NOTIFICATION_TIMEOUT).click();
    }

}
