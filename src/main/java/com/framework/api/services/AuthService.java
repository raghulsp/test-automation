package com.framework.api.services;

import com.framework.api.ApiClient;
import com.framework.models.UserCredentials;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Handles authentication-related API operations (login, token refresh, logout).
 *
 * <p>The {@link #getAuthToken} helper is particularly useful for service-level tests
 * that need a bearer token before calling protected endpoints.
 */
public class AuthService {

    private final ApiClient client;

    public AuthService() {
        this.client = new ApiClient();
    }

    /**
     * Sends login credentials and returns the raw response for assertion.
     */
    public Response login(UserCredentials credentials) {
        Map<String, String> body = Map.of(
                "email",    credentials.getUsername(),
                "password", credentials.getPassword()
        );
        return client.post("/api/login", body);
    }

    /**
     * Convenience method: authenticates and extracts the token string.
     * Fails the test immediately if the login call does not return HTTP 200.
     */
    public String getAuthToken(UserCredentials credentials) {
        Response response = login(credentials);
        response.then().statusCode(200);
        return response.jsonPath().getString("token");
    }
}
