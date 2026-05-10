package com.framework.api.utils;

import com.framework.api.config.ApiConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class ApiRequestSpecBuilder {

    private final RequestSpecBuilder builder;
    private RestAssuredConfig config = RestAssuredConfig.config();

    public ApiRequestSpecBuilder() {
        builder = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .log(LogDetail.ALL);
    }

    public ApiRequestSpecBuilder withBaseUri(String service) {
        builder.setBaseUri(ApiConfig.getBaseUrl(service));
        return this;
    }

    public ApiRequestSpecBuilder withLiteralBaseUri(String uri) {
        builder.setBaseUri(uri);
        return this;
    }

    public ApiRequestSpecBuilder withBasePath(String path) {
        builder.setBasePath(path);
        return this;
    }

    public ApiRequestSpecBuilder withBearerToken(String token) {
        builder.addHeader("Authorization", "Bearer " + token);
        return this;
    }

    public ApiRequestSpecBuilder withApiKey(String headerName, String key) {
        builder.addHeader(headerName, key);
        return this;
    }

    public ApiRequestSpecBuilder withHeader(String name, String value) {
        builder.addHeader(name, value);
        return this;
    }

    public ApiRequestSpecBuilder withHeaders(Map<String, String> headers) {
        headers.forEach(builder::addHeader);
        return this;
    }

    public ApiRequestSpecBuilder withQueryParam(String name, Object value) {
        builder.addQueryParam(name, value);
        return this;
    }

    public ApiRequestSpecBuilder withRelaxedHttps() {
        builder.setRelaxedHTTPSValidation();
        return this;
    }

    public ApiRequestSpecBuilder withTimeout(int connectionTimeoutMs, int socketTimeoutMs) {
        config = config.httpClient(
            HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", connectionTimeoutMs)
                .setParam("http.socket.timeout", socketTimeoutMs)
        );
        builder.setConfig(config);
        return this;
    }

    public RequestSpecification build() {
        return builder.build();
    }
}
