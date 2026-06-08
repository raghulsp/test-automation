# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run all tests (UI smoke + UI regression + API smoke + API regression) and generate Masterthought report
mvn clean verify

# Run tests only (no Masterthought report)
mvn clean test

# Run with a specific platform
mvn clean verify -Dplatform=web
mvn clean verify -Dplatform=android
mvn clean verify -Dplatform=ios
mvn clean verify -Dplatform=androidtv

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

**Maven profiles** — use `-P<profile>` to target a specific suite with pre-tuned thread counts:

```bash
# Run only UI suites
mvn clean verify -Pui              # smoke + regression together
mvn clean verify -Pui-smoke        # UI smoke only
mvn clean verify -Pui-regression   # UI regression only

# Run only API suites
mvn clean verify -Papi             # smoke + regression together
mvn clean verify -Papi-smoke       # API smoke only (thread-count=2, data-provider-thread-count=4)
mvn clean verify -Papi-regression  # API regression only (thread-count=2, data-provider-thread-count=4)

# Run API tests against staging
mvn clean verify -Papi -Dapi.env=staging

# Run mock API tests (offline, no real network calls)
mvn clean verify -Pmock -Dapi.env=mock

# Run a single API service suite
mvn clean verify -Dcucumber.filter.tags="@user-api"
mvn clean verify -Dcucumber.filter.tags="@auth-api"
mvn clean verify -Dcucumber.filter.tags="@product-api"
mvn clean verify -Dcucumber.filter.tags="@order-api"
```

**Reports:**
- UI Smoke HTML: `target/cucumber-reports/smoke-report.html`
- UI Regression HTML: `target/cucumber-reports/regression-report.html`
- API Smoke HTML: `target/cucumber-reports/api-smoke-report.html`
- API Regression HTML: `target/cucumber-reports/api-regression-report.html`
- Masterthought (all four combined): `target/masterthought-reports/cucumber-html-reports/overview-features.html`

## Architecture

**Stack:** Java 17 · Selenium 4.18 · Appium Server 2 (Java Client 9.1) · Cucumber 7.15 · TestNG 7.9 · WebDriverManager 5.7 · Jackson 2.17 · REST Assured 5.4 · WireMock 3.5 · H2 2.2

### Execution flow

