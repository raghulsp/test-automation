package com.framework.steps;

import com.framework.config.ConfigManager;
import com.framework.pages.ILoginPage;
import com.framework.pages.LoginPageFactory;
import com.framework.utils.NotificationUtils;
import com.framework.utils.TapUtils;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class LoginSteps {

    private ILoginPage loginPage;

    private ILoginPage loginPage() {
        if (loginPage == null) {
            loginPage = LoginPageFactory.get();
        }
        return loginPage;
    }

    @Given("the user is on the login page")
    public void userIsOnLoginPage() {
        loginPage().open(ConfigManager.get("base.url", ""));
    }

    @When("the user enters {string} and {string}")
    public void enterUserCredentials(String username, String password) {
        loginPage().enterEmail(username);
        loginPage().clickContinue();
        loginPage().enterPassword(password);
    }

    @And("the user clicks on the login button")
    public void clickLoginButton() {
        loginPage().clickLogin();
    }

    @Then("the user should be logged in")
    public void verifyUserIsLoggedIn() {
        String message = loginPage().getVisibleMessage();
        Assert.assertFalse(message == null || message.isBlank(),
                "Expected a post-login message but the page returned nothing");
    }

    @Then("the user should be able to see the {string}")
    public void verifyMessage(String expectedMessage) {
        String actual = loginPage().getVisibleMessage();
        Assert.assertTrue(actual.contains(expectedMessage),
                "Expected to see '" + expectedMessage + "' but got: '" + actual + "'");
    }

    @When("the user swipes down")
    public void the_user_swipes_down() {

        // SwipeUtils.androidScrollToResourceId("in.amazon.mShop:id/footer", Direction.DOWN);

        // driver.findElement(
        //     AppiumBy.androidUIAutomator(
        //         "new UiScrollable(new UiSelector().scrollable(true))"+
        //         ".scrollIntoView(new UiSelector().resourceId(\"" + resourceId + "\"))"))

        NotificationUtils.openNotificationTray();
        NotificationUtils.tapNotificationByUiSelector("WhatsApp");
        TapUtils.tapByAndroidUiResourceId("com.android.permissioncontroller:id/permission_allow_button");


    }
}
