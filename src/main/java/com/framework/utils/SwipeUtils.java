package com.framework.utils;

import com.framework.drivers.DeviceManager;
import com.framework.drivers.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwipeUtils {

    public enum Direction { UP, DOWN, LEFT, RIGHT }

    private static final int DEFAULT_MAX_SWIPES = 15;

    public static void swipeUp() {
        swipe(Direction.UP);
    }

    public static void swipeDown() {
        swipe(Direction.DOWN);
    }

    public static void swipeLeft() {
        swipe(Direction.LEFT);
    }

    public static void swipeRight() {
        swipe(Direction.RIGHT);
    }

    public static void swipe(Direction direction) {
        requireMobile();
        AppiumDriver driver = (AppiumDriver) DriverManager.getDriver();
        Dimension size = driver.manage().window().getSize();
        int centerX = size.width / 2;
        int centerY = size.height / 2;

        int endX, endY;
        switch (direction) {
            case UP    -> { endX = centerX; endY = (int) (size.height * 0.10); }
            case DOWN  -> { endX = centerX; endY = (int) (size.height * 0.90); }
            case LEFT  -> { endX = (int) (size.width * 0.10); endY = centerY; }
            case RIGHT -> { endX = (int) (size.width * 0.90); endY = centerY; }
            default    -> throw new IllegalArgumentException("Unknown direction: " + direction);
        }

        performSwipe(centerX, centerY, endX, endY);
    }

    public static void swipeUntilVisible(By locator, Direction direction) {
        swipeUntilVisible(locator, direction, DEFAULT_MAX_SWIPES);
    }

    public static void swipeUntilVisible(By locator, Direction direction, int maxSwipes) {
        requireMobile();
        for (int i = 0; i < maxSwipes; i++) {
            try {
                WaitUtils.waitForVisible(locator, 2);
                return;
            } catch (Exception e) {
                swipe(direction);
            }
        }
        // final attempt — throws TimeoutException if still not visible
        WaitUtils.waitForVisible(locator, 2);
    }

    // -------------------------------------------------------------------------
    // Seek bar (surfer bar) drag — drag thumb from its current position to a
    // target percentage of the bar width. Reads current progress via "value"
    // and "max" attributes (UiAutomator2 / XCUITest expose these for SeekBar /
    // UISlider). Falls back to the left edge when attributes are unavailable.
    // -------------------------------------------------------------------------

    public static void swipeSurferBarToMiddle(By seekBarLocator) {
        swipeSurferBarToPercent(seekBarLocator, 0.5);
    }

    public static void swipeSurferBarToPercent(By seekBarLocator, double targetPercent) {
        requireMobile();
        WebElement seekBar = WaitUtils.waitForVisible(seekBarLocator);
        Rectangle rect = seekBar.getRect();
        int barY   = rect.getY() + rect.getHeight() / 2;
        int barLeft  = rect.getX();
        int barWidth = rect.getWidth();

        double currentPercent = readSeekBarProgress(seekBar);
        int startX = barLeft + (int) (barWidth * currentPercent);
        int endX   = barLeft + (int) (barWidth * targetPercent);
        performSwipe(startX, barY, endX, barY);
    }

    private static double readSeekBarProgress(WebElement seekBar) {
        try {
            String value    = seekBar.getAttribute("value");
            String maxValue = seekBar.getAttribute("max");
            if (value != null && !value.isEmpty() && maxValue != null && !maxValue.isEmpty()) {
                return Double.parseDouble(value) / Double.parseDouble(maxValue);
            }
        } catch (NumberFormatException ignored) {}
        return 0.0;
    }

    private static void performSwipe(int startX, int startY, int endX, int endY) {
        AppiumDriver driver = (AppiumDriver) DriverManager.getDriver();
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(new Pause(finger, Duration.ofMillis(200)));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(swipe));
    }

    // -------------------------------------------------------------------------
    // Android — UiScrollable (scrolls the nearest scrollable container)
    // Direction LEFT/RIGHT uses a horizontal list; UP/DOWN uses vertical (default).
    // UiScrollable.scrollIntoView searches both forward and backward automatically,
    // so UP vs DOWN within the same axis has no effect on UiScrollable — only
    // horizontal vs vertical matters.
    // -------------------------------------------------------------------------

    public static void androidScrollToText(String text) {
        androidScrollToText(text, Direction.DOWN);
    }

    public static void androidScrollToText(String text, Direction direction) {
        requireAndroid();
        String selector = buildUiScrollable(direction) +
                ".scrollIntoView(new UiSelector().text(\"" + text + "\"))";
        ((AndroidDriver) DriverManager.getDriver()).findElement(AppiumBy.androidUIAutomator(selector));
    }

    public static void androidScrollToTextContains(String text) {
        androidScrollToTextContains(text, Direction.DOWN);
    }

    public static void androidScrollToTextContains(String text, Direction direction) {
        requireAndroid();
        String selector = buildUiScrollable(direction) +
                ".scrollIntoView(new UiSelector().textContains(\"" + text + "\"))";
        ((AndroidDriver) DriverManager.getDriver()).findElement(AppiumBy.androidUIAutomator(selector));
    }

    public static void androidScrollToResourceId(String resourceId) {
        androidScrollToResourceId(resourceId, Direction.DOWN);
    }

    public static void androidScrollToResourceId(String resourceId, Direction direction) {
        requireAndroid();
        String selector = buildUiScrollable(direction) +
                ".scrollIntoView(new UiSelector().resourceId(\"" + resourceId + "\"))";
        ((AndroidDriver) DriverManager.getDriver()).findElement(AppiumBy.androidUIAutomator(selector));

        AppiumBy.androidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().resourceId(resourceId))");
    }

    // -------------------------------------------------------------------------
    // iOS — mobile: scroll (XCUITest driver extension)
    // Scrolls the whole window in the given direction until the matching element
    // becomes visible. direction DOWN means "scroll content downward" (reveals
    // elements further down the page).
    // -------------------------------------------------------------------------

    public static void iosScrollToText(String text) {
        iosScrollToText(text, Direction.DOWN);
    }

    public static void iosScrollToText(String text, Direction direction) {
        requireIOS();
        iosMobileScroll(direction, "predicateString", "label == '" + text + "'");
    }

    public static void iosScrollToTextContains(String text) {
        iosScrollToTextContains(text, Direction.DOWN);
    }

    public static void iosScrollToTextContains(String text, Direction direction) {
        requireIOS();
        iosMobileScroll(direction, "predicateString", "label CONTAINS '" + text + "'");
    }

    public static void iosScrollToAccessibilityId(String accessibilityId) {
        iosScrollToAccessibilityId(accessibilityId, Direction.DOWN);
    }

    public static void iosScrollToAccessibilityId(String accessibilityId, Direction direction) {
        requireIOS();
        iosMobileScroll(direction, "name", accessibilityId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static String buildUiScrollable(Direction direction) {
        boolean horizontal = direction == Direction.LEFT || direction == Direction.RIGHT;
        String orientation = horizontal ? ".setAsHorizontalList()" : "";
        return "new UiScrollable(new UiSelector().scrollable(true))" + orientation;
    }

    private static void iosMobileScroll(Direction direction, String matchKey, String matchValue) {
        Map<String, Object> args = new HashMap<>();
        args.put("direction", direction.name().toLowerCase());
        args.put(matchKey, matchValue);
        ((IOSDriver) DriverManager.getDriver()).executeScript("mobile: scroll", args);
    }

    private static void requireMobile() {
        if (DeviceManager.currentDevice().getPlatform().equals("web")) {
            throw new UnsupportedOperationException("swipe is not supported on web");
        }
    }

    private static void requireAndroid() {
        if (!DeviceManager.currentDevice().getPlatform().equals("android")) {
            throw new UnsupportedOperationException("androidScroll methods are Android-only");
        }
    }

    private static void requireIOS() {
        if (!DeviceManager.currentDevice().getPlatform().equals("ios")) {
            throw new UnsupportedOperationException("iosScroll methods are iOS-only");
        }
    }
}
