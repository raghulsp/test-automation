package com.framework.api.mock;

import com.github.tomakehurst.wiremock.matching.UrlPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Reusable WireMock stub patterns.
 * All methods assume MockServerManager.start() has already been called (done by MockApiHooks).
 *
 * URL matching uses exact path equality by default.
 * Use stubGetContaining / stubPostContaining for partial path matching.
 */
public class StubFactory {

    private static final Logger log = LoggerFactory.getLogger(StubFactory.class);
    private static final String JSON = "application/json";

    // -------------------------------------------------------------------------
    // GET stubs
    // -------------------------------------------------------------------------

    public static void stubGet(String url, int status, String bodyJson) {
        log.debug("[WireMock] Stubbing GET {} → {}", url, status);
        stubFor(get(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(status)
                .withHeader("Content-Type", JSON)
                .withBody(bodyJson)));
    }

    public static void stubGetContaining(String urlSubstring, int status, String bodyJson) {
        stubFor(get(urlPathContaining(urlSubstring))
            .willReturn(aResponse()
                .withStatus(status)
                .withHeader("Content-Type", JSON)
                .withBody(bodyJson)));
    }

    // -------------------------------------------------------------------------
    // POST stubs
    // -------------------------------------------------------------------------

    public static void stubPost(String url, int status, String bodyJson) {
        log.debug("[WireMock] Stubbing POST {} → {}", url, status);
        stubFor(post(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(status)
                .withHeader("Content-Type", JSON)
                .withBody(bodyJson)));
    }

    public static void stubPostWithRequestBody(String url, String requestBodyFragment, int status, String responseBody) {
        stubFor(post(urlEqualTo(url))
            .withRequestBody(containing(requestBodyFragment))
            .willReturn(aResponse()
                .withStatus(status)
                .withHeader("Content-Type", JSON)
                .withBody(responseBody)));
    }

    // -------------------------------------------------------------------------
    // PUT / PATCH / DELETE stubs
    // -------------------------------------------------------------------------

    public static void stubPut(String url, int status, String bodyJson) {
        stubFor(put(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(status)
                .withHeader("Content-Type", JSON)
                .withBody(bodyJson)));
    }

    public static void stubPatch(String url, int status, String bodyJson) {
        stubFor(patch(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(status)
                .withHeader("Content-Type", JSON)
                .withBody(bodyJson)));
    }

    public static void stubDelete(String url, int status) {
        stubFor(delete(urlEqualTo(url))
            .willReturn(aResponse().withStatus(status)));
    }

    // -------------------------------------------------------------------------
    // Error / fault stubs
    // -------------------------------------------------------------------------

    public static void stubServerError(String url) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", JSON)
                .withBody("{\"error\":\"Internal Server Error\"}")));
    }

    public static void stubUnauthorized(String url) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(401)
                .withHeader("Content-Type", JSON)
                .withBody("{\"error\":\"Unauthorized\"}")));
    }

    public static void stubNotFound(String url) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", JSON)
                .withBody("{\"error\":\"Not Found\"}")));
    }

    /**
     * Simulates a slow response. Use with a generous socket timeout in mock.properties
     * or set socket timeout lower than the delay to test timeout-handling code paths.
     */
    public static void stubTimeout(String url, int delayMs) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(delayMs)));
    }

    /**
     * Simulates a connection reset — useful for testing retry logic.
     */
    public static void stubConnectionReset(String url) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)));
    }
}