```
testng.xml  (parallel="tests", thread-count=${thread.count} — default 1, overridden per profile)
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
- **Runner level:** `parallel="tests"` in `testng.xml` — suites run concurrently; `thread-count` defaults to `1` in `pom.xml` and is overridden per profile (API profiles use `2`).
- **Scenario level:** `@DataProvider(parallel=true)` in each runner — scenarios within a suite run concurrently; `data-provider-thread-count` defaults to `1`, overridden per profile (API profiles use `4`).
- **Device pool (UI):** shared `ConcurrentLinkedQueue` across all threads; threads queue when no device is free.
- **API tests:** stateless REST calls — fully parallel with no shared mutable state; `ApiScenarioContext` is scenario-scoped (PicoContainer creates one per scenario).

### Platform switching

Platform is **per-thread from the device pool** — NOT from `config.properties`. This is critical for mixed-platform parallel execution.

- `devices.json` — single source of truth for all devices (web + mobile). Each entry has `udid`, `platform`, `platformVersion`, `appiumUrl`, `browser`.
- `DeviceManager` — loads `devices.json` into a `ConcurrentLinkedQueue` pool at JVM startup. Each thread calls `acquireDevice()` before test and `releaseDevice()` after.
- `DriverFactory.createDriver()` — acquires device first, then branches on `device.getPlatform()` to create the right driver.
- `LoginPageFactory` — reads `DeviceManager.currentDevice().getPlatform()` to return web or mobile page impl.
- `BasePage.platform()` — reads `DeviceManager.currentDevice().getPlatform()`.

**`thread-count` in `testng.xml` controls runner-level concurrency.** Default is `1`; set via `-Dthread.count=N` or via a Maven profile. `data-provider-thread-count` controls scenario-level concurrency within each runner; set via `-Ddata.provider.thread.count=N`. The device pool serves all threads; add devices to `devices.json` to increase UI throughput.

### Device pool parallel execution

Devices are reusable slots, not 1-to-1 with tests. 5 devices running 10 tests = 2 rounds of continuous pipeline execution. `ConcurrentLinkedQueue` is thread-safe — a freed device is immediately available to the next waiting thread.

### Page object pattern

- `BasePage` (`src/main/java/com/framework/base/`) — shared helpers for all page objects. All page classes extend this. Exposes protected wrappers for every utility class — page objects never import utils directly.
  - **Element helpers:** `find`, `click`, `type`, `getText`, `open`, `currentUrl`, `isDisplayed`, `isDisplayedSafely(locator, seconds)` — `isDisplayedSafely` catches exceptions and returns `false`, useful when you don't know which of two elements will appear.
  - **Scroll helpers:** `swipeUp()`, `swipeDown()`, `swipeToBottom()`, `swipeToTop()` — the `ToBottom`/`ToTop` variants loop up to 15 swipes and stop early when the page source stops changing.
  - **Seek bar helpers:** `swipeSurferBarToMiddle(By)`, `swipeSurferBarToPercent(By, double)`.
  - **Keyboard helpers (mobile):** `hideKeyboard()`, `hideKeyboard(String dismissKey)` (iOS-only overload), `isKeyboardShown()`.
  - **Android hardware keys:** `pressBack()`, `pressHome()`, `pressAppSwitch()`, `pressEnter()`, `pressSearch()`, `pressEscape()`, `pressKey(AndroidKey)`, `longPressKey(AndroidKey)`.
  - **Gesture helpers (mobile):** `longPress(element)`, `longPress(element, duration)`, `doubleTap(element)`, `pinchOpen(element)`, `pinchClose(element)`, `dragAndDrop(source, target)`, `tapAt(x, y)`, `tapScreenCenter()`.
  - **Tap helpers:** `tapByXpath`, `tapById`, `tapByAccessibilityId`, `tapByAndroidUiText`, `tapByAndroidUiTextContains`, `tapByAndroidUiResourceId`, `tapByIosPredicateString`, `tapByIosClassChain`.
  - **Notification helpers (Android):** `openNotificationTray()`, `tapNotificationByText(text)`, `tapNotificationByTextContains(text)`.
  - **D-pad helpers (Android TV):** `dpadUp/Down/Left/Right()`, `dpadSelect()`, `navigateDownTo/UpTo/LeftTo/RightTo(By)` — moves focus until the locator is focused then presses SELECT; throws `RuntimeException` after 20 presses if the element is never reached. `typeInFocused(text)`, `isFocused(By)`, `getFocusedElement()`.
- `ILoginPage` — interface defining the page contract. Step definitions depend on this interface only. Key method: `getVisibleMessage()` returns whichever message is currently on screen (success greeting or error text) — the page object owns this branching logic, not the step.
- `pages/web/LoginPage` — Selenium `By` locators (CSS/ID). Amazon login is a **2-step flow**: enter email → click Continue → enter password → click Sign-In. `getVisibleMessage()` waits up to 8 s for the "Hello" greeting; if absent, falls back to `.a-alert-content` error text.
- `pages/mobile/LoginPage` — `AppiumBy` locators. Locators are **methods not fields** so `platform()` resolves at call time for Android vs iOS.
- `pages/mobile/PlaybackPage` — tap-to-reveal controls then seek to middle via `revealAndSeekToMiddle()`. Seek bar locator is a placeholder — replace with real locator from Appium Inspector.
- `pages/tv/LoginPage` — Android TV implementation of `ILoginPage`. `open()` is a no-op (TV apps launch via capabilities). All actions use D-pad: `navigateDownTo(locator)` to focus each field/button, `typeInFocused(text)` to enter credentials. Locators are placeholder `accessibilityId` values — replace with real values from Appium Inspector.
- `LoginPageFactory` — 3-way switch on `currentDevice().getPlatform()`: `android`/`ios` → `mobile/LoginPage`; `androidtv` → `tv/LoginPage`; all other values → `web/LoginPage`.

### Utility classes (`src/main/java/com/framework/utils/`)

| Class | Platform | What it provides |
|---|---|---|
| `WaitUtils` | Any | `waitForVisible`, `waitForClickable` with configurable timeout |
| `SwipeUtils` | Mobile | `swipeUp/Down/Left/Right`, `swipeUntilVisible`, `androidScrollToText*`, `iosScrollToText*`, `swipeSurferBarToMiddle`, `swipeSurferBarToPercent` |
| `TapUtils` | Mixed | Locate-then-tap helpers by XPath, ID, AccessibilityId (any); UIAutomator text/textContains/resourceId (Android); predicateString/classChain (iOS) |
| `NotificationUtils` | Android | `openNotificationTray()`, `tapNotificationByText()`, `tapNotificationByTextContains()`, `tapNotificationByUiSelector()` |
| `KeyboardUtils` | Mobile | `hideKeyboard()`, `hideKeyboard(dismissKey)` (iOS overload), `isKeyboardShown()` — no-op if keyboard not visible |
| `AndroidKeyUtils` | Android | Hardware key presses: `back()`, `home()`, `appSwitch()`, `enter()`, `search()`, `escape()`, `tab()`, `space()`, `del()`, `volumeUp/Down()`, `power()`, `press(AndroidKey)`, `longPress(AndroidKey)` |
| `GestureUtils` | Mobile | Appium 2 `mobile:` script extensions: `longPress`, `doubleTap`, `pinchOpen`, `pinchClose`, `dragAndDrop`, `tapAt(x,y)`, `tapScreenCenter()` — normalises Android (ms) vs iOS (s) duration units |
| `DPadUtils` | Android TV | D-pad remote navigation: `up/down/left/right/select`, `navigateDownTo/UpTo/LeftTo/RightTo(By)` — polls focus up to `MAX_NAVIGATIONS=20` times, presses CENTER when focused, throws `RuntimeException` if element is unreachable; `typeInFocused(text)`, `getFocusedElement()`, `isFocused(By)`; all methods guard via `requireAndroidTV()` — throws `UnsupportedOperationException` on non-`androidtv` platform |
| `BrokenLinksUtils` | Web | Iterates all `<a>` tags and opens HTTP HEAD connections to detect broken links — currently a standalone utility (incomplete, no response-code assertion yet) |

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
| `@mock` | `MockApiRunner` | `features/api/mock/`, WireMock-backed offline scenarios |
| `@wip` | excluded from all runners | in-progress scenarios, not run in CI |

- `TestRunner` (kept for local ad-hoc use) runs `not @wip` — all UI scenarios in one shot.
- Dummy step definitions for non-login UI features live in `steps/DummySteps.java`.
- `steps/SwipeSteps.java` — Cucumber steps for swipe-to-bottom / swipe-back-to-top verification using `BasePage.swipeToBottom()` / `swipeToTop()` and page source comparison.

## UI Testing Module

### Directory structure

```
src/main/java/com/framework/
├── base/
│   └── BasePage.java               — base class for all page objects; all protected wrappers live here
├── config/
│   └── ConfigManager.java          — singleton; reads config.properties; -D flags override any key
├── drivers/
│   ├── Device.java                 — POJO: udid, platform, platformVersion, appiumUrl, browser, appPackage
│   ├── DeviceManager.java          — loads devices.json into ConcurrentLinkedQueue at JVM startup;
│   │                                 acquireDevice() / releaseDevice() / currentDevice() (ThreadLocal)
│   ├── DriverFactory.java          — createDriver() acquires device, then branches on platform:
│   │                                   web     → ChromeDriver / FirefoxDriver (WebDriverManager setup)
│   │                                   android → AndroidDriver (UiAutomator2Options) + activateApp()
│   │                                   ios     → IOSDriver (XCUITestOptions)
│   └── DriverManager.java          — ThreadLocal<WebDriver>: setDriver / getDriver / quitDriver
├── pages/
│   ├── ILoginPage.java             — interface: navigate(), login(email, password), getVisibleMessage()
│   ├── LoginPageFactory.java       — reads currentDevice().getPlatform() → returns web or mobile impl
│   ├── web/
│   │   └── LoginPage.java          — Selenium By (CSS/ID); Amazon 2-step flow: email→Continue→password→Sign-In
│   └── mobile/
│       ├── LoginPage.java          — AppiumBy locators; locators are methods (not fields) for
│       │                             platform-aware Android vs iOS resolution at call time
│       └── PlaybackPage.java       — revealAndSeekToMiddle(): tapScreenCenter() then swipeSurferBarToMiddle()
└── tv/
│   └── LoginPage.java              — Android TV; open() is no-op; all actions are D-pad driven:
│                                     navigateDownTo(locator) + typeInFocused(text); locators are placeholders
└── utils/                          — see Utility classes table; all methods wrapped on BasePage

