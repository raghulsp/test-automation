package com.framework.pages.web;

import com.framework.base.BasePage;
import com.framework.pages.ILoginPage;
import org.openqa.selenium.By;

public class LoginPage extends BasePage implements ILoginPage {

    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By loginButton   = By.id("login");
    private final By errorMessage  = By.cssSelector(".flash.error");

    @Override
    public void open(String url) {
        open(url);
    }

    @Override
    public void enterUsername(String username) {
        type(usernameField, username);
    }

    @Override
    public void enterPassword(String password) {
        type(passwordField, password);
    }

    @Override
    public void clickLogin() {
        click(loginButton);
    }

    @Override
    public String getErrorMessage() {
        return getText(errorMessage);
    }

    @Override
    public String getCurrentUrl() {
        return currentUrl();
    }
}
