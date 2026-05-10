package com.framework.api.steps;

import com.framework.api.core.ApiScenarioContext;
import com.framework.api.services.ProductService;
import com.framework.api.utils.ResponseValidator;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.Map;

public class ProductSteps {

    private final ApiScenarioContext ctx;
    private final ProductService productService;

    public ProductSteps(ApiScenarioContext ctx) {
        this.ctx = ctx;
        this.productService = new ProductService();
    }

    @When("I request all products")
    public void getAllProducts() {
        ctx.setResponse(productService.getAllProducts());
    }

    @When("I request product with id {int}")
    public void getProductById(int productId) {
        ctx.setResponse(productService.getProductById(productId));
    }

    @When("I request products in category {string}")
    public void getProductsByCategory(String category) {
        ctx.setResponse(productService.getProductsByCategory(category));
    }

    @When("I request all product categories")
    public void getAllCategories() {
        ctx.setResponse(productService.getAllCategories());
    }

    @When("I create a product with title {string} and price {double}")
    public void createProduct(String title, double price) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("price", price);
        payload.put("description", "Test product created by automation");
        payload.put("image", "https://fakestoreapi.com/img/dummy.jpg");
        payload.put("category", "electronics");
        ctx.setResponse(productService.createProduct(payload));
    }

    @When("I delete product with id {int}")
    public void deleteProduct(int productId) {
        ctx.setResponse(productService.deleteProduct(productId));
    }

    @Then("the product response status should be {int}")
    public void assertProductStatus(int status) {
        ResponseValidator.assertStatusCode(ctx.getResponse(), status);
    }

    @Then("the product list should not be empty")
    public void assertProductListNotEmpty() {
        ResponseValidator.assertListNotEmpty(ctx.getResponse());
    }

    @Then("the product response field {string} should be present")
    public void assertProductFieldPresent(String jsonPath) {
        ResponseValidator.assertFieldNotNull(ctx.getResponse(), jsonPath);
    }

    @Then("the created product id should be present")
    public void assertCreatedProductIdPresent() {
        ResponseValidator.assertFieldNotNull(ctx.getResponse(), "id");
    }
}