src/test/java/com/framework/
├── hooks/
│   └── Hooks.java                  — @Before(order=0): createDriver → setDriver
│                                     @After: screenshot on fail → quitDriver → releaseDevice
│                                     @Before(value="@mobile-only", order=1): throws SkipException if platform=web
├── runners/
│   ├── SmokeTestRunner.java        — @CucumberOptions tags=@smoke; glue=hooks+steps;
│   │                                 @DataProvider(parallel=true); writes smoke-report.json
│   └── RegressionTestRunner.java   — @CucumberOptions tags=@regression; glue=hooks+steps;
│                                     @DataProvider(parallel=true); writes regression-report.json
└── steps/
    ├── LoginSteps.java             — drives ILoginPage; asserts via getVisibleMessage()
    ├── SwipeSteps.java             — swipeToBottom / swipeToTop with page-source comparison assertion
    └── DummySteps.java             — stub steps for non-login UI features (prevents "undefined step" errors)

src/test/resources/
├── devices.json                    — device pool definition (udid, platform, platformVersion, appiumUrl, browser, appPackage)
├── config.properties               — base.url, browser, headless, implicit.wait, app.package, bundle.id, no.reset
└── features/
    ├── login.feature               — ad-hoc feature (no runner tag; use TestRunner locally)
    ├── smoke/
    │   ├── smoke.feature           — @smoke: Amazon login happy path
    │   └── swipe.feature           — @smoke: swipe-to-bottom / swipe-to-top verification
    └── regression/
        └── regression.feature      — @regression: Amazon login negative paths
