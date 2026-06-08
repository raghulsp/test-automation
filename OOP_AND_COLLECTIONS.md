# OOP and Collections in the Framework

---

## OOP Concepts

### 1. Abstraction

Abstraction hides implementation details and exposes only what callers need.

**`BasePage` — concrete base class as an abstraction layer**
- `src/main/java/com/framework/base/BasePage.java`
- All page objects extend `BasePage`. It hides raw Selenium/Appium calls (e.g., `WaitUtils`, `SwipeUtils`) behind simple `protected` wrappers like `click(By)`, `type(By, String)`, and `swipeUp()`. Page classes never import utility classes directly.

**`BaseApiService` — abstract base for all API services**
- `src/main/java/com/framework/api/core/BaseApiService.java`
- Declared `abstract`. Encapsulates REST Assured request spec construction (`buildBaseSpec`, `commonSpecBuilder`) and exposes `doGet`, `doPost`, `doPut`, `doPatch`, `doDelete` as the only API surface concrete services need. The token lifecycle, timeouts, and HTTPS relaxation are invisible to subclasses.

**`ILoginPage` — interface as a pure contract**
- `src/main/java/com/framework/pages/ILoginPage.java`
- Declares the login page contract (`open`, `enterEmail`, `clickContinue`, `enterPassword`, `clickLogin`, `getVisibleMessage`, `getCurrentUrl`) without any implementation. Step definitions depend only on this interface — they never know whether they are driving a browser or a mobile app.

---

### 2. Inheritance

Inheritance lets subclasses reuse and specialise parent behaviour.

**Page objects inherit `BasePage`**
- `src/main/java/com/framework/pages/web/LoginPage.java` — `extends BasePage implements ILoginPage`
- `src/main/java/com/framework/pages/mobile/LoginPage.java` — `extends BasePage implements ILoginPage`
- `src/main/java/com/framework/pages/tv/LoginPage.java` — `extends BasePage implements ILoginPage`
- `src/main/java/com/framework/pages/mobile/PlaybackPage.java` — `extends BasePage`
- All page classes inherit every protected helper (click, type, swipeUp, tapByXpath, pressBack, etc.) from `BasePage` without re-implementing them.

**API services inherit `BaseApiService`**
- `UserService`, `AuthService`, `ProductService`, `OrderService` each call `super("serviceName")` and inherit `doGet/doPost/doPut/doPatch/doDelete` plus `baseSpec` and `authSpec()`.

**TestNG test class hierarchy**
- `src/test/java/com/framework/testng/BaseTest.java` — defines `@BeforeSuite / @BeforeClass / @BeforeMethod / @AfterMethod / @AfterSuite` lifecycle with driver creation and teardown.
- `src/test/java/com/framework/testng/LoginTest.java` — `extends BaseTest`, inherits the full lifecycle and only writes `@Test` methods.

---

### 3. Polymorphism

Polymorphism allows one type to take many forms at runtime.

**Interface polymorphism — `ILoginPage`**
- `LoginPageFactory.get()` returns `ILoginPage`. At runtime, depending on the device platform, this is `web.LoginPage`, `mobile.LoginPage`, or `tv.LoginPage`. Step definitions call `loginPage.clickLogin()` without knowing which class handles it.

```java
// LoginPageFactory.java
return switch (platform) {
    case "android", "ios" -> new LoginPage();           // mobile impl
    case "androidtv"      -> new com.framework.pages.tv.LoginPage();  // TV impl
    default               -> new com.framework.pages.web.LoginPage(); // web impl
};
```

**Supertype reference — `WebDriver`**
- `DriverFactory.createDriver()` returns `WebDriver`. The actual runtime type is `ChromeDriver`, `FirefoxDriver`, `AndroidDriver`, or `IOSDriver`. `DriverManager` stores it as `WebDriver` — all calling code works with the common interface.

**Method overloading (compile-time polymorphism)**

| Method | File | Overloads |
|--------|------|-----------|
| `hideKeyboard()` / `hideKeyboard(String dismissKey)` | `BasePage.java` | no-arg vs iOS dismiss-key overload |
| `longPress(element)` / `longPress(element, Duration)` | `BasePage.java` | default vs custom duration |
| `doGet(path)` / `doGet(path, RequestSpecification)` | `BaseApiService.java` | default spec vs override spec |
| `doPost(path, body)` / `doPost(path, body, RequestSpecification)` | `BaseApiService.java` | default spec vs override spec |
| `get(key)` / `get(key, defaultValue)` | `ConfigManager.java`, `ApiConfig.java` | with or without a fallback value |

