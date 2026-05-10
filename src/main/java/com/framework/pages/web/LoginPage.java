package com.framework.pages.web;

import com.framework.base.BasePage;
import com.framework.pages.ILoginPage;
import org.openqa.selenium.By;

public class LoginPage extends BasePage implements ILoginPage {

    private final By emailField = By.id("ap_email_login");
    private final By continueButton = By.id("continue-announce");
    private final By passwordField = By.id("ap_password");
    private final By loginButton   = By.id("signInSubmit");
    private final By helloGreeting = By.xpath("//span[contains(@class,'nav-line-1') and contains(text(),'Hello')]");
    private final By errorMessage  = By.cssSelector(".a-alert-content");

    @Override
    public void open(String url) {
        super.open(url);
    }

    @Override
    public void enterEmail(String username) {
        type(emailField, username);
    }

    @Override
    public void clickContinue(){
        click(continueButton);
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
    public String getVisibleMessage() {
        if (isDisplayedSafely(helloGreeting, 8)) return getText(helloGreeting);
        return getText(errorMessage);
    }

    @Override
    public String getCurrentUrl() {
        return currentUrl();
    }
}
