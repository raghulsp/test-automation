package com.framework.drivers;

public class Device {

    private String udid;
    private String platformVersion;
    private String platform;
    private String appiumUrl;
    private String browser;
    private String appPackage;
    private String appActivity;

    public String getUdid()            { return udid; }
    public String getPlatformVersion() { return platformVersion; }
    public String getPlatform()        { return platform != null ? platform.toLowerCase() : "web"; }
    public String getAppiumUrl()       { return appiumUrl != null ? appiumUrl : "http://127.0.0.1:4723"; }
    public String getBrowser()         { return browser != null ? browser.toLowerCase() : "chrome"; }
    public String getAppPackage()      { return appPackage; }
    public String getAppActivity()     { return appActivity; }

    public void setUdid(String udid){
        this.udid = udid;
    }
    public void setPlatformVersion(String platformVersion){
        this.platformVersion = platformVersion;
    }
    public void setPlatform(String platform){
        this.platform = platform;
    }
    public void setAppiumUrl(String appiumUrl){
        this.appiumUrl = appiumUrl;
    }
    public void setBrowser(String browser){
        this.browser = browser;
    }
    public void setAppPackage(String appPackage){
        this.appPackage = appPackage;
    }
    public void setAppActivity(String appActivity){
        this.appActivity = appActivity;
    }

    @Override
    public String toString() {
        return "Device{udid='" + udid
                + "', platform='" + platform
                + "', version='" + platformVersion
                + "', browser='" + browser + "'}";
    }
}