```

### UI execution flow

```
testng-ui.xml (or testng-ui-smoke.xml / testng-ui-regression.xml)
  └── SmokeTestRunner | RegressionTestRunner
        └── @DataProvider(parallel=true) → one thread per scenario
              │
              ├── Hooks.setUp() [@Before order=0]
              │     ├── DriverFactory.createDriver()
              │     │     ├── DeviceManager.acquireDevice()   — polls ConcurrentLinkedQueue;
              │     │     │                                     throws if pool is empty (no free device)
              │     │     └── switch(device.platform):
              │     │           web     → WebDriverManager.setup() → ChromeDriver / FirefoxDriver
              │     │           android → UiAutomator2Options → AndroidDriver → activateApp(pkg)
              │     │           ios     → XCUITestOptions → IOSDriver
              │     └── DriverManager.setDriver(driver)       — stores in ThreadLocal
              │
              ├── Hooks.skipIfWeb() [@Before(value="@mobile-only") order=1]
              │     └── throws SkipException if currentDevice().platform == "web"
              │
              ├── Step definitions execute
              │     └── LoginPageFactory.create()
              │           ├── platform=web    → web/LoginPage  (Selenium By locators)
              │           └── platform=mobile → mobile/LoginPage (AppiumBy locators)
              │
              └── Hooks.tearDown() [@After]
                    ├── scenario.isFailed() → TakesScreenshot → scenario.attach(bytes, "image/png", ...)
                    ├── DriverManager.quitDriver()            — driver.quit() + ThreadLocal.remove()
                    └── DeviceManager.releaseDevice()         — returns device to pool for next scenario
```

### Driver layer details

| Class | Responsibility |
|---|---|
| `Device` | POJO deserialized from `devices.json`; defaults: platform→`web`, browser→`chrome`, appiumUrl→`http://127.0.0.1:4723` |
| `DeviceManager` | Static initializer loads `devices.json` once at JVM start; `ConcurrentLinkedQueue` is the thread-safe pool; `ThreadLocal<Device>` tracks which device this thread holds |
| `DriverFactory` | `createDriver()` is the single entry point — calls `acquireDevice()` then branches on platform: `web` → WebDriver, `android` → `createAndroidDriver`, `androidtv` → `createAndroidTVDriver` (same UiAutomator2Options + `autoLaunch=false` + `activateApp()` as Android phone), `ios` → IOSDriver |
| `DriverManager` | `ThreadLocal<WebDriver>` — each parallel thread gets its own driver instance; `quitDriver()` calls `driver.quit()` and clears the ThreadLocal to prevent leaks |

### `devices.json` schema

```json
[
  {
    "udid": "emulator-5554",
    "platform": "web",
    "platformVersion": "",
    "appiumUrl": "http://127.0.0.1:4723",
    "browser": "chrome",
    "appPackage": ""
  }
]
```

