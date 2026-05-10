package com.framework.api.steps;

import com.framework.api.core.ApiScenarioContext;
import com.framework.api.services.UserService;
import com.framework.api.utils.ResponseValidator;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.Map;

public class UserSteps {

    private final ApiScenarioContext ctx;
    private final UserService userService;

    public UserSteps(ApiScenarioContext ctx) {
        this.ctx = ctx;
        this.userService = new UserService();
    }

    @When("I request all users on page {int}")
    public void getAllUsers(int page) {
        ctx.setResponse(userService.getAllUsers(page));
    }

    @When("I request user with id {int}")
    public void getUserById(int userId) {
        ctx.store("userId", userId);
        ctx.setResponse(userService.getUserById(userId));
    }

    @When("I create a user with name {string} and job {string}")
    public void createUser(String name, String job) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("job", job);
        ctx.setResponse(userService.createUser(payload));
    }

    @When("I update user {int} with name {string} and job {string}")
    public void updateUser(int userId, String name, String job) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("job", job);
        ctx.setResponse(userService.updateUser(userId, payload));
    }

    @When("I delete user {int}")
    public void deleteUser(int userId) {
        ctx.setResponse(userService.deleteUser(userId));
    }

    @Then("the user response status should be {int}")
    public void assertUserStatus(int status) {
        ResponseValidator.assertStatusCode(ctx.getResponse(), status);
    }

    @Then("the user response field {string} should be present")
    public void assertUserFieldPresent(String jsonPath) {
        ResponseValidator.assertFieldNotNull(ctx.getResponse(), jsonPath);
    }

    @Then("the user response field {string} should equal {string}")
    public void assertUserFieldEquals(String jsonPath, String expected) {
        ResponseValidator.assertFieldEquals(ctx.getResponse(), jsonPath, expected);
    }

    @Then("the created user id should be present")
    public void assertCreatedUserIdPresent() {
        String id = ctx.getResponse().jsonPath().getString("id");
        org.testng.Assert.assertNotNull(id, "Created user 'id' should be present in response");
        ctx.store("createdUserId", id);
    }
}
