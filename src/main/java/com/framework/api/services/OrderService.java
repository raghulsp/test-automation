package com.framework.api.services;

import com.framework.api.core.BaseApiService;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Service Object for the Order Management Service.
 * Base URL resolves from api.base.url.order in the active environment config.
 * Backed by jsonplaceholder.typicode.com in the QA environment.
 */
public class OrderService extends BaseApiService {

    public OrderService() {
        super("order");
    }

    public Response getAllOrders() {
        return doGet("/todos");
    }

    public Response getOrderById(int orderId) {
        return doGet("/todos/" + orderId);
    }

    public Response getOrdersByUser(int userId) {
        return doGet("/todos?userId=" + userId);
    }

    public Response createOrder(Map<String, Object> payload) {
        return doPost("/todos", payload);
    }

    public Response updateOrder(int orderId, Map<String, Object> payload) {
        return doPut("/todos/" + orderId, payload);
    }

    public Response patchOrder(int orderId, Map<String, Object> payload) {
        return doPatch("/todos/" + orderId, payload);
    }

    public Response deleteOrder(int orderId) {
        return doDelete("/todos/" + orderId);
    }
}
