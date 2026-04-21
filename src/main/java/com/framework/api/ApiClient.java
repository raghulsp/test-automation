package com.framework.api;

import com.framework.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Thin wrapper around REST Assured that centralises base-URI, content-type,
 * and logging so individual service classes stay focused on business logic.
 *
 * <p>Two constructors are provided:
 * <ul>
 *   <li>No-arg — for unauthenticated requests or calls that manage auth at a higher level.</li>
 *   <li>Bearer-token — injects {@code Authorization: Bearer <token>} on every request.</li>
 * </ul>
 */
public class ApiClient {

    private final RequestSpecification spec;

    public ApiClient() {
        spec = buildSpec(null);
    }

    public ApiClient(String bearerToken) {
        spec = buildSpec(bearerToken);
    }

    private RequestSpecification buildSpec(String bearerToken) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(ConfigManager.get("api.base.url", "https://reqres.in"))
                .setContentType(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter());

        if (bearerToken != null && !bearerToken.isBlank()) {
            builder.addHeader("Authorization", "Bearer " + bearerToken);
        }
        return builder.build();
    }

    public Response get(String path) {
        return RestAssured.given(spec).get(path);
    }

    public Response post(String path, Object body) {
        return RestAssured.given(spec).body(body).post(path);
    }

    public Response put(String path, Object body) {
        return RestAssured.given(spec).body(body).put(path);
    }

    public Response patch(String path, Object body) {
        return RestAssured.given(spec).body(body).patch(path);
    }

    public Response delete(String path) {
        return RestAssured.given(spec).delete(path);
    }
}
