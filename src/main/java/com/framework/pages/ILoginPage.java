package com.framework.pages;

public interface ILoginPage {
    void open(String url);
    void enterEmail(String username);
    void clickContinue();
    void enterPassword(String password);
    void clickLogin();
    String getVisibleMessage();
    String getCurrentUrl();
}
