package com.framework.api.auth;

import com.framework.api.config.ApiConfig;
import com.framework.api.services.AuthService;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * JVM-scoped bearer token cache. Generates the token once on first call and
 * serves it to all parallel threads without re-authenticating.
 *
 * Proactive refresh: getToken() checks whether the token is within
 * api.auth.token.buffer.seconds of its expiry and re-fetches before it actually
 * expires, so no in-flight request ever receives a stale token.
 *
 * Expiry resolution order:
 *   1. "expires_in" field from the login response (seconds from now)
 *   2. api.auth.token.ttl.seconds from the environment properties (fallback)
 *
 * Thread safety: double-checked locking on volatile fields — only the
 * generating thread pays the synchronized cost; all others are lock-free.
 */
public final class TokenManager {

    private static final Logger log = LoggerFactory.getLogger(TokenManager.class);

    private static volatile String cachedToken;
    private static volatile Instant tokenExpiry = Instant.MIN;
    private static final Object LOCK = new Object();

    private TokenManager() {}

    public static String getToken() {
        if (isStale()) {
            synchronized (LOCK) {
                if (isStale()) {
                    fetchAndCache();
                }
            }
        }
        return cachedToken;
    }

    /**
     * Forces the next getToken() call to re-authenticate regardless of expiry.
     */
    public static void invalidate() {
        synchronized (LOCK) {
            cachedToken = null;
            tokenExpiry = Instant.MIN;
        }
    }

    private static boolean isStale() {
        int bufferSeconds = ApiConfig.getInt("api.auth.token.buffer.seconds", 30);
        return cachedToken == null || Instant.now().isAfter(tokenExpiry.minusSeconds(bufferSeconds));
    }

    private static void fetchAndCache() {
        String email = ApiConfig.get("api.auth.email");
        String password = ApiConfig.get("api.auth.password");

        log.info("[TokenManager] Fetching bearer token for environment: {}", ApiConfig.getEnvironment());

        Response response = new AuthService().login(email, password);
        String token = response.jsonPath().getString("token");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                "[TokenManager] Login did not return a token. Status: "
                    + response.statusCode() + " | Body: " + response.asString()
            );
        }

        cachedToken = token;
        tokenExpiry = resolveExpiry(response);

        log.info("[TokenManager] Token cached. Expires at: {} (buffer: {}s)",
            tokenExpiry, ApiConfig.getInt("api.auth.token.buffer.seconds", 30));
    }

    /**
     * Reads "expires_in" (seconds) from the response if present; otherwise falls
     * back to api.auth.token.ttl.seconds from config (default 3600).
     */
    private static Instant resolveExpiry(Response response) {
        Integer expiresIn = response.jsonPath().get("expires_in");
        if (expiresIn != null && expiresIn > 0) {
            return Instant.now().plusSeconds(expiresIn);
        }
        int ttl = ApiConfig.getInt("api.auth.token.ttl.seconds", 3600);
        return Instant.now().plusSeconds(ttl);
    }
}
