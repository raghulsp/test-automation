package com.framework.utils;

import com.framework.drivers.DeviceManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;

import com.framework.drivers.DriverManager;

public class TapUtils {

    // -------------------------------------------------------------------------
    // Cross-platform
    // -------------------------------------------------------------------------

    public static void tapByXpath(String xpath) {
        tap(By.xpath(xpath));
    }

    public static void tapById(String resourceId) {
        tap(By.id(resourceId));
    }

    public static void tapByAccessibilityId(String id) {
        tap(AppiumBy.accessibilityId(id));
    }

    // -------------------------------------------------------------------------
    // Android — UiSelector
    // -------------------------------------------------------------------------

    public static void tapByAndroidUiText(String text) {
        requireAndroid();
        tap(AppiumBy.androidUIAutomator(
                "new UiSelector().text(\"%s\")".formatted(text)));
    }

    public static void tapByAndroidUiTextContains(String text) {
        requireAndroid();
        tap(AppiumBy.androidUIAutomator(
                "new UiSelector().textContains(\"%s\")".formatted(text)));
    }

    public static void tapByAndroidUiResourceId(String resourceId) {
        requireAndroid();
        tap(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceId(\"%s\")".formatted(resourceId)));
    }

    // -------------------------------------------------------------------------
    // iOS — predicate string / class chain
    // -------------------------------------------------------------------------

    public static void tapByIosPredicateString(String predicate) {
        requireIOS();
        tap(AppiumBy.iOSNsPredicateString(predicate));
    }

    public static void tapByIosClassChain(String chain) {
        requireIOS();
        tap(AppiumBy.iOSClassChain(chain));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void tap(By locator) {
        WaitUtils.waitForClickable(locator).click();
    }

    private static void requireAndroid() {
        if (!DeviceManager.currentDevice().getPlatform().equals("android")) {
            throw new UnsupportedOperationException("Android tap methods are Android-only");
        }
    }

    private static void requireIOS() {
        if (!DeviceManager.currentDevice().getPlatform().equals("ios")) {
            throw new UnsupportedOperationException("iOS tap methods are iOS-only");
        }
    }
}