**Runtime overriding**
- `mobile.LoginPage` overrides `open(String url)` to be a no-op (native apps don't navigate by URL), while `web.LoginPage` calls `super.open(url)`. The same method name does different things depending on the concrete type.

---

### 4. Encapsulation

Encapsulation bundles data with its behaviour and hides internal state.

**Page objects — locators and interaction logic**
- `src/main/java/com/framework/pages/web/LoginPage.java`
- `src/main/java/com/framework/pages/mobile/LoginPage.java`
- Locators are `private final` fields (`emailField`, `passwordField`, `loginButton`, etc.) — no caller ever sees a `By` selector. Interaction logic (`getVisibleMessage()` waits up to 8 s then falls back to the error element) lives inside the class. Step definitions only call `enterEmail()`, `clickLogin()`, `getVisibleMessage()` — they have no knowledge of CSS selectors, XPaths, or wait strategies.

**`Device` POJO**
- `src/main/java/com/framework/drivers/Device.java`
- All fields (`udid`, `platform`, `browser`, etc.) are `private`. Access is through getters that apply safe defaults — `getPlatform()` returns `"web"` when null, `getAppiumUrl()` returns the default Appium URL when null. Callers cannot accidentally get a `null` platform.

**`ConfigManager` — Singleton with private state**
- `src/main/java/com/framework/config/ConfigManager.java`
- Declared `final`. Constructor is `private`. The `Properties` object is a `private final` field. The only way to read a value is through the `static get / getInt / getBoolean` helpers, which transparently layer `-D` system property overrides on top of the file.

**`TokenManager` — Singleton with locked state**
- `src/main/java/com/framework/api/auth/TokenManager.java`
- Private constructor prevents instantiation. `cachedToken` and `tokenExpiry` are `private static volatile` — thread-safe and unreachable from outside. The double-checked `getToken()` method is the only entry point; callers never see the expiry logic or the re-authentication flow.

**`ApiScenarioContext` — scenario-scoped shared state**
- `src/main/java/com/framework/api/core/ApiScenarioContext.java`
- The `lastResponse` and `store` map are `private`. Typed accessors (`retrieveAsString`, `retrieveAsInt`) prevent raw-cast errors at the call site.

---

### 5. Interface

Interfaces define contracts that multiple unrelated classes can fulfil.

**`ILoginPage`**
- `src/main/java/com/framework/pages/ILoginPage.java`
- Three completely different implementations (`web.LoginPage`, `mobile.LoginPage`, `tv.LoginPage`) satisfy the same interface. `LoginSteps.java` holds a field of type `ILoginPage` — the step logic never changes regardless of which platform is under test.

**Selenium's `WebDriver` interface (framework-level)**
- `DriverManager` stores `ThreadLocal<WebDriver>`. The entire framework interacts with the driver through this interface — it is never cast to a concrete type unless a platform-specific API is needed (e.g., `AppiumDriver` in `BasePage.swipeToBottom()`).

---

### 6. Design Patterns (OOP applied)

| Pattern | Where | How |
|---------|-------|-----|
| **Singleton** | `ConfigManager`, `ApiConfig`, `TokenManager`, `MockServerManager` | Private constructor / static instance / double-checked locking |
| **Factory** | `LoginPageFactory`, `DriverFactory` | Returns the right implementation based on runtime platform string |
| **Page Object Model** | `BasePage` → `web/LoginPage`, `mobile/LoginPage`, `tv/LoginPage` | Inherits base; implements interface; used only via interface reference |
| **Service Object Model** | `BaseApiService` → `UserService`, `AuthService`, etc. | Mirrors POM for API layer |
| **Builder** | `ApiRequestSpecBuilder` | Fluent API — each `with*()` method returns `this`, terminated by `build()` |
| **Template Method** | `BaseApiService.commonSpecBuilder()` | Common spec construction is a private template; subclasses only pick the endpoint |
| **ThreadLocal** | `DriverManager`, `DeviceManager` | Isolates per-thread state for parallel execution without shared mutable variables |

---

## Collections

### `ConcurrentLinkedQueue<Device>` — device pool

```java
// DeviceManager.java
private static final ConcurrentLinkedQueue<Device> devicePool = new ConcurrentLinkedQueue<>();
```

A non-blocking, thread-safe queue. Multiple parallel test threads call `poll()` (acquire) and `offer()` (release) without any explicit `synchronized` block. A freed device is immediately visible to the next waiting thread. Chosen over `LinkedList` specifically because of concurrent access from the `@DataProvider(parallel=true)` threads.

---

### `ThreadLocal<Device>` and `ThreadLocal<WebDriver>` — per-thread state

```java
// DeviceManager.java
private static final ThreadLocal<Device> assignedDevice = new ThreadLocal<>();

// DriverManager.java
private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
```

Each parallel thread gets its own isolated slot. Thread A's driver is invisible to Thread B. `remove()` is called in `@After` / `@AfterMethod` to prevent memory leaks after the thread is returned to the pool.

---

### `List<Device>` — Jackson deserialisation

```java
// DeviceManager.java (static initializer)
List<Device> devices = mapper.readValue(is, new TypeReference<>() {});
devicePool.addAll(devices);
```

Jackson reads `devices.json` into a `List<Device>`. The list is immediately drained into the `ConcurrentLinkedQueue` — the `List` is a one-time loading buffer, not a long-lived structure.

---

### `List<WebElement>` — link collection

```java
// BrokenLinksUtils.java
List<WebElement> links = driver.findElements(By.tagName("a"));
for (WebElement link : links) { ... }
```

`findElements` returns a `List<WebElement>`. Iterated with an enhanced for-loop to open an HTTP HEAD connection per link. (Note: this utility is currently incomplete — no response code assertion is wired up yet.)

---

### `Map<String, Object>` — key-value scenario store

```java
// ApiScenarioContext.java
private final Map<String, Object> store = new HashMap<>();
```

A `HashMap` acts as a flexible bag of typed values shared across step definitions within one Cucumber scenario (e.g., storing a created user ID in one step, reading it in the next). `retrieveAsString` and `retrieveAsInt` provide typed access without raw casts at the call site.

---

### `Map<String, Object>` — API request payloads

```java
// UserService.java
public Response createUser(Map<String, Object> payload) {
    return doPost("/users", payload);
}
public Response updateUser(int userId, Map<String, Object> payload) {
    return doPut("/users/" + userId, payload);
}
```

Service methods accept a generic `Map<String, Object>` payload. REST Assured serialises it to JSON automatically. Using a map (rather than a typed POJO) keeps the service layer payload-agnostic and lets step definitions construct whatever body the scenario requires.

---

### `Map<String, String>` — bulk header injection

```java
// ApiRequestSpecBuilder.java
public ApiRequestSpecBuilder withHeaders(Map<String, String> headers) {
    headers.forEach(builder::addHeader);
    return this;
}
```

Allows callers to pass an entire header map in one call instead of chaining multiple `withHeader()` calls. `forEach` with a method reference keeps the iteration concise.

---

### `Properties` — configuration loading

```java
// ConfigManager.java
private final Properties properties = new Properties();

// ApiConfig.java
private static final Properties props = new Properties();
```

`java.util.Properties` (a `Hashtable<Object,Object>` subclass) is the standard Java mechanism for reading `.properties` files. Both `ConfigManager` and `ApiConfig` load their respective files into `Properties` instances at class initialisation time (static block / constructor), then layer `-D` system property overrides on top via `System.getProperty(key, properties.getProperty(key, default))`.

---

### Summary table

| Collection | Class | Purpose |
|------------|-------|---------|
| `ConcurrentLinkedQueue<Device>` | `DeviceManager` | Thread-safe device pool for parallel test execution |
| `ThreadLocal<Device>` | `DeviceManager` | Per-thread device slot — no cross-thread leakage |
| `ThreadLocal<WebDriver>` | `DriverManager` | Per-thread driver instance — parallel-safe |
| `List<Device>` | `DeviceManager` | Temporary buffer during `devices.json` deserialisation |
| `List<WebElement>` | `BrokenLinksUtils` | Holds all anchor elements found on the page |
| `HashMap<String, Object>` | `ApiScenarioContext` | Scenario-scoped key-value store shared across step classes |
| `Map<String, Object>` (method param) | `UserService`, `OrderService` | Flexible API request payload passed to REST Assured |
| `Map<String, String>` (method param) | `ApiRequestSpecBuilder` | Bulk header injection into the request spec |
| `Properties` | `ConfigManager`, `ApiConfig` | File-backed configuration with system property override |
