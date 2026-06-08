package com.framework.api.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * JVM-scoped WireMock server — started once on first use, stopped on JVM exit.
 * Per-scenario isolation is achieved by calling reset() in @After, which clears
 * all stubs without stopping the server.
 *
 * Thread safety: start() uses double-checked locking; it is safe to call from
 * multiple threads but the stub registry is shared — run mock scenarios with
 * data-provider-thread-count=1 to avoid cross-scenario stub interference.
 */
public class MockServerManager {

    private static final Logger log = LoggerFactory.getLogger(MockServerManager.class);

    public static final int PORT = 9999;
    public static final String BASE_URL = "http://localhost:" + PORT;

    private static volatile WireMockServer server;

    public static void start() {
        if (server == null || !server.isRunning()) {
            synchronized (MockServerManager.class) {
                if (server == null || !server.isRunning()) {
                    server = new WireMockServer(options().port(PORT));
                    server.start();
                    WireMock.configureFor("localhost", PORT);
                    registerShutdownHook();
                    log.info("[WireMock] Server started on port {}", PORT);
                }
            }
        }
    }

    /** Clears all stubs and recorded requests — call between scenarios. */
    public static void reset() {
        if (server != null && server.isRunning()) {
            server.resetAll();
            log.debug("[WireMock] Stubs reset");
        }
    }

    public static void stop() {
        if (server != null && server.isRunning()) {
            server.stop();
            log.info("[WireMock] Server stopped");
        }
    }

    public static boolean isRunning() {
        return server != null && server.isRunning();
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(MockServerManager::stop, "wiremock-shutdown"));
    }
}
