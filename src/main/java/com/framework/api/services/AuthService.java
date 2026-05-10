package com.framework.api.services;

import com.framework.api.core.BaseApiService;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Service Object for the Auth Service (login / register flows).
 * Base URL resolves from api.base.url.auth in the active environment config.
 * Backed by reqres.in in the QA environment.
 */
public class AuthService extends BaseApiService {

    public AuthService() {
        super("auth");
    }

    public Response login(String email, String password) {
        return doPost("/login", Map.of("email", email, "password", password));
    }

    public Response loginWithPayload(Map<String, Object> payload) {
        return doPost("/login", payload);
    }

    public Response register(String email, String password) {
        return doPost("/register", Map.of("email", email, "password", password));
    }

    public Response registerWithPayload(Map<String, Object> payload) {
        return doPost("/register", payload);
    }
}
