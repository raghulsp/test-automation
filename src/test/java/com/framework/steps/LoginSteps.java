package com.framework.steps;

import com.framework.config.ConfigManager;
import com.framework.pages.ILoginPage;
import com.framework.pages.LoginPageFactory;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class LoginSteps {

    private final ILoginPage loginPage = LoginPageFactory.get();

    @Given("the user is on the login page")
    public void userIsOnLoginPage() {
        loginPage.open(ConfigManager.get("base.url", "https://the-internet.herokuapp.com/login"));
    }

    @When("the user enters {string} and {string}")
    public void enterUserCredentials(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }

    @And("the user clicks on the login button")
    public void clickLoginButton() {
        loginPage.clickLogin();
    }

    @Then("the user should be able to see the {string}")
    public void verifyMessage(String expectedMessage) {
        String platform = ConfigManager.get("platform", "web").toLowerCase();
        if (expectedMessage.equals("login success")) {
            if (platform.equals("web")) {
                Assert.assertTrue(loginPage.getCurrentUrl().contains("secure"),
                        "Expected URL to contain 'secure' but got: " + loginPage.getCurrentUrl());
            }
        } else {
            Assert.assertTrue(loginPage.getErrorMessage().contains(expectedMessage),
                    "Expected error to contain: " + expectedMessage);
        }
    }
}
