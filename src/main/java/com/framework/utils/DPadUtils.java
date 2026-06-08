package com.framework.utils;

import com.framework.drivers.DeviceManager;
import com.framework.drivers.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class DPadUtils {

    private static final int MAX_NAVIGATIONS = 20;

    public static void up()     { press(AndroidKey.DPAD_UP); }
    public static void down()   { press(AndroidKey.DPAD_DOWN); }
    public static void left()   { press(AndroidKey.DPAD_LEFT); }
    public static void right()  { press(AndroidKey.DPAD_RIGHT); }
    public static void select() { press(AndroidKey.DPAD_CENTER); }

    public static void press(AndroidKey key) {
        requireAndroidTV();
        ((AndroidDriver) DriverManager.getDriver()).pressKey(new KeyEvent(key));
    }

    /**
     * Moves focus downward until the element matching the locator is focused,
     * then presses DPAD_CENTER to select/activate it.
     */
    public static void navigateDownTo(By locator) {
        requireAndroidTV();
        for (int i = 0; i < MAX_NAVIGATIONS; i++) {
            if (isFocused(locator)) {
                select();
                return;
            }
            down();
        }
        throw new RuntimeException(
                "Element not reachable after " + MAX_NAVIGATIONS + " D-pad down presses: " + locator);
    }

    /**
     * Moves focus upward until the element matching the locator is focused,
     * then presses DPAD_CENTER to select/activate it.
     */
    public static void navigateUpTo(By locator) {
        requireAndroidTV();
        for (int i = 0; i < MAX_NAVIGATIONS; i++) {
            if (isFocused(locator)) {
                select();
                return;
            }
            up();
        }
        throw new RuntimeException(
                "Element not reachable after " + MAX_NAVIGATIONS + " D-pad up presses: " + locator);
    }

    /**
     * Moves focus rightward until the element matching the locator is focused,
     * then presses DPAD_CENTER to select/activate it.
     */
    public static void navigateRightTo(By locator) {
        requireAndroidTV();
        for (int i = 0; i < MAX_NAVIGATIONS; i++) {
            if (isFocused(locator)) {
                select();
                return;
            }
            right();
        }
        throw new RuntimeException(
                "Element not reachable after " + MAX_NAVIGATIONS + " D-pad right presses: " + locator);
    }

    /**
     * Moves focus leftward until the element matching the locator is focused,
     * then presses DPAD_CENTER to select/activate it.
     */
    public static void navigateLeftTo(By locator) {
        requireAndroidTV();
        for (int i = 0; i < MAX_NAVIGATIONS; i++) {
            if (isFocused(locator)) {
                select();
                return;
            }
            left();
        }
        throw new RuntimeException(
                "Element not reachable after " + MAX_NAVIGATIONS + " D-pad left presses: " + locator);
    }

    /** Returns the element that currently holds focus on screen. */
    public static WebElement getFocusedElement() {
        return DriverManager.getDriver().switchTo().activeElement();
    }

    /** Types text into whichever element currently has focus (e.g. after navigating to an EditText). */
    public static void typeInFocused(String text) {
        requireAndroidTV();
        getFocusedElement().sendKeys(text);
    }

    /** True when the element found by {@code locator} is the currently focused element. */
    public static boolean isFocused(By locator) {
        try {
            WebElement target = DriverManager.getDriver().findElement(locator);
            WebElement focused = getFocusedElement();
            return target.equals(focused);
        } catch (Exception e) {
            return false;
        }
    }

    private static void requireAndroidTV() {
        if (!"androidtv".equals(DeviceManager.currentDevice().getPlatform())) {
            throw new UnsupportedOperationException("DPad methods are Android TV-only");
        }
    }
}
