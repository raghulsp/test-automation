# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run all tests and generate Masterthought report
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

# Run tests by Cucumber tag
mvn clean verify -Dcucumber.filter.tags="@smoke"
mvn clean verify -Dcucumber.filter.tags="not @wip"

# Override device at runtime
mvn clean verify -Ddevice.name=emulator-5554 -Dplatform.version=14
```

**Reports:**
- Cucumber HTML: `target/cucumber-reports/report.html`
- Masterthought: `target/masterthought-reports/cucumber-html-reports/overview-features.html`

## Architecture

**Stack:** Java 17 · Selenium 4 · Appium Server 2 (Java Client 9) · Cucumber 7 · TestNG 7 · WebDriverManager · Jackson 2.17

### Execution flow

```
testng.xml
  └── TestRunner (@CucumberOptions, parallel=true)
        └── Hooks (@Before / @After)
              ├── @Before → DriverFactory.createDriver() → DriverManager.setDriver()
              └── @After  → screenshot on fail → DriverManager.quitDriver() → DeviceManager.releaseDevice()
        └── Step definitions → Page objects via LoginPageFactory
```

### Platform switching

Platform is **per-thread from the device pool** — NOT from `config.properties`. This is critical for mixed-platform parallel execution.

- `devices.json` — single source of truth for all devices (web + mobile). Each entry has `udid`, `platform`, `platformVersion`, `appiumUrl`, `browser`.
- `DeviceManager` — loads `devices.json` into a `ConcurrentLinkedQueue` pool at JVM startup. Each thread calls `acquireDevice()` before test and `releaseDevice()` after.
- `DriverFactory.createDriver()` — acquires device first, then branches on `device.getPlatform()` to create the right driver.
- `LoginPageFactory` — reads `DeviceManager.currentDevice().getPlatform()` to return web or mobile page impl.
- `BasePage.platform()` — reads `DeviceManager.currentDevice().getPlatform()`.

**`thread-count` in `testng.xml` must equal number of entries in `devices.json`** — otherwise threads will throw "No available devices in pool".

### Device pool parallel execution

Devices are reusable slots, not 1-to-1 with tests. 5 devices running 10 tests = 2 rounds of continuous pipeline execution. `ConcurrentLinkedQueue` is thread-safe — a freed device is immediately available to the next waiting thread.

### Page object pattern

- `BasePage` (`src/main/java`) — shared helpers (`click`, `type`, `find`, `open`, `platform()`). All page classes extend this.
- `ILoginPage` — interface defining the page contract. Step definitions depend on this interface only.
- `pages/web/LoginPage` — Selenium `By` locators (CSS/ID).
- `pages/mobile/LoginPage` — `AppiumBy` locators. Locators are **methods not fields** so `platform()` resolves at call time for Android vs iOS.
- `LoginPageFactory` — returns correct implementation based on current thread's device platform.

### Adding a new page

1. Add interface in `src/main/java/com/framework/pages/`.
2. Create `web/` and `mobile/` implementations extending `BasePage`.
3. Add factory class following `LoginPageFactory` pattern.
4. Step definitions use the interface type only.

### Adding a new device

Add one entry to `src/test/resources/devices.json` and increment `thread-count` in `testng.xml` by 1. No Java changes needed.

## Key config properties (`src/test/resources/config.properties`)

| Property | Default | Purpose |
|---|---|---|
| `platform` | `web` | Fallback only — runtime platform comes from `devices.json` |
| `browser` | `chrome` | Fallback only — browser comes from `devices.json` `browser` field |
| `headless` | `false` | Web only |
| `base.url` | herokuapp login | URL opened in web scenarios |
| `appium.url` | `http://127.0.0.1:4723` | Fallback — each device has its own in `devices.json` |
| `implicit.wait` | `10` | Seconds for implicit waits |

All properties can be overridden at runtime with `-D` flags.

## Build plugins

- **Surefire** — runs TestNG tests in `test` phase. `testFailureIgnore=true` ensures build continues to `verify` phase even when tests fail.
- **Masterthought** (`maven-cucumber-reporting`) — runs in `verify` phase, reads `target/cucumber-reports/report.json`, generates rich HTML report. Always use `mvn clean verify` to get the report.

## Appium setup (for mobile)

Each device needs its own Appium server instance on a separate port:
```bash
appium --port 4723 --udid emulator-5554
appium --port 4724 --udid emulator-5556
appium --port 4725 --udid iPhone14-UDID
```

`UiAutomator2Options` used for Android, `XCUITestOptions` for iOS — both are Appium 2.x standard, replacing deprecated `DesiredCapabilities`.

## Known pending items

- `src/test/java/com/framework/base/BasePage.java` and `src/test/java/com/framework/pages/LoginPage.java` are leftover duplicates — delete both
- `mobile/LoginPage` locators use placeholder `accessibilityId` values — replace with real app locators
- Netflix app configured: package `com.netflix.mediaclient`, activity `com.netflix.mediaclient.ui.launch.UIWebViewActivity`
- ExtentReports dependency in `pom.xml` not yet integrated
