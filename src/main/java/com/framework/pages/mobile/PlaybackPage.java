package com.framework.pages.mobile;

import org.openqa.selenium.By;

import com.framework.base.BasePage;

public class PlaybackPage extends BasePage{

    // TODO: replace with actual locator from Appium Inspector
      private final By seekBar = By.id("com.yourapp:id/seek_bar");

      public void revealAndSeekToMiddle() {
          tapScreenCenter();
          swipeSurferBarToMiddle(seekBar);
      }

}