Fields `appiumUrl`, `browser`, and `platform` have safe defaults — only `udid` is strictly required. For mobile entries set `platform` to `android` or `ios` and provide `appiumUrl` pointing to a running Appium server.

## API Testing Module

### Architecture (Service Object Model)

Mirrors POM: each API service has its own class that encapsulates all calls to that endpoint. Step definitions depend only on the service interface, not raw HTTP.

```
src/main/java/com/framework/api/
├── auth/
│   └── TokenManager.java        — JVM-scoped bearer token cache; double-checked locking so only the
│                                   first thread authenticates; proactive refresh before expiry
│                                   (buffer configurable via api.auth.token.buffer.seconds, default 30s);
│                                   expiry resolved from "expires_in" response field or api.auth.token.ttl.seconds
├── config/
│   ├── Environment.java         — enum DEV | QA | STAGING | PROD | MOCK; fromString() defaults to QA
│   └── ApiConfig.java           — loads api/environments/<env>.properties; getBaseUrl(service);
│                                   getInt(key, default) and get(key) helpers
├── core/
│   ├── BaseApiService.java      — doGet/doPost/doPut/doPatch/doDelete with shared RequestSpec;
│   │                               authSpec() returns a bearer-token spec via TokenManager for
│   │                               authenticated endpoints (services call this instead of baseSpec)
│   └── ApiScenarioContext.java  — PicoContainer shared state (lastResponse, key-value store)
├── mock/
│   ├── MockServerManager.java   — JVM-scoped WireMock server on port 9999; double-checked locking;
│   │                               shutdown hook auto-stops it; reset() clears stubs between scenarios
│   └── StubFactory.java         — static stub helpers: stubGet/Post/Put/Patch/Delete (exact URL),
│                                   stubGetContaining/stubPostContaining (partial URL);
│                                   error helpers: stubServerError/stubUnauthorized/stubNotFound;
│                                   fault helpers: stubTimeout(url, delayMs), stubConnectionReset(url)
├── services/
│   ├── UserService.java         — /users CRUD  (QA: reqres.in)
│   ├── AuthService.java         — /login /register  (QA: reqres.in)
│   ├── ProductService.java      — /products CRUD + category filter  (QA: fakestoreapi.com)
│   └── OrderService.java        — /todos CRUD + user filter  (QA: jsonplaceholder.typicode.com)
└── utils/
    ├── ApiRequestSpecBuilder.java — fluent builder: baseUri, auth (withBearerToken), headers, timeout, relaxed HTTPS
    ├── ResponseValidator.java     — static assertions wrapping TestNG Assert
    └── ApiLogger.java             — SLF4J log helpers for request/response

src/test/java/com/framework/
├── api/
│   ├── hooks/ApiHooks.java        — @Before/@After for @api-smoke and @api-regression tags
│   ├── runners/
│   │   ├── ApiSmokeRunner.java
│   │   └── ApiRegressionRunner.java
│   └── steps/
│       ├── UserSteps.java
│       ├── AuthSteps.java
│       ├── ProductSteps.java
│       └── OrderSteps.java
└── mock/
    ├── hooks/MockApiHooks.java    — @Before(value="@mock"): starts WireMock server (idempotent);
    │                                @After(value="@mock"): resets all stubs via MockServerManager.reset()
    ├── runners/MockApiRunner.java — @DataProvider(parallel=false) — MUST stay false; shared stub
    │                                registry would cause cross-scenario interference if parallelised;
    │                                glue includes both mock.steps and api.steps (reuses When/Then)
    └── steps/MockSteps.java       — Cucumber Given steps that call StubFactory; covers all HTTP
                                     verbs + error/timeout/connection-reset scenarios

src/test/resources/
├── api/environments/
│   ├── qa.properties      — uses public stub APIs (reqres.in, fakestoreapi.com, jsonplaceholder)
│   ├── dev.properties     — placeholder URLs for internal dev environment
│   ├── staging.properties — placeholder URLs for staging environment
│   └── mock.properties    — all service URLs point to http://localhost:9999 (WireMock port)
└── features/api/
    ├── smoke/api-smoke.feature
    ├── regression/
    │   ├── user-service.feature
    │   ├── auth-service.feature
    │   ├── product-service.feature
    │   └── order-service.feature
    └── mock/api-mock.feature      — @mock scenarios: happy paths (200/201/204) + error paths
                                     (404/500/401) that are hard to reproduce on real APIs
```

### Environment switching

