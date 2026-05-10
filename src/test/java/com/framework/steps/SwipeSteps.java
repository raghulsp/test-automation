package com.framework.steps;

import com.framework.base.BasePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class SwipeSteps extends BasePage {

    private String topPageSource;

    @Given("the user is on a scrollable mobile screen")
    public void userIsOnScrollableScreen() {
        topPageSource = ((io.appium.java_client.AppiumDriver) com.framework.drivers.DriverManager.getDriver()).getPageSource();
    }

    @When("the user swipes to the bottom of the screen")
    public void userSwipesToBottom() {
        swipeToBottom();
    }

    @And("the user swipes back to the top of the screen")
    public void userSwipesToTop() {
        swipeToTop();
    }

    @Then("the screen should be back at the top")
    public void screenShouldBeAtTop() {
        String currentSource = ((io.appium.java_client.AppiumDriver) com.framework.drivers.DriverManager.getDriver()).getPageSource();
        Assert.assertEquals(currentSource, topPageSource,
                "Page source after swipeToTop does not match initial top-of-screen state");
    }
}
