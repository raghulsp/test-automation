package com.framework.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class BrokenLinksUtils {

    public static void main(String[] args) throws IOException {
        
        WebDriver driver = new ChromeDriver();
        List<WebElement> links = driver.findElements(By.tagName("a"));

        for(WebElement link : links){
            String linkUrl = link.getAttribute("href");

            URL url = new URL(linkUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
           
        }

    }

}
