# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run all tests (smoke + regression in parallel) and generate Masterthought report
mvn clean verify

# Run tests only (no Masterthought report)
mvn clean test

# Run with a specific platform
mvn clean verify -Dplatform=web
mvn clean verify -Dplatform=android
mvn clean verify -Dplatform=ios

# Run a specific browser (web only)
mvn clean verify -Dbrowser=firefox
mvn clean verify -Dheadless=true

# Run a single suite via TestRunner (bypasses testng.xml runner split)
mvn clean verify -Dcucumber.filter.tags="@smoke"
mvn clean verify -Dcucumber.filter.tags="@regression"
mvn clean verify -Dcucumber.filter.tags="not @wip"

# Override device at runtime
mvn clean verify -Ddevice.name=emulator-5554 -Dplatform.version=14
```

**Reports:**
- UI Smoke HTML: `target/cucumber-reports/smoke-report.html`
- UI Regression HTML: `target/cucumber-reports/regression-report.html`
- API Smoke HTML: `target/cucumber-reports/api-smoke-report.html`
- API Regression HTML: `target/cucumber-reports/api-regression-report.html`
- Masterthought (all four combined): `target/masterthought-reports/cucumber-html-reports/overview-features.html`

```bash
# Run only API tests against QA environment (default)
mvn clean verify -Dcucumber.filter.tags="@api-smoke or @api-regression"

# Run API tests against staging
mvn clean verify -Dapi.env=staging

# Run a single API service suite
mvn clean verify -Dcucumber.filter.tags="@user-api"
mvn clean verify -Dcucumber.filter.tags="@auth-api"
mvn clean verify -Dcucumber.filter.tags="@product-api"
mvn clean verify -Dcucumber.filter.tags="@order-api"
```

## Architecture

**Stack:** Java 17 · Selenium 4 · Appium Server 2 (Java Client 9) · Cucumber 7 · TestNG 7 · WebDriverManager · Jackson 2.17 · REST Assured 5.4

### Execution flow

```
testng.xml  (parallel="tests", thread-count=4)
  ├── UI Smoke Tests      → SmokeTestRunner      (@smoke tags)
  ├── UI Regression Tests → RegressionTestRunner  (@regression tags)
  ├── API Smoke Tests     → ApiSmokeRunner        (@api-smoke tags)
  └── API Regression      → ApiRegressionRunner   (@api-regression tags)
        │
        └── UI runners (parallel=true DataProvider)
              └── Hooks (@Before / @After)
                    ├── @Before → DriverFactory.createDriver() → DriverManager.setDriver()
                    └── @After  → screenshot on fail → DriverManager.quitDriver() → DeviceManager.releaseDevice()
              └── Step definitions → Page objects via LoginPageFactory
        │
        └── API runners (parallel=true DataProvider)
              └── ApiHooks (@Before / @After tagged @api-smoke or @api-regression)
              └── Step definitions → Service objects (UserService, AuthService, etc.)
                    └── ApiScenarioContext injected via PicoContainer (shared state between steps)
