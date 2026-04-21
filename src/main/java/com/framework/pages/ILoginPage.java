package com.framework.pages;

public interface ILoginPage {
    void open(String url);
    void enterUsername(String username);
    void enterPassword(String password);
    void clickLogin();
    String getErrorMessage();
    String getCurrentUrl();
}
