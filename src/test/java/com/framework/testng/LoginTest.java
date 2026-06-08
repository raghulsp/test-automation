package com.framework.testng;

import com.framework.config.ConfigManager;
import com.framework.drivers.DeviceManager;
import com.framework.pages.web.LoginPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

/**
 * Pure TestNG login tests — no Cucumber, no feature files.
 *
 * Key TestNG concepts demonstrated here:
 *   @Test attributes  : description, groups, priority, dependsOnMethods, enabled
 *   @DataProvider     : parameterised runs (like Cucumber Scenario Outline, but in Java)
 *   Assert            : hard fail — stops the test immediately on first failure
 *   SoftAssert        : collects all failures, reports them together at assertAll()
 *   SkipException     : programmatic skip (equivalent to Cucumber's @wip / assumeTrue)
 *   enabled = false   : static skip — exclude a test without deleting it
 */
public class LoginTest extends BaseTest {

    private static final String SIGNIN_URL = "/ap/signin";

    // -------------------------------------------------------------------------
    // 1. @Test basics — description, groups, priority
    // -------------------------------------------------------------------------

    @Test(
        description = "Login page loads and shows the email field",
        groups      = {"smoke"},
        priority    = 1
    )
    public void testLoginPageLoads() {
        LoginPage page = new LoginPage();
        page.open(ConfigManager.get("base.url") + SIGNIN_URL);

        // Hard assert: test stops here if the title doesn't match
        Assert.assertTrue(
            driver.getTitle().contains("Amazon"),
            "Page title should contain 'Amazon'"
        );
    }

    // -------------------------------------------------------------------------
    // 2. dependsOnMethods — only runs when testLoginPageLoads passes
    //    If the dependency fails this test is automatically SKIPPED (not failed)
    // -------------------------------------------------------------------------

    @Test(
        description      = "Invalid credentials show an error message",
        groups           = {"smoke", "regression"},
        priority         = 2,
        dependsOnMethods = {"testLoginPageLoads"}
    )
    public void testInvalidLogin() {
        LoginPage page = new LoginPage();
        page.open(ConfigManager.get("base.url") + SIGNIN_URL);
        page.enterEmail("invalid@nowhere.com");
        page.clickContinue();
        page.enterPassword("wrongpassword");
        page.clickLogin();

        String message = page.getVisibleMessage();
        Assert.assertTrue(
            message.contains("problem") || message.contains("incorrect"),
            "Expected an error message but got: " + message
        );
    }

    // -------------------------------------------------------------------------
    // 3. @DataProvider — same test logic, multiple input rows
    //    Equivalent to a Cucumber Scenario Outline + Examples table
    // -------------------------------------------------------------------------

    @DataProvider(name = "invalidEmailFormats")
    public Object[][] invalidEmailFormats() {
        return new Object[][] {
            { "notanemail",     "Enter your email" },
            { "missing@",       "Enter your email" },
            { "spaces @eg.com", "Enter your email" },
        };
    }

    @Test(
        description  = "Malformed email addresses trigger inline validation",
        groups       = {"regression"},
        priority     = 3,
        dataProvider = "invalidEmailFormats"
    )
    public void testEmailValidation(String email, String expectedFragment) {
        LoginPage page = new LoginPage();
        page.open(ConfigManager.get("base.url") + SIGNIN_URL);
        page.enterEmail(email);
        page.clickContinue();

        String message = page.getVisibleMessage();
        Assert.assertTrue(
            message.contains(expectedFragment),
            "Expected '" + expectedFragment + "' in message but got: " + message
        );
    }

    // -------------------------------------------------------------------------
    // 4. SoftAssert — check multiple things, report ALL failures at once
    //    Contrast with Assert: a hard assert would stop at the first failure
    //    and hide any subsequent issues in the same test.
    // -------------------------------------------------------------------------

    @Test(
        description = "Login page has correct title and URL",
        groups      = {"smoke"},
        priority    = 4
    )
    public void testLoginPageMetadata() {
        LoginPage page = new LoginPage();
        page.open(ConfigManager.get("base.url") + SIGNIN_URL);

        SoftAssert soft = new SoftAssert();
        soft.assertTrue(driver.getTitle().contains("Amazon"),    "Title should contain 'Amazon'");
        soft.assertTrue(page.getCurrentUrl().contains("signin"), "URL should contain 'signin'");
        // assertAll() triggers the actual failure; without it soft assertions are silently ignored
        soft.assertAll();
    }

    // -------------------------------------------------------------------------
    // 5. enabled = false — permanently skip without deleting the test
    //    Useful for WIP tests or known-broken flows you'll fix later
    // -------------------------------------------------------------------------

    @Test(
        description = "Forgot-password flow — not yet implemented",
        groups      = {"regression"},
        enabled     = false
    )
    public void testForgotPassword() {
        // TODO: implement forgot-password flow
    }

    // -------------------------------------------------------------------------
    // 6. SkipException — programmatic skip based on runtime conditions
    //    Unlike enabled=false (compile-time), this decides at runtime
    // -------------------------------------------------------------------------

    @Test(
        description = "Deep-link login — mobile only",
        groups      = {"regression"},
        priority    = 5
    )
    public void testMobileDeepLink() {
        String platform = DeviceManager.currentDevice().getPlatform();
        if (!"android".equals(platform) && !"ios".equals(platform)) {
            throw new SkipException("Deep-link test requires a mobile device; skipping on web.");
        }
        // mobile-only steps would go here
    }
}