```

**Parallelism levels:**
- **Runner level:** `parallel="tests"` in `testng.xml` — all 4 suites run concurrently (thread-count=4).
- **Scenario level:** `@DataProvider(parallel=true)` in each runner — scenarios within a suite run concurrently.
- **Device pool (UI):** shared `ConcurrentLinkedQueue` across all threads; threads queue when no device is free.
- **API tests:** stateless REST calls — fully parallel with no shared mutable state; `ApiScenarioContext` is scenario-scoped (PicoContainer creates one per scenario).

### Platform switching

Platform is **per-thread from the device pool** — NOT from `config.properties`. This is critical for mixed-platform parallel execution.

- `devices.json` — single source of truth for all devices (web + mobile). Each entry has `udid`, `platform`, `platformVersion`, `appiumUrl`, `browser`.
- `DeviceManager` — loads `devices.json` into a `ConcurrentLinkedQueue` pool at JVM startup. Each thread calls `acquireDevice()` before test and `releaseDevice()` after.
- `DriverFactory.createDriver()` — acquires device first, then branches on `device.getPlatform()` to create the right driver.
- `LoginPageFactory` — reads `DeviceManager.currentDevice().getPlatform()` to return web or mobile page impl.
- `BasePage.platform()` — reads `DeviceManager.currentDevice().getPlatform()`.

**`thread-count` in `testng.xml` controls runner-level concurrency** (currently 2 — one per suite). `data-provider-thread-count` controls scenario-level concurrency within each runner. The device pool serves all threads; add devices to `devices.json` to increase throughput.

### Device pool parallel execution

Devices are reusable slots, not 1-to-1 with tests. 5 devices running 10 tests = 2 rounds of continuous pipeline execution. `ConcurrentLinkedQueue` is thread-safe — a freed device is immediately available to the next waiting thread.

### Page object pattern

- `BasePage` (`src/main/java`) — shared helpers (`click`, `type`, `find`, `open`, `platform()`, `isDisplayedSafely(locator, seconds)`). All page classes extend this. `isDisplayedSafely` catches `TimeoutException` and returns `false` — use it when you don't know which of two elements will appear (e.g. success vs error). Also exposes protected wrappers for all utility methods (tap, swipe, notification) so page objects never import utils directly.
- `ILoginPage` — interface defining the page contract. Step definitions depend on this interface only. Key method: `getVisibleMessage()` returns whichever message is currently on screen (success greeting or error text) — the page object owns this branching logic, not the step.
- `pages/web/LoginPage` — Selenium `By` locators (CSS/ID). Amazon login is a **2-step flow**: enter email → click Continue → enter password → click Sign-In. `getVisibleMessage()` waits up to 8 s for the "Hello" greeting; if absent, falls back to `.a-alert-content` error text.
- `pages/mobile/LoginPage` — `AppiumBy` locators. Locators are **methods not fields** so `platform()` resolves at call time for Android vs iOS.
- `LoginPageFactory` — returns correct implementation based on current thread's device platform.

### Utility classes (`src/main/java/com/framework/utils/`)

| Class | Platform | What it provides |
|---|---|---|
| `WaitUtils` | Any | `waitForVisible`, `waitForClickable` with configurable timeout |
| `SwipeUtils` | Mobile | `swipeUp/Down/Left/Right`, `swipeUntilVisible`, `androidScrollToText*`, `iosScrollToText*` |
| `TapUtils` | Mixed | Locate-then-tap helpers by XPath, ID, AccessibilityId (any); UIAutomator text/textContains/resourceId (Android); predicateString/classChain (iOS) |
| `NotificationUtils` | Android | `openNotificationTray()`, `tapNotificationByText()`, `tapNotificationByTextContains()`, `tapNotificationByUiSelector()` |

All utility methods are exposed as `protected` wrappers on `BasePage`. Platform-restricted methods throw `UnsupportedOperationException` when called on the wrong platform.

### Adding a new page

1. Add interface in `src/main/java/com/framework/pages/`.
2. Create `web/` and `mobile/` implementations extending `BasePage`.
3. Add factory class following `LoginPageFactory` pattern.
4. Step definitions use the interface type only.

### Adding a new device

Add one entry to `src/test/resources/devices.json`. No Java changes needed. Increment `data-provider-thread-count` in `testng.xml` if you want more concurrent scenarios within a runner.

### Test suite tagging convention

| Tag | Runner | Feature files |
|---|---|---|
| `@smoke` | `SmokeTestRunner` | `features/smoke/`, UI login happy path |
| `@regression` | `RegressionTestRunner` | `features/regression/`, UI login negative path |
| `@api-smoke` | `ApiSmokeRunner` | `features/api/smoke/`, one happy-path per service |
| `@api-regression` | `ApiRegressionRunner` | `features/api/regression/`, full CRUD + negatives |
| `@user-api` | sub-tag | user service scenarios |
| `@auth-api` | sub-tag | auth service scenarios |
| `@product-api` | sub-tag | product service scenarios |
| `@order-api` | sub-tag | order service scenarios |
| `@wip` | excluded from all runners | in-progress scenarios, not run in CI |

- `TestRunner` (kept for local ad-hoc use) runs `not @wip` — all UI scenarios in one shot.
- Dummy step definitions for non-login UI features live in `steps/DummySteps.java`.

## API Testing Module

### Architecture (Service Object Model)

Mirrors POM: each API service has its own class that encapsulates all calls to that endpoint. Step definitions depend only on the service interface, not raw HTTP.

```
src/main/java/com/framework/api/
├── config/
│   ├── Environment.java         — enum DEV | QA | STAGING | PROD
│   └── ApiConfig.java           — loads api/environments/<env>.properties; getBaseUrl(service)
├── core/
│   ├── BaseApiService.java      — doGet/doPost/doPut/doPatch/doDelete with shared RequestSpec
│   └── ApiScenarioContext.java  — PicoContainer shared state (lastResponse, key-value store)
├── services/
│   ├── UserService.java         — /users CRUD  (QA: reqres.in)
│   ├── AuthService.java         — /login /register  (QA: reqres.in)
│   ├── ProductService.java      — /products CRUD + category filter  (QA: fakestoreapi.com)
│   └── OrderService.java        — /todos CRUD + user filter  (QA: jsonplaceholder.typicode.com)
└── utils/
    ├── ApiRequestSpecBuilder.java — fluent builder: baseUri, auth, headers, timeout, relaxed HTTPS
    ├── ResponseValidator.java     — static assertions wrapping TestNG Assert
    └── ApiLogger.java             — SLF4J log helpers for request/response

