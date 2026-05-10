package com.framework.api.steps;

import com.framework.api.core.ApiScenarioContext;
import com.framework.api.services.OrderService;
import com.framework.api.utils.ResponseValidator;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.Map;

public class OrderSteps {

    private final ApiScenarioContext ctx;
    private final OrderService orderService;

    public OrderSteps(ApiScenarioContext ctx) {
        this.ctx = ctx;
        this.orderService = new OrderService();
    }

    @When("I request all orders")
    public void getAllOrders() {
        ctx.setResponse(orderService.getAllOrders());
    }

    @When("I request order with id {int}")
    public void getOrderById(int orderId) {
        ctx.setResponse(orderService.getOrderById(orderId));
    }

    @When("I request orders for user {int}")
    public void getOrdersByUser(int userId) {
        ctx.setResponse(orderService.getOrdersByUser(userId));
    }

    @When("I create an order for user {int} with title {string}")
    public void createOrder(int userId, String title) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("title", title);
        payload.put("completed", false);
        ctx.setResponse(orderService.createOrder(payload));
    }

    @When("I cancel order {int}")
    public void cancelOrder(int orderId) {
        ctx.setResponse(orderService.deleteOrder(orderId));
    }

    @Then("the order response status should be {int}")
    public void assertOrderStatus(int status) {
        ResponseValidator.assertStatusCode(ctx.getResponse(), status);
    }

    @Then("the order list should not be empty")
    public void assertOrderListNotEmpty() {
        ResponseValidator.assertListNotEmpty(ctx.getResponse());
    }

    @Then("the order response field {string} should be present")
    public void assertOrderFieldPresent(String jsonPath) {
        ResponseValidator.assertFieldNotNull(ctx.getResponse(), jsonPath);
    }

    @Then("the created order id should be present")
    public void assertCreatedOrderIdPresent() {
        ResponseValidator.assertFieldNotNull(ctx.getResponse(), "id");
    }

    @Then("the order user id should be {int}")
    public void assertOrderUserId(int expectedUserId) {
        ResponseValidator.assertFieldEquals(ctx.getResponse(), "userId", expectedUserId);
    }
}
