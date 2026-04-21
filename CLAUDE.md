# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run all tests
mvn clean test

# Run with a specific platform
mvn clean test -Dplatform=web
mvn clean test -Dplatform=android
mvn clean test -Dplatform=ios

# Run a specific browser (web only)
mvn clean test -Dbrowser=firefox
mvn clean test -Dheadless=true

# Run tests by Cucumber tag
mvn clean test -Dcucumber.filter.tags="@smoke"
mvn clean test -Dcucumber.filter.tags="not @wip"

# Skip WIP tests (default behaviour via TestRunner)
mvn clean test

# Run API tests only (no browser required)
mvn clean test -Dcucumber.filter.tags="@api"

# Run integration tests (API setup + UI verification)
mvn clean test -Dplatform=web -Dcucumber.filter.tags="@integration"

# Run UI tests only (exclude API-only scenarios)
mvn clean test -Dplatform=web -Dcucumber.filter.tags="not @api and not @wip"
```

Reports are written to `target/cucumber-reports/report.html` after each run.

## Architecture

**Stack:** Java 17 · Selenium 4 · Appium 9 · Cucumber 7 · TestNG 7

### Execution flow

```
testng.xml
  └── TestRunner (@CucumberOptions)
        └── Hooks (@Before / @After)
              ├── @Before → if NOT @api: DriverFactory.createDriver() → DriverManager.setDriver()
              └── @After  → if NOT @api: screenshot on fail → DriverManager.quitDriver()
                          → always: ScenarioContext.clear()
        └── Step definitions
              ├── LoginSteps  → Page objects via LoginPageFactory  (UI scenarios)
              ├── ApiSteps    → ApiClient → UserService / AuthService  (@api scenarios)
              └── Both        → ScenarioContext (shared data bridge for @integration scenarios)
```

### API + integration flow

```
@api scenario                          @integration scenario
─────────────────────────────────      ──────────────────────────────────────────
Hooks.@Before (no driver created)      Hooks.@Before → DriverFactory.createDriver()
  │                                      │
ApiSteps → ApiClient                   ApiSteps.createUserViaApi()
  └─ UserService / AuthService           └─ UserService.createAndReturnCredentials()
       │                                      └─ ScenarioContext.set("credentials", ...)
       └─ ScenarioContext.set(                │
            "lastResponse", response)   LoginSteps.loginWithApiCreatedCredentials()
  │                                      └─ ScenarioContext.get("credentials", ...)
ApiSteps assertion steps                     └─ loginPage.enterUsername / enterPassword
  └─ ScenarioContext.get("lastResponse") │
                                        UI assertions
Hooks.@After (no driver to quit)        │
  └─ ScenarioContext.clear()           Hooks.@After → DriverManager.quitDriver()
                                          └─ ScenarioContext.clear()
```

### Platform switching

`config.properties` (`platform=web|android|ios`) or `-Dplatform=` at runtime drives everything:

- `DriverFactory` — creates `ChromeDriver`/`FirefoxDriver` for web, `AndroidDriver` (UiAutomator2Options) for Android, `IOSDriver` (XCUITestOptions) for iOS.
- `LoginPageFactory` — returns `pages.web.LoginPage` or `pages.mobile.LoginPage` based on the same property.
- `DriverManager` — holds the active `WebDriver` in a `ThreadLocal` for thread-safe parallel access.

### Page object pattern

- `BasePage` (`src/main/java`) — shared helpers (`click`, `type`, `find`, `open`, `platform()`). All page classes extend this.
- `ILoginPage` — interface defining the page contract. Step definitions depend on this interface, not concrete classes.
- `pages/web/LoginPage` — Selenium `By` locators (CSS/ID).
- `pages/mobile/LoginPage` — `AppiumBy` locators; each locator is a **method** (not a field) so `platform()` is evaluated at call time, returning the correct Android (`accessibilityId`, `androidUIAutomator`) or iOS (`iOSNsPredicateString`) locator.

### Adding a new page

1. Add an interface in `src/main/java/com/framework/pages/`.
2. Create `web/` and `mobile/` implementations extending `BasePage`.
3. Add a factory class following `LoginPageFactory` pattern.
4. Step definitions use the interface type.

### Key config properties

| Property | Default | Purpose |
|---|---|---|
| `platform` | `web` | Selects driver + page implementation |
| `browser` | `chrome` | Web only: `chrome` or `firefox` |
| `headless` | `false` | Web only |
| `base.url` | herokuapp login | URL opened in web scenarios |
| `appium.url` | `http://127.0.0.1:4723` | Appium server for mobile |
| `implicit.wait` | `10` | Seconds for implicit waits |
| `api.base.url` | `https://reqres.in` | Base URL for REST API calls |

All properties can be overridden at runtime with `-D` flags without editing the file.

### Adding a new API service

1. Add a service class in `src/main/java/com/framework/api/services/` extending or composing `ApiClient`.
2. Return raw `Response` objects from each method for assertions in step definitions.
3. For setup helpers (e.g. `createAndReturnX()`), assert the status inline and return a model object.
4. Add step definitions in `ApiSteps` (or a new `*Steps` class if the domain warrants it).
5. Create feature files under `src/test/resources/features/api/` and tag them `@api`.
