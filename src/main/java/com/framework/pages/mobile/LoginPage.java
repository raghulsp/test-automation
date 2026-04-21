package com.framework.pages.mobile;

import com.framework.base.BasePage;
import com.framework.pages.ILoginPage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class LoginPage extends BasePage implements ILoginPage {

    // Each locator method returns the right locator based on platform at call time
    private By usernameField() {
        return platform().equals("android")
                ? AppiumBy.accessibilityId("username_field")
                : AppiumBy.iOSNsPredicateString("name == 'username_field'");
    }

    private By passwordField() {
        return platform().equals("android")
                ? AppiumBy.accessibilityId("password_field")
                : AppiumBy.iOSNsPredicateString("name == 'password_field'");
    }

    private By loginButton() {
        return platform().equals("android")
                ? AppiumBy.accessibilityId("login_btn")
                : AppiumBy.iOSNsPredicateString("name == 'login_btn'");
    }

    private By errorMessage() {
        return platform().equals("android")
                ? AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.example:id/error_msg\")")
                : AppiumBy.iOSNsPredicateString("name == 'error_message'");
    }

    @Override
    public void open(String url) {
        // Native apps launch via capabilities — URL navigation not applicable
        // Override only if testing a mobile browser (hybrid app)
    }

    @Override
    public void enterUsername(String username) {
        type(usernameField(), username);
    }

    @Override
    public void enterPassword(String password) {
        type(passwordField(), password);
    }

    @Override
    public void clickLogin() {
        click(loginButton());
    }

    @Override
    public String getErrorMessage() {
        return getText(errorMessage());
    }

    @Override
    public String getCurrentUrl() {
        return platform().equals("android") ? "" : "";
    }
}