Active environment is read from `-Dapi.env=<dev|qa|staging|prod>` (default: `qa`). Each environment has its own properties file under `src/test/resources/api/environments/`. Swap environment without changing code:

```bash
mvn clean verify -Papi -Dapi.env=staging
```

**Auth config properties** (add to each env properties file when real auth is needed):

| Property | Purpose |
|---|---|
| `api.auth.email` | Login credential read by `TokenManager` |
| `api.auth.password` | Login credential read by `TokenManager` |
| `api.auth.token.ttl.seconds` | Token lifetime fallback if response has no `expires_in` (default: `3600`) |
| `api.auth.token.buffer.seconds` | Proactive refresh window before expiry (default: `30`) |
| `api.connection.timeout` | REST Assured connection timeout in ms (default: `10000`) |
| `api.socket.timeout` | REST Assured socket timeout in ms (default: `30000`) |

### Adding a new API service

1. Add a new service class in `src/main/java/com/framework/api/services/` extending `BaseApiService`.
2. Pass the service name key to `super("myservice")` — it resolves `api.base.url.myservice` from env config.
3. Use `doGet/doPost/...` with `baseSpec` for public endpoints; use `authSpec()` for endpoints that require a bearer token (`TokenManager` handles the token lifecycle automatically).
4. Add step definitions in `src/test/java/com/framework/api/steps/`, injecting `ApiScenarioContext` via constructor.
5. Add feature files in `src/test/resources/features/api/smoke/` and `features/api/regression/`.
6. Add `api.base.url.myservice=<url>` to each environment properties file.

## Key config properties (`src/test/resources/config.properties`)

`ConfigManager` is a singleton that reads `config.properties` and allows `-D` system property overrides. Typed accessors: `get(key)`, `get(key, default)`, `getInt(key, default)`, `getBoolean(key, default)`.

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
- **Masterthought** (`maven-cucumber-reporting`) — runs in `verify` phase, reads all four JSON files (`smoke-report.json`, `regression-report.json`, `api-smoke-report.json`, `api-regression-report.json`) from `target/cucumber-reports/`, generates combined rich HTML report. Always use `mvn clean verify` to get the report.

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

## Mock API Module

### Purpose and design

Runs API scenarios against an embedded WireMock server instead of real external endpoints. Stubs are set up in `Given` steps, the existing real-service `When`/`Then` steps are reused unchanged. Use this for error paths (500, 401, connection resets) that are impossible or unreliable to reproduce against live APIs.

### Lifecycle

```
MockApiRunner (parallel=false — stub registry is shared, must not run concurrently)
  └── @DataProvider(parallel=false)
        │
        ├── MockApiHooks.startMockServer [@Before @mock order=0]
        │     ├── MockServerManager.start()  — no-op if already running (double-checked lock)
        │     └── RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
        │
        ├── MockSteps (Given) → StubFactory → WireMock.stubFor(...)
        │
        ├── api/steps/* (When/Then) — identical step impls; service URLs hit localhost:9999
        │
        └── MockApiHooks.resetMockServer [@After @mock order=0]
              └── MockServerManager.reset()  — clears all stubs; server keeps running for next scenario
```

### `StubFactory` reference

| Method | Matches | Registers |
|---|---|---|
| `stubGet(url, status, body)` | exact path GET | JSON response |
| `stubGetContaining(sub, status, body)` | partial path GET | JSON response |
| `stubPost(url, status, body)` | exact path POST | JSON response |
| `stubPostWithRequestBody(url, fragment, status, body)` | POST + request body contains fragment | JSON response |
| `stubPut / stubPatch / stubDelete` | exact path | JSON response or status-only |
| `stubServerError(url)` | any method, exact path | 500 + `{"error":"Internal Server Error"}` |
| `stubUnauthorized(url)` | any method | 401 + `{"error":"Unauthorized"}` |
| `stubNotFound(url)` | any method | 404 + `{"error":"Not Found"}` |
| `stubTimeout(url, delayMs)` | any method | 200 after fixed delay — set socket timeout lower to test timeout handling |
| `stubConnectionReset(url)` | any method | `Fault.CONNECTION_RESET_BY_PEER` — tests retry logic |

### Cucumber step vocabulary (Given)

