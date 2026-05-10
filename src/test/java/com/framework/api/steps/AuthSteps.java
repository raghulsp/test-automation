package com.framework.api.steps;

import com.framework.api.core.ApiScenarioContext;
import com.framework.api.services.AuthService;
import com.framework.api.utils.ResponseValidator;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class AuthSteps {

    private final ApiScenarioContext ctx;
    private final AuthService authService;

    public AuthSteps(ApiScenarioContext ctx) {
        this.ctx = ctx;
        this.authService = new AuthService();
    }

    @When("I login with email {string} and password {string}")
    public void login(String email, String password) {
        ctx.setResponse(authService.login(email, password));
    }

    @When("I register with email {string} and password {string}")
    public void register(String email, String password) {
        ctx.setResponse(authService.register(email, password));
    }

    @Then("the auth response status should be {int}")
    public void assertAuthStatus(int status) {
        ResponseValidator.assertStatusCode(ctx.getResponse(), status);
    }

    @Then("a valid auth token should be returned")
    public void assertTokenPresent() {
        String token = ctx.getResponse().jsonPath().getString("token");
        Assert.assertNotNull(token, "Expected auth token in response but found none");
        ctx.store("authToken", token);
    }

    @Then("an auth error message should be returned")
    public void assertErrorPresent() {
        ResponseValidator.assertFieldNotNull(ctx.getResponse(), "error");
    }
}
