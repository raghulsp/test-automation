package com.framework.base;

import com.framework.drivers.DeviceManager;
import com.framework.drivers.DriverManager;
import com.framework.utils.AndroidKeyUtils;
import com.framework.utils.DPadUtils;
import com.framework.utils.GestureUtils;
import com.framework.utils.KeyboardUtils;
import com.framework.utils.NotificationUtils;
import com.framework.utils.SwipeUtils;
import com.framework.utils.TapUtils;
import com.framework.utils.WaitUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;

public class BasePage {

    private static final int MAX_SWIPES = 15;

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

    protected boolean isDisplayed(By locator){
        return find(locator).isDisplayed();
    }

    protected boolean isDisplayedSafely(By locator, int timeoutSeconds) {
        try {
            return WaitUtils.waitForVisible(locator, timeoutSeconds).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void swipeUp() {
        SwipeUtils.swipeUp();
    }

    protected void swipeDown() {
        SwipeUtils.swipeDown();
    }

    protected void swipeToBottom() {
        AppiumDriver driver = (AppiumDriver) DriverManager.getDriver();
        String prevSource = null;
        for (int i = 0; i < MAX_SWIPES; i++) {
            String currentSource = driver.getPageSource();
            if (currentSource.equals(prevSource)) break;
            prevSource = currentSource;
            SwipeUtils.swipeUp();
        }
    }

    // --- Keyboard helpers ----------------------------------------------------

    protected void hideKeyboard() {
        KeyboardUtils.hideKeyboard();
    }

    protected void hideKeyboard(String dismissKey) {
        KeyboardUtils.hideKeyboard(dismissKey);
    }

    protected boolean isKeyboardShown() {
        return KeyboardUtils.isKeyboardShown();
    }

    // --- D-pad / Android TV remote ------------------------------------------

    protected void dpadUp()                        { DPadUtils.up(); }
    protected void dpadDown()                      { DPadUtils.down(); }
    protected void dpadLeft()                      { DPadUtils.left(); }
    protected void dpadRight()                     { DPadUtils.right(); }
    protected void dpadSelect()                    { DPadUtils.select(); }
    protected void navigateDownTo(By locator)      { DPadUtils.navigateDownTo(locator); }
    protected void navigateUpTo(By locator)        { DPadUtils.navigateUpTo(locator); }
    protected void navigateRightTo(By locator)     { DPadUtils.navigateRightTo(locator); }
    protected void navigateLeftTo(By locator)      { DPadUtils.navigateLeftTo(locator); }
    protected void typeInFocused(String text)      { DPadUtils.typeInFocused(text); }
    protected boolean isFocused(By locator)        { return DPadUtils.isFocused(locator); }
    protected WebElement getFocusedElement()       { return DPadUtils.getFocusedElement(); }

    // --- Android hardware keys ----------------------------------------------

    protected void pressBack()       { AndroidKeyUtils.back(); }
    protected void pressHome()       { AndroidKeyUtils.home(); }
    protected void pressAppSwitch()  { AndroidKeyUtils.appSwitch(); }
    protected void pressEnter()      { AndroidKeyUtils.enter(); }
    protected void pressSearch()     { AndroidKeyUtils.search(); }
    protected void pressEscape()     { AndroidKeyUtils.escape(); }
    protected void pressKey(AndroidKey key)     { AndroidKeyUtils.press(key); }
    protected void longPressKey(AndroidKey key) { AndroidKeyUtils.longPress(key); }

    // --- Gesture helpers -----------------------------------------------------

    protected void longPress(WebElement element) {
        GestureUtils.longPress(element);
    }

    protected void longPress(WebElement element, Duration duration) {
        GestureUtils.longPress(element, duration);
    }

    protected void doubleTap(WebElement element) {
        GestureUtils.doubleTap(element);
    }

    protected void pinchOpen(WebElement element) {
        GestureUtils.pinchOpen(element);
    }

    protected void pinchClose(WebElement element) {
        GestureUtils.pinchClose(element);
    }

    protected void dragAndDrop(WebElement source, WebElement target) {
        GestureUtils.dragAndDrop(source, target);
    }

    protected void tapAt(int x, int y) {
        GestureUtils.tapAt(x, y);
    }

    protected void tapScreenCenter() {
        GestureUtils.tapScreenCenter();
    }

    protected void swipeSurferBarToMiddle(By seekBarLocator) {
        SwipeUtils.swipeSurferBarToMiddle(seekBarLocator);
    }

    protected void swipeSurferBarToPercent(By seekBarLocator, double percent) {
        SwipeUtils.swipeSurferBarToPercent(seekBarLocator, percent);
    }

    // --- Tap helpers ---------------------------------------------------------

    protected void tapByXpath(String xpath) {
        TapUtils.tapByXpath(xpath);
    }

    protected void tapById(String resourceId) {
        TapUtils.tapById(resourceId);
    }

    protected void tapByAccessibilityId(String id) {
        TapUtils.tapByAccessibilityId(id);
    }

    protected void tapByAndroidUiText(String text) {
        TapUtils.tapByAndroidUiText(text);
    }

    protected void tapByAndroidUiTextContains(String text) {
        TapUtils.tapByAndroidUiTextContains(text);
    }

    protected void tapByAndroidUiResourceId(String resourceId) {
        TapUtils.tapByAndroidUiResourceId(resourceId);
    }

    protected void tapByIosPredicateString(String predicate) {
        TapUtils.tapByIosPredicateString(predicate);
    }

    protected void tapByIosClassChain(String chain) {
        TapUtils.tapByIosClassChain(chain);
    }

    // --- Notification helpers ------------------------------------------------

    protected void openNotificationTray() {
        NotificationUtils.openNotificationTray();
    }

    protected void tapNotificationByText(String text) {
        NotificationUtils.tapNotificationByText(text);
    }

    protected void tapNotificationByTextContains(String text) {
        NotificationUtils.tapNotificationByTextContains(text);
    }

    protected void swipeToTop() {
        AppiumDriver driver = (AppiumDriver) DriverManager.getDriver();
        String prevSource = null;
        for (int i = 0; i < MAX_SWIPES; i++) {
            String currentSource = driver.getPageSource();
            if (currentSource.equals(prevSource)) break;
            prevSource = currentSource;
            SwipeUtils.swipeDown();
        }
    }
}
