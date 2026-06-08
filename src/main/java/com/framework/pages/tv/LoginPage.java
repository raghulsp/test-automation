package com.framework.pages.tv;

import com.framework.base.BasePage;
import com.framework.pages.ILoginPage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Android TV login page — no touch events.
 * Navigation is entirely D-pad driven: focus moves to each field with navigateDownTo(),
 * text is typed into the focused element, and the login button is activated the same way.
 * Replace the placeholder locators below with real values from Appium Inspector.
 */
public class LoginPage extends BasePage implements ILoginPage {

    // Replace with real locators from Appium Inspector for your TV app
    private static final By EMAIL_FIELD     = AppiumBy.accessibilityId("tv_email_field");
    private static final By CONTINUE_BUTTON = AppiumBy.accessibilityId("tv_continue_btn");
    private static final By PASSWORD_FIELD  = AppiumBy.accessibilityId("tv_password_field");
    private static final By LOGIN_BUTTON    = AppiumBy.accessibilityId("tv_login_btn");
    private static final By SUCCESS_MESSAGE = AppiumBy.androidUIAutomator(
            "new UiSelector().resourceId(\"com.example.tv:id/welcome_msg\")");
    private static final By ERROR_MESSAGE   = AppiumBy.androidUIAutomator(
            "new UiSelector().resourceId(\"com.example.tv:id/error_msg\")");

    @Override
    public void open(String url) {
        // TV apps launch via capabilities — no URL navigation
    }

    @Override
    public void enterEmail(String email) {
        navigateDownTo(EMAIL_FIELD);
        typeInFocused(email);
    }

    @Override
    public void clickContinue() {
        navigateDownTo(CONTINUE_BUTTON);
    }

    @Override
    public void enterPassword(String password) {
        navigateDownTo(PASSWORD_FIELD);
        typeInFocused(password);
    }

    @Override
    public void clickLogin() {
        navigateDownTo(LOGIN_BUTTON);
    }

    @Override
    public String getVisibleMessage() {
        if (isDisplayedSafely(SUCCESS_MESSAGE, 8)) return getText(SUCCESS_MESSAGE);
        if (isDisplayedSafely(ERROR_MESSAGE, 2))   return getText(ERROR_MESSAGE);
        return "";
    }

    @Override
    public String getCurrentUrl() {
        return "";
    }
}