```gherkin
Given the GET "/users/1" endpoint is stubbed to return 200 with body:
  """
  {"data":{"id":1,"email":"mock@test.com"}}
  """
Given the POST "/users" endpoint is stubbed to return 201 with body: ...
Given the PUT "/users/2" endpoint is stubbed to return 200 with body: ...
Given the PATCH "/users/2" endpoint is stubbed to return 200 with body: ...
Given the DELETE "/users/2" endpoint is stubbed to return 204
Given the "/users" endpoint is stubbed to return a 500 server error
Given the "/login" endpoint is stubbed to return a 401 unauthorized
Given the GET "/users/999" endpoint is stubbed to return 404 with body: ...
Given the "/slow" endpoint is stubbed to respond after 5000ms delay
Given the POST "/login" endpoint containing "wrongpass" in request body is stubbed to return 400 with body: ...
```

### Running mock tests

```bash
mvn clean verify -Pmock -Dapi.env=mock
```

`mock.properties` routes all service base URLs to `http://localhost:9999`. The `MOCK` value was added to the `Environment` enum to support this.

**Important:** Do not set `data-provider-thread-count > 1` for mock scenarios — `MockServerManager` has a single shared stub registry; concurrent scenarios would overwrite each other's stubs.

## Pure TestNG Native Tests

A separate test layer using plain TestNG (`@Test` methods) instead of Cucumber feature files. Located in `com.framework.testng`. Run via the `login-testng` Maven profile.

```bash
mvn clean test -Plogin-testng                  # run all tests
mvn clean test -Plogin-testng -Dgroups=smoke   # run smoke group only
mvn clean test -Plogin-testng -Dgroups=regression
```

### `BaseTest`

Provides the full TestNG lifecycle:

| Annotation | Scope | What it does |
|---|---|---|
| `@BeforeSuite` | once per JVM | logs suite start |
| `@AfterSuite` | once per JVM | logs suite end |
| `@BeforeClass` | once per test class | logs class name |
| `@AfterClass` | once per test class | logs class teardown |
| `@BeforeMethod` | each `@Test` | `DriverFactory.createDriver()` + `DriverManager.setDriver()` |
| `@AfterMethod(ITestResult)` | each `@Test` | screenshot on failure → saved to `target/screenshots/<testName>_FAILED.png`; `DriverManager.quitDriver()` + `DeviceManager.releaseDevice()` |

All annotations carry `alwaysRun=true` — teardown runs even when setup or the test itself fails.

### `LoginTest` — TestNG concepts demonstrated

| Concept | Method | Notes |
|---|---|---|
| `@Test` attributes | `testLoginPageLoads` | `description`, `groups`, `priority` |
| Hard `Assert` | `testLoginPageLoads` | stops test on first failure |
| `dependsOnMethods` | `testInvalidLogin` | auto-skipped if dependency fails |
| `@DataProvider` | `testEmailValidation` | parameterised rows, equivalent to Cucumber Scenario Outline |
| `SoftAssert` | `testLoginPageMetadata` | collects all failures; `assertAll()` triggers the report |
| `enabled = false` | `testForgotPassword` | static exclusion without deleting the test |
| `SkipException` | `testMobileDeepLink` | programmatic skip at runtime based on current platform |

## CI/CD Pipeline (Jenkinsfile)

Windows agent (`bat` step). Configured with `buildDiscarder(numToKeepStr: '20')` and `timestamps()`.

### Parameters

| Parameter | Type | Default | Purpose |
|---|---|---|---|
| `TEST_SUITE` | choice | `api-smoke` | Maven profile to activate (`api-smoke`, `api-regression`, `api`, `ui-smoke`, `ui-regression`, `ui`) |
| `API_ENV` | string | `qa` | Passed as `-Dapi.env` — selects environment properties file |
| `THREAD_COUNT` | string | `1` | Passed as `-Dthread.count` |
| `DATA_PROVIDER_THREAD_COUNT` | string | `4` | Passed as `-Ddata.provider.thread.count` |
| `HEADLESS` | boolean | `true` | Passed as `-Dheadless` |

### Post-build actions (always)

