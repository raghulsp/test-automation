package com.framework.api.services;

import com.framework.api.core.BaseApiService;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Service Object for the Product Catalog Service.
 * Base URL resolves from api.base.url.product in the active environment config.
 * Backed by fakestoreapi.com in the QA environment.
 */
public class ProductService extends BaseApiService {

    public ProductService() {
        super("product");
    }

    public Response getAllProducts() {
        return doGet("/products");
    }

    public Response getAllProducts(int limit) {
        return doGet("/products?limit=" + limit);
    }

    public Response getProductById(int productId) {
        return doGet("/products/" + productId);
    }

    public Response getProductsByCategory(String category) {
        return doGet("/products/category/" + category);
    }

    public Response getAllCategories() {
        return doGet("/products/categories");
    }

    public Response createProduct(Map<String, Object> payload) {
        return doPost("/products", payload);
    }

    public Response updateProduct(int productId, Map<String, Object> payload) {
        return doPut("/products/" + productId, payload);
    }

    public Response patchProduct(int productId, Map<String, Object> payload) {
        return doPatch("/products/" + productId, payload);
    }

    public Response deleteProduct(int productId) {
        return doDelete("/products/" + productId);
    }
}
