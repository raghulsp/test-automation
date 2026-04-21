package com.framework.steps;

import com.framework.api.ApiClient;
import com.framework.api.services.AuthService;
import com.framework.api.services.UserService;
import com.framework.context.ScenarioContext;
import com.framework.models.UserCredentials;
import com.framework.models.UserRequest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;

/**
 * Step definitions for:
 * <ul>
 *   <li>Pure API scenarios tagged {@code @api}</li>
 *   <li>The API-setup step used in {@code @integration} scenarios</li>
 * </ul>
 *
 * Responses are stashed in {@link ScenarioContext} under the key {@code "lastResponse"}
 * so that assertion steps can be written once and reused across scenarios.
 */
public class ApiSteps {

    private final UserService userService = new UserService();
    private final AuthService authService = new AuthService();

    // ── Test-setup step (used by @integration scenarios) ─────────────────────

    /**
     * Creates a new user via the API and stores the returned credentials in
     * {@link ScenarioContext} so that subsequent UI login steps can read them.
     */
    @Given("a new user is created via the API")
    public void createUserViaApi() {
        String email    = "user_" + System.currentTimeMillis() + "@test.com";
        String password = "Password123!";
        UserCredentials credentials = userService.createAndReturnCredentials(email, password);
        ScenarioContext.set("credentials", credentials);
    }

    // ── Generic HTTP steps (reusable across @api scenarios) ──────────────────

    @When("I send POST to {string} with new user details")
    public void sendPostWithNewUser(String path) {
        UserRequest request = UserRequest.builder()
                .name("Test User")
                .job("qa-engineer")
                .build();
        Response response = userService.createUser(request);
        ScenarioContext.set("lastResponse", response);
    }

    @When("I send GET to {string}")
    public void sendGet(String path) {
        Response response = new ApiClient().get(path);
        ScenarioContext.set("lastResponse", response);
    }

    @When("I send DELETE to {string}")
    public void sendDelete(String path) {
        Response response = new ApiClient().delete(path);
        ScenarioContext.set("lastResponse", response);
    }

    // ── Auth steps ────────────────────────────────────────────────────────────

    @When("I authenticate with email {string} and password {string}")
    public void authenticateWithCredentials(String email, String password) {
        UserCredentials credentials = new UserCredentials(email, password);
        Response response = authService.login(credentials);
        ScenarioContext.set("lastResponse", response);
    }

    @Then("the response should contain an auth token")
    public void verifyAuthToken() {
        Response response = ScenarioContext.get("lastResponse", Response.class);
        Assert.assertNotNull(
                response.jsonPath().getString("token"),
                "Auth token was not present in the response"
        );
    }

    // ── Assertion steps ───────────────────────────────────────────────────────

    @Then("the response status should be {int}")
    public void verifyStatusCode(int expectedStatus) {
        Response response = ScenarioContext.get("lastResponse", Response.class);
        Assert.assertEquals(
                response.getStatusCode(), expectedStatus,
                "Status mismatch — expected " + expectedStatus + " but got " + response.getStatusCode()
        );
    }

    @Then("the response body should contain {string}")
    public void verifyResponseContainsField(String field) {
        Response response = ScenarioContext.get("lastResponse", Response.class);
        Assert.assertNotNull(
                response.jsonPath().get(field),
                "Field '" + field + "' not found in response:\n" + response.asPrettyString()
        );
    }

    @And("the response field {string} should equal {string}")
    public void verifyResponseFieldEquals(String field, String expected) {
        Response response = ScenarioContext.get("lastResponse", Response.class);
        String actual = response.jsonPath().getString(field);
        Assert.assertEquals(actual, expected,
                "Field '" + field + "' — expected '" + expected + "' but got '" + actual + "'");
    }
}