- `junit` — publishes `target/surefire-reports/*.xml` (allows empty results so the stage doesn't fail when no XML exists)
- `archiveArtifacts` — archives `target/cucumber-reports/**`, `target/masterthought-reports/**`, `target/surefire-reports/**`

### Required Jenkins tool configuration

Jenkinsfile expects two named tools in Jenkins global config:
- JDK named `jdk17`
- Maven named `maven3`

## Database Testing Module

### Architecture

Thin JDBC wrapper for test data setup and assertion. All classes live under `com.framework.db`.

```
src/main/java/com/framework/db/
├── DbConfig.java       — reads db.properties (or db/<env>.properties via -Ddb.env=);
│                         System-property overrides: -Ddb.url, -Ddb.username, -Ddb.password
├── DbClient.java       — PreparedStatement-based query executor; each method opens/closes its
│                         own connection (safe for parallel test execution without shared state)
└── DbAssertions.java   — static TestNG assertion helpers for row-level validation

src/test/resources/
└── db.properties       — connection config; defaults to H2 in-memory for local/CI runs
```

### `DbClient` API

```java
DbClient db = new DbClient();  // reads from db.properties

// SELECT — returns rows as List<Map<columnName, value>>; column names are lowercased
List<Map<String, Object>> rows = db.queryRows("SELECT * FROM users WHERE status = ?", "active");
Map<String, Object> row       = db.queryRow("SELECT * FROM users WHERE id = ?", 42);
Object count                  = db.queryScalar("SELECT COUNT(*) FROM users");

// INSERT / UPDATE / DELETE
int affected = db.executeUpdate("UPDATE users SET status = ? WHERE id = ?", "inactive", 1);

// Batch INSERT / UPDATE in a single transaction (rolls back on failure)
List<Object[]> batch = List.of(new Object[]{"alice", "alice@test.com"}, new Object[]{"bob", "bob@test.com"});
int[] counts = db.executeBatch("INSERT INTO users(name, email) VALUES (?, ?)", batch);
```

`bindParams` handles: `null`, `String`, `Integer`, `Long`, `Double`, `Float`, `Boolean`, `LocalDate`, `LocalDateTime`. All other types fall through to `PreparedStatement.setObject`.

### `DbAssertions` API

| Method | What it checks |
|---|---|
| `assertRowCount(rows, n)` | Exactly n rows returned |
| `assertNotEmpty(rows)` | At least one row |
| `assertEmpty(rows)` | No rows |
| `assertColumnValue(row, col, expected)` | Single column equality (String.valueOf comparison) |
| `assertColumnContains(row, col, sub)` | Column value contains substring |
| `assertColumnNotContains(row, col, sub)` | Column value does not contain substring |
| `assertColumnNull(row, col)` | Column value is null |
| `assertRowExists(rows, col, value)` | At least one row has column = value |
| `assertRowAbsent(rows, col, value)` | No row has column = value |
| `assertAllRowsMatch(rows, col, value)` | Every row has column = value |

### Environment switching

```bash
mvn clean verify -Ddb.env=staging   # loads db/staging.properties from classpath
mvn clean verify -Ddb.url=jdbc:mysql://host:3306/mydb -Ddb.username=tester -Ddb.password=secret
```

### JDBC drivers

H2 (`test` scope) is the default driver for local/CI runs. Add your real driver to `pom.xml` — MySQL (`com.mysql:mysql-connector-j`) and PostgreSQL (`org.postgresql:postgresql`) blocks are included as comments.

### Using DB assertions in step definitions

```java
// In a step definition (no PicoContainer injection needed — DbClient is stateless)
private final DbClient db = new DbClient();

@Then("the user {string} should exist in the database")
public void verifyUserExists(String email) {
    List<Map<String, Object>> rows = db.queryRows(
        "SELECT * FROM users WHERE email = ?", email);
    DbAssertions.assertRowCount(rows, 1);
    DbAssertions.assertColumnValue(rows.get(0), "email", email);
}
```

## Known pending items

- `mobile/LoginPage` locators use placeholder `accessibilityId` values — replace with real Amazon app locators from Appium Inspector
- `mobile/PlaybackPage` seek bar locator (`com.yourapp:id/seek_bar`) is a placeholder — replace with real locator
- `tv/LoginPage` locators are placeholders (`tv_email_field`, `tv_login_btn`, `tv_password_field`, etc.) — replace with real `accessibilityId` / `androidUIAutomator` values from Appium Inspector connected to the TV device
- `BrokenLinksUtils` is incomplete: the `HttpURLConnection` is opened but no response code is checked and no assertion is made — needs completion before use in tests
- Amazon app package: `in.amazon.mShop.android.shopping` — no `appActivity` needed (see non-exported activity workaround above)
- ExtentReports dependency in `pom.xml` is present but not yet wired into the framework
