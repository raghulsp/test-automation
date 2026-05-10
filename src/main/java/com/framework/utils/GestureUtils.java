package com.framework.utils;

import com.framework.drivers.DeviceManager;
import com.framework.drivers.DriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Mobile gestures via Appium 2's {@code mobile:} script extensions.
 * These are more reliable than W3C Actions for pinch / drag / long-press.
 * Android uses UiAutomator2 driver extensions; iOS uses XCUITest.
 *
 * <p>Duration semantics differ across drivers — Android takes milliseconds, iOS takes seconds.
 * The methods here normalise on {@link Duration} as input and convert internally.
 */
public class GestureUtils {

    private static final Duration DEFAULT_LONG_PRESS = Duration.ofSeconds(1);
    private static final Duration DEFAULT_DRAG       = Duration.ofSeconds(1);

    // -------------------------------------------------------------------------
    // Long press
    // -------------------------------------------------------------------------

    public static void longPress(WebElement element) {
        longPress(element, DEFAULT_LONG_PRESS);
    }

    public static void longPress(WebElement element, Duration duration) {
        requireMobile();
        Map<String, Object> args = new HashMap<>();
        args.put("elementId", elementId(element));
        if (isAndroid()) {
            args.put("duration", (int) duration.toMillis());          // ms
            executeMobile("longClickGesture", args);
        } else {
            args.put("duration", duration.toMillis() / 1000.0);       // seconds
            executeMobile("touchAndHold", args);
        }
    }

    // -------------------------------------------------------------------------
    // Double tap
    // -------------------------------------------------------------------------

    public static void doubleTap(WebElement element) {
        requireMobile();
        Map<String, Object> args = Map.of("elementId", elementId(element));
        executeMobile(isAndroid() ? "doubleClickGesture" : "doubleTap", args);
    }

    // -------------------------------------------------------------------------
    // Pinch — open (zoom in) and close (zoom out)
    // -------------------------------------------------------------------------

    /** Zoom in on the element. Default 75% pinch-out (Android) / 2x scale (iOS). */
    public static void pinchOpen(WebElement element) {
        pinchOpen(element, 0.75);
    }

    /**
     * Zoom in. {@code amount} is the pinch percent on Android (0–1), or scale factor on iOS (>1).
     * If you pass 0.75 on iOS it's auto-bumped to 2.0 since iOS scale must be >1 to zoom in.
     */
    public static void pinchOpen(WebElement element, double amount) {
        requireMobile();
        Map<String, Object> args = new HashMap<>();
        args.put("elementId", elementId(element));
        if (isAndroid()) {
            args.put("percent", amount);
            executeMobile("pinchOpenGesture", args);
        } else {
            args.put("scale", amount > 1 ? amount : 2.0);
            args.put("velocity", 1.0);
            executeMobile("pinch", args);
        }
    }

    /** Zoom out on the element. Default 75% pinch-in (Android) / 0.5x scale (iOS). */
    public static void pinchClose(WebElement element) {
        pinchClose(element, 0.75);
    }

    /**
     * Zoom out. {@code amount} is the pinch percent on Android (0–1), or scale factor on iOS (<1).
     * If you pass 0.75 on iOS it's auto-clamped to 0.5 since iOS scale must be <1 to zoom out.
     */
    public static void pinchClose(WebElement element, double amount) {
        requireMobile();
        Map<String, Object> args = new HashMap<>();
        args.put("elementId", elementId(element));
        if (isAndroid()) {
            args.put("percent", amount);
            executeMobile("pinchCloseGesture", args);
        } else {
            args.put("scale", amount < 1 ? amount : 0.5);
            args.put("velocity", -1.0);
            executeMobile("pinch", args);
        }
    }

    // -------------------------------------------------------------------------
    // Drag and drop
    // -------------------------------------------------------------------------

    public static void dragAndDrop(WebElement source, WebElement target) {
        dragAndDrop(source, target, DEFAULT_DRAG);
    }

    public static void dragAndDrop(WebElement source, WebElement target, Duration duration) {
        requireMobile();
        int[] src = center(source);
        int[] tgt = center(target);
        Map<String, Object> args = new HashMap<>();
        if (isAndroid()) {
            args.put("elementId", elementId(source));
            args.put("endX", tgt[0]);
            args.put("endY", tgt[1]);
            executeMobile("dragGesture", args);
        } else {
            args.put("fromX", src[0]);
            args.put("fromY", src[1]);
            args.put("toX",   tgt[0]);
            args.put("toY",   tgt[1]);
            args.put("duration", duration.toMillis() / 1000.0);
            executeMobile("dragFromToForDuration", args);
        }
    }

    // -------------------------------------------------------------------------
    // Tap at coordinates (no element required) — useful to dismiss overlays
    // -------------------------------------------------------------------------

    public static void tapAt(int x, int y) {
        requireMobile();
        Map<String, Object> args = Map.of("x", x, "y", y);
        executeMobile(isAndroid() ? "clickGesture" : "tap", args);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static int[] center(WebElement element) {
        Rectangle r = element.getRect();
        return new int[] { r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2 };
    }

    private static String elementId(WebElement element) {
        return ((RemoteWebElement) element).getId();
    }

    private static void executeMobile(String command, Map<String, Object> args) {
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("mobile: " + command, args);
    }

    private static boolean isAndroid() {
        return "android".equals(DeviceManager.currentDevice().getPlatform());
    }

    private static void requireMobile() {
        String platform = DeviceManager.currentDevice().getPlatform();
        if (!"android".equals(platform) && !"ios".equals(platform)) {
            throw new UnsupportedOperationException("Gesture methods are mobile-only");
        }
    }
}
