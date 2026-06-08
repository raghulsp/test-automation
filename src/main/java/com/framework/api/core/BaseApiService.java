package com.framework.api.core;

import com.framework.api.auth.TokenManager;
import com.framework.api.config.ApiConfig;
import com.framework.api.utils.ApiRequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public abstract class BaseApiService {

    protected final String serviceName;
    protected final RequestSpecification baseSpec;

    protected BaseApiService(String serviceName) {
        this.serviceName = serviceName;
        this.baseSpec = buildBaseSpec();
    }

    private RequestSpecification buildBaseSpec() {
        return commonSpecBuilder().build();
    }

    /**
     * Returns a spec identical to baseSpec but with the shared bearer token attached.
     * Token is generated once per JVM by TokenManager (lock-free after the first call).
     * Use this for endpoints that require authentication.
     */
    protected RequestSpecification authSpec() {
        return commonSpecBuilder()
            .withBearerToken(TokenManager.getToken())
            .build();
    }

    private ApiRequestSpecBuilder commonSpecBuilder() {
        return new ApiRequestSpecBuilder()
            .withBaseUri(serviceName)
            .withRelaxedHttps()
            .withTimeout(
                ApiConfig.getInt("api.connection.timeout", 10000),
                ApiConfig.getInt("api.socket.timeout", 30000)
            );
    }

    protected Response doGet(String path) {
        return given().spec(baseSpec).when().get(path);
    }

    protected Response doGet(String path, RequestSpecification overrideSpec) {
        return given().spec(overrideSpec).when().get(path);
    }

    protected Response doPost(String path, Object body) {
        return given().spec(baseSpec).body(body).when().post(path);
    }

    protected Response doPost(String path, Object body, RequestSpecification overrideSpec) {
        return given().spec(overrideSpec).body(body).when().post(path);
    }

    protected Response doPut(String path, Object body) {
        return given().spec(baseSpec).body(body).when().put(path);
    }

    protected Response doPatch(String path, Object body) {
        return given().spec(baseSpec).body(body).when().patch(path);
    }

    protected Response doDelete(String path) {
        return given().spec(baseSpec).when().delete(path);
    }
}
