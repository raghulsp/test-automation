package com.framework.utils;

import com.framework.drivers.DeviceManager;
import com.framework.drivers.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

/**
 * Hardware key presses on Android (BACK, HOME, APP_SWITCH, ENTER, etc.).
 * Wraps {@link AndroidDriver#pressKey(KeyEvent)} so callers don't construct KeyEvent themselves.
 * For the full enum see {@link AndroidKey}.
 */
public class AndroidKeyUtils {

    public static void back()        { press(AndroidKey.BACK); }
    public static void home()        { press(AndroidKey.HOME); }
    public static void appSwitch()   { press(AndroidKey.APP_SWITCH); }
    public static void enter()       { press(AndroidKey.ENTER); }
    public static void search()      { press(AndroidKey.SEARCH); }
    public static void escape()      { press(AndroidKey.ESCAPE); }
    public static void tab()         { press(AndroidKey.TAB); }
    public static void space()       { press(AndroidKey.SPACE); }
    public static void del()         { press(AndroidKey.DEL); }
    public static void volumeUp()    { press(AndroidKey.VOLUME_UP); }
    public static void volumeDown()  { press(AndroidKey.VOLUME_DOWN); }
    public static void power()       { press(AndroidKey.POWER); }

    /** Press any AndroidKey not covered by the named helpers above. */
    public static void press(AndroidKey key) {
        requireAndroid();
        ((AndroidDriver) DriverManager.getDriver()).pressKey(new KeyEvent(key));
    }

    /** Long-press (hold) any AndroidKey — e.g. POWER for shutdown menu. */
    public static void longPress(AndroidKey key) {
        requireAndroid();
        ((AndroidDriver) DriverManager.getDriver()).longPressKey(new KeyEvent(key));
    }

    private static void requireAndroid() {
        String platform = DeviceManager.currentDevice().getPlatform();
        if (!"android".equals(platform) && !"androidtv".equals(platform)) {
            throw new UnsupportedOperationException("Hardware key presses are Android/AndroidTV-only");
        }
    }
}
