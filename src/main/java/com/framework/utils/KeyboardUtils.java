package com.framework.utils;

import com.framework.drivers.DeviceManager;
import com.framework.drivers.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class KeyboardUtils {

    /**
     * Hide the soft keyboard if it is currently shown.
     * Android: uses adb to dismiss. iOS: taps outside the keyboard.
     * No-op if the keyboard is not shown.
     */
    public static void hideKeyboard() {
        if (!isKeyboardShown()) return;
        if (isAndroid()) {
            ((AndroidDriver) DriverManager.getDriver()).hideKeyboard();
        } else if (isIos()) {
            ((IOSDriver) DriverManager.getDriver()).hideKeyboard();
        } else {
            throw new UnsupportedOperationException("Keyboard methods are mobile-only");
        }
    }

    /**
     * iOS-only: hide keyboard by tapping a specific dismiss key (e.g. "Done", "Return", "Search").
     * Required for iOS forms where tap-outside doesn't dismiss reliably.
     */
    public static void hideKeyboard(String dismissKey) {
        if (!isIos()) {
            throw new UnsupportedOperationException("hideKeyboard(dismissKey) is iOS-only — use hideKeyboard() on Android");
        }
        if (!isKeyboardShown()) return;
        ((IOSDriver) DriverManager.getDriver()).hideKeyboard(dismissKey);
    }

    public static boolean isKeyboardShown() {
        if (isAndroid()) {
            return ((AndroidDriver) DriverManager.getDriver()).isKeyboardShown();
        } else if (isIos()) {
            return ((IOSDriver) DriverManager.getDriver()).isKeyboardShown();
        }
        return false;
    }

    private static boolean isAndroid() {
        return "android".equals(DeviceManager.currentDevice().getPlatform());
    }

    private static boolean isIos() {
        return "ios".equals(DeviceManager.currentDevice().getPlatform());
    }
}