src/test/java/com/framework/api/
├── hooks/ApiHooks.java            — @Before/@After for @api-smoke and @api-regression tags
├── runners/
│   ├── ApiSmokeRunner.java
│   └── ApiRegressionRunner.java
└── steps/
    ├── UserSteps.java
    ├── AuthSteps.java
    ├── ProductSteps.java
    └── OrderSteps.java

src/test/resources/
├── api/environments/
│   ├── qa.properties      — uses public stub APIs (reqres.in, fakestoreapi.com, jsonplaceholder)
│   ├── dev.properties     — placeholder URLs for internal dev environment
│   └── staging.properties — placeholder URLs for staging environment
└── features/api/
    ├── smoke/api-smoke.feature
    └── regression/
        ├── user-service.feature
        ├── auth-service.feature
        ├── product-service.feature
        └── order-service.feature
```

### Environment switching

Active environment is read from `-Dapi.env=<dev|qa|staging|prod>` (default: `qa`). Each environment has its own properties file under `src/test/resources/api/environments/`. Swap environment without changing code:

```bash
mvn clean verify -Dapi.env=staging
```

### Adding a new API service

1. Add a new service class in `src/main/java/com/framework/api/services/` extending `BaseApiService`.
2. Pass the service name key to `super("myservice")` — it resolves `api.base.url.myservice` from env config.
3. Add step definitions in `src/test/java/com/framework/api/steps/`, injecting `ApiScenarioContext` via constructor.
4. Add feature files in `src/test/resources/features/api/smoke/` and `features/api/regression/`.
5. Add `api.base.url.myservice=<url>` to each environment properties file.

## Key config properties (`src/test/resources/config.properties`)

| Property | Default | Purpose |
|---|---|---|
| `platform` | `web` | Fallback only — runtime platform comes from `devices.json` |
| `browser` | `chrome` | Fallback only — browser comes from `devices.json` `browser` field |
| `headless` | `false` | Web only |
| `base.url` | Amazon India (`https://www.amazon.in`) | URL opened in web scenarios |
| `appium.url` | `http://127.0.0.1:4723` | Fallback — each device has its own in `devices.json` |
| `implicit.wait` | `10` | Seconds for implicit waits |

All properties can be overridden at runtime with `-D` flags.

## Build plugins

- **Surefire** — runs TestNG tests in `test` phase. `testFailureIgnore=true` ensures build continues to `verify` phase even when tests fail.
- **Masterthought** (`maven-cucumber-reporting`) — runs in `verify` phase, reads `smoke-report.json` + `regression-report.json` from `target/cucumber-reports/`, generates combined rich HTML report. Always use `mvn clean verify` to get the report.

## Appium setup (for mobile)

Each device needs its own Appium server instance on a separate port:
```bash
appium --port 4723 --udid emulator-5554
appium --port 4724 --udid emulator-5556
appium --port 4725 --udid iPhone14-UDID
```

`UiAutomator2Options` used for Android, `XCUITestOptions` for iOS — both are Appium 2.x standard, replacing deprecated `DesiredCapabilities`.

### Android app launch — non-exported activity workaround

Amazon's `MainActivity` is not exported (`android:exported="false"`), so `adb shell am start-activity -n package/activity` is blocked by Android. `DriverFactory.createAndroidDriver()` works around this:

1. `setAppPackage(pkg)` — sets the package only; **no** `setAppActivity` call.
2. `setCapability("appium:autoLaunch", false)` — prevents Appium from running `am start-activity` during session init.
3. `driver.activateApp(pkg)` — called immediately after session creation; uses `adb shell monkey -p <pkg> -c android.intent.category.LAUNCHER 1`, which the OS permits regardless of exported status.

Apply the same pattern for any app whose launcher activity is not exported.

## Known pending items

- `src/test/java/com/framework/base/BasePage.java` and `src/test/java/com/framework/pages/LoginPage.java` are leftover duplicates — delete both
- `mobile/LoginPage` locators use placeholder `accessibilityId` values — replace with real Amazon app locators
- Amazon app package: `in.amazon.mShop.android.shopping` — no `appActivity` needed (see non-exported activity workaround above)
- ExtentReports dependency in `pom.xml` not yet integrated
