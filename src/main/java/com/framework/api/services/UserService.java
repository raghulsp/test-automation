package com.framework.api.services;

import com.framework.api.core.BaseApiService;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Service Object for the User Service.
 * Base URL resolves from api.base.url.user in the active environment config.
 * Backed by reqres.in in the QA environment.
 */
public class UserService extends BaseApiService {

    public UserService() {
        super("user");
    }

    public Response getAllUsers(int page) {
        return doGet("/users?page=" + page);
    }

    public Response getUserById(int userId) {
        return doGet("/users/" + userId);
    }

    public Response createUser(Map<String, Object> payload) {
        return doPost("/users", payload);
    }

    public Response updateUser(int userId, Map<String, Object> payload) {
        return doPut("/users/" + userId, payload);
    }

    public Response patchUser(int userId, Map<String, Object> payload) {
        return doPatch("/users/" + userId, payload);
    }

    public Response deleteUser(int userId) {
        return doDelete("/users/" + userId);
    }
}
