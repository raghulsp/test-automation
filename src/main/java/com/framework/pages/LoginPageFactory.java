package com.framework.pages;

import com.framework.drivers.DeviceManager;
import com.framework.pages.mobile.LoginPage;

public class LoginPageFactory {

    public static ILoginPage get() {
        String platform = DeviceManager.currentDevice().getPlatform();
        return switch (platform) {
            case "android", "ios" -> new LoginPage();
            default               -> new com.framework.pages.web.LoginPage();
        };
    }
}
