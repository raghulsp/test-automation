package com.framework.api.services;

import com.framework.api.ApiClient;
import com.framework.models.UserCredentials;
import com.framework.models.UserRequest;
import io.restassured.response.Response;

/**
 * Encapsulates all user-related API operations.
 *
 * <p>Used in two roles:
 * <ol>
 *   <li><b>Test setup</b> — {@link #createAndReturnCredentials} creates a user and hands back
 *       {@link UserCredentials} that UI login steps can consume via {@code ScenarioContext}.</li>
 *   <li><b>Standalone API tests</b> — the raw {@link Response}-returning methods let step
 *       definitions assert on status codes, response bodies, etc.</li>
 * </ol>
 *
 * <p><b>Adapt to your real API:</b> The response-parsing inside
 * {@link #createAndReturnCredentials} assumes a typical REST API that echoes back the email
 * or username in the response body. Adjust the {@code jsonPath} expression to match your
 * actual API contract.
 */
public class UserService {

    private final ApiClient client;

    public UserService() {
        this.client = new ApiClient();
    }

    public UserService(String bearerToken) {
        this.client = new ApiClient(bearerToken);
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public Response createUser(UserRequest request) {
        return client.post("/api/users", request);
    }

    public Response getUserById(int userId) {
        return client.get("/api/users/" + userId);
    }

    public Response updateUser(int userId, UserRequest request) {
        return client.put("/api/users/" + userId, request);
    }

    public Response patchUser(int userId, UserRequest request) {
        return client.patch("/api/users/" + userId, request);
    }

    public Response deleteUser(int userId) {
        return client.delete("/api/users/" + userId);
    }

    // ── Setup helper ─────────────────────────────────────────────────────────

    /**
     * Creates a user via the API and returns credentials suitable for UI login.
     *
     * @param email    email / username to register
     * @param password plain-text password (sent only over HTTPS in real environments)
     * @return {@link UserCredentials} extracted from the API response
     */
    public UserCredentials createAndReturnCredentials(String email, String password) {
        UserRequest request = UserRequest.builder()
                .email(email)
                .password(password)
                .name("Test User")
                .job("qa-automation")
                .build();

        Response response = createUser(request);
        response.then().statusCode(201);

        // Parse username from response — adapt this to your API's actual field name
        String returnedEmail = response.jsonPath().getString("email");
        String username = (returnedEmail != null) ? returnedEmail : email;

        return new UserCredentials(username, password);
    }
}
