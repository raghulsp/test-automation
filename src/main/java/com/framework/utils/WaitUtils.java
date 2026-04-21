package com.framework.utils;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.framework.drivers.DriverManager;

public class WaitUtils{

    private static WebDriverWait wait(int seconds){
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(seconds));
    }

    public static WebElement waitForVisible(By locator, int seconds){
        return wait(seconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(By locator, int seconds){
        return wait(seconds).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForClickable(By locator){
        return waitForClickable(locator, 10);
    }

    public static WebElement waitForVisible(By locator) {
        return waitForVisible(locator, 10);
    }


}