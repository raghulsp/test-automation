package com.framework.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-local key-value store that lives for the duration of a single Cucumber scenario.
 * Used to pass data between step definitions (e.g. API-created credentials → UI login steps).
 * Must be cleared in {@code @After} hooks to prevent cross-scenario leakage.
 */
public class ScenarioContext {

    private static final ThreadLocal<Map<String, Object>> ctx =
            ThreadLocal.withInitial(HashMap::new);

    private ScenarioContext() {}

    public static void set(String key, Object value) {
        ctx.get().put(key, value);
    }

    public static <T> T get(String key, Class<T> type) {
        return type.cast(ctx.get().get(key));
    }

    public static boolean has(String key) {
        return ctx.get().containsKey(key);
    }

    /** Must be called from {@code @After} to release the ThreadLocal map. */
    public static void clear() {
        ctx.remove();
    }
}
