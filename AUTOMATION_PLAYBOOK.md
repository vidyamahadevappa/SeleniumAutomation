# TatvaOps Complete End-to-End Automation Playbook
> **A Comprehensive, Self-Sufficient Guide to Writing, Running, and Maintaining Tests Without Assistance**

---

## Table of Contents
1. [Framework Architecture & Mental Model](#1-framework-architecture--mental-model)
2. [Environment & Configuration Setup](#2-environment--configuration-setup)
3. [Element Locators: Finding Reliable Selectors in SPAs](#3-element-locators-finding-reliable-selectors-in-spas)
4. [Step-by-Step: Creating a New Page Object](#4-step-by-step-creating-a-new-page-object)
5. [Step-by-Step: Creating a New Test Case](#5-step-by-step-creating-a-new-test-case)
6. [Data-Driven Testing (Excel + DataProvider)](#6-data-driven-testing-excel--dataprovider)
7. [Handling Advanced Web Automation Scenarios](#7-handling-advanced-web-automation-scenarios)
8. [Running Tests & Maven Execution Recipes](#8-running-tests--maven-execution-recipes)
9. [Debugging, Screenshots & Logs](#9-debugging-screenshots--logs)
10. [End-to-End Walkthrough: Adding a New Page & Test from Scratch](#10-end-to-end-walkthrough-adding-a-new-page--test-from-scratch)
11. [Troubleshooting & Common Exceptions Cheat Sheet](#11-troubleshooting--common-exceptions-cheat-sheet)

---

## 1. Framework Architecture & Mental Model

This framework follows the **Page Object Model (POM)** design pattern with **TestNG** and **Maven**:

```
tatva-automation/
├── pom.xml                               # Dependencies (Selenium, TestNG, POI, Logback)
├── src/
│   ├── main/java/
│   │   ├── base/
│   │   │   └── BaseTest.java             # Test lifecycle (@BeforeMethod, @AfterMethod, screenshots)
│   │   ├── pages/
│   │   │   ├── BasePage.java             # Shared driver + explicit wait wrapper methods
│   │   │   ├── LoginPage.java            # Login page elements and interactions
│   │   │   └── HomePage.java             # Post-login dashboard elements and interactions
│   │   └── utilities/
│   │       ├── BrowserFactory.java       # ThreadLocal WebDriver manager
│   │       ├── ConfigReader.java         # Reads src/test/resources/config.properties
│   │       ├── ExcelUtility.java         # Apache POI Excel reader for data-driven tests
│   │       └── ScreenshotUtility.java    # Automated screenshot capture on failure
│   └── test/
│       ├── java/
│       │   ├── tests/
│       │   │   ├── SmokeTest.java        # Health check test
│       │   │   ├── LoginTest.java        # Auth tests (positive, negative via Excel)
│       │   │   └── DashboardTest.java    # Post-login navigation tests
│       │   └── runners/
│       │       └── TestRunner.java       # Standalone runner for execution from main()
│       └── resources/
│           ├── config.properties         # URLs, credentials, timeouts, browser configs
│           ├── testng.xml                # Test suite runner & grouping definitions
│           ├── testdata.xlsx             # Test data sheets
│           └── logback.xml               # Logger config
├── screenshots/                          # Auto-saved PNGs on test failures
├── logs/                                 # Runtime logs
└── reports/                              # Test execution reports
```

### Core Design Rules
1. **Never write `driver.findElement(...)` inside test files.** All DOM interactions must stay inside Page classes.
2. **Never write assertions inside Page classes.** Page classes return data or status; tests (`@Test`) assert conditions.
3. **Always extend `BasePage` for page classes and `BaseTest` for test classes.**
4. **Use Fluent Page Navigation.** When an action navigates to another page (e.g. clicking login), the method should return an instance of that next page (e.g. `return new HomePage(driver);`).

---

## 2. Environment & Configuration Setup

All runtime parameters are centralized in `src/test/resources/config.properties`:

```properties
# ---- Application ----
baseUrl=https://tatva-direct-frontend-five.vercel.app
baseHost=vercel.app

# ---- Browser: chrome | firefox | edge ----
browser=chrome
headless=false

# ---- Timeouts (seconds) ----
implicitWait=10
explicitWait=20

# ---- Test Accounts ----
validUsername=your_test_user@example.com
validPassword=YourTestPassword123!
```

### Accessing Config in Code
Use the `ConfigReader` utility anywhere:
```java
String url = ConfigReader.get("baseUrl");
String timeout = ConfigReader.get("explicitWait", "20"); // with fallback default
```

### Overriding via Command Line
You can dynamically override settings during Maven runs without modifying the file:
```bash
mvn test -Dbrowser=firefox -Dheadless=true
```

---

## 3. Element Locators: Finding Reliable Selectors in SPAs

TatvaOps is a Single Page Application (React/Next.js/Vite). In SPAs, generated CSS classes (e.g., `css-1a2b3c`, `sc-bdVaJa`) change between builds and cause tests to break.

### Best-to-Worst Locator Priority
1. **`data-testid` or `data-cy` attributes (BEST & MOST STABLE)**:
   ```java
   private final By submitBtn = By.cssSelector("[data-testid='submit-button']");
   ```
2. **Fixed ID or Name**:
   ```java
   private final By emailInput = By.id("email");
   private final By searchBox = By.name("q");
   ```
3. **Semantic CSS Attributes**:
   ```java
   private final By roleBtn = By.cssSelector("button[type='submit']");
   private final By placeholder = By.cssSelector("input[placeholder='Enter email']");
   ```
4. **XPath by Visible Text or Partial Text**:
   ```java
   private final By saveBtn = By.xpath("//button[normalize-space()='Save Changes']");
   private final By menuLink = By.xpath("//nav//a[contains(text(),'Orders')]");
   ```
5. **DO NOT USE**: Absolute XPaths (`/html/body/div[2]/div/div[1]/button`) or generated hashes (`.btn_3h4j5k`).

---

## 4. Step-by-Step: Creating a New Page Object

To add automation for any new screen:

### Step 1: Create the file in `src/main/java/pages/`
Name it after the feature or page: `<FeatureName>Page.java` (e.g., `OrdersPage.java`).

### Step 2: Extend `BasePage` and declare locators
Keep all locators `private final By`.

```java
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OrdersPage extends BasePage {

    // 1. Locators
    private final By pageTitle       = By.cssSelector("[data-testid='orders-title']");
    private final By createOrderBtn  = By.cssSelector("[data-testid='create-order-btn']");
    private final By searchInput     = By.cssSelector("[data-testid='search-orders']");
    private final By orderTableRows  = By.cssSelector("table tbody tr");

    // 2. Constructor
    public OrdersPage(WebDriver driver) {
        super(driver);
    }

    // 3. Page Verification / Guard Check
    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }

    // 4. Action Methods
    public OrdersPage searchOrder(String orderId) {
        type(searchInput, orderId);
        return this;
    }

    public CreateOrderModal clickCreateOrder() {
        click(createOrderBtn);
        return new CreateOrderModal(driver);
    }

    public int getOrderCount() {
        return driver.findElements(orderTableRows).size();
    }
}
```

### Inherited Methods Available from `BasePage`
Because you extend `BasePage`, you don't need to reinvent waits:
- `click(By locator)`: Waits until clickable, then clicks.
- `type(By locator, String text)`: Waits until visible, clears the input, and sends keys.
- `getText(By locator)`: Waits until visible and returns inner text.
- `isDisplayed(By locator)`: Returns `true` if visible, `false` otherwise (safe; never crashes).
- `waitForVisible(By locator)`: Explicitly waits and returns `WebElement`.
- `waitForClickable(By locator)`: Explicitly waits for clickability and returns `WebElement`.

---

## 5. Step-by-Step: Creating a New Test Case

### Step 1: Create the test class in `src/test/java/tests/`
Name it `<FeatureName>Test.java` (e.g., `OrdersTest.java`).

### Step 2: Extend `BaseTest`
`BaseTest` automatically starts a browser before every `@Test` method and closes it after, taking a screenshot if the test fails.

```java
package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;
import pages.OrdersPage;
import utilities.ConfigReader;

public class OrdersTest extends BaseTest {

    // Helper method to authenticate
    private HomePage login() {
        return new LoginPage(driver).open().login(
                ConfigReader.get("validUsername"),
                ConfigReader.get("validPassword")
        );
    }

    @Test(groups = {"smoke", "regression"}, description = "Verify orders page is accessible")
    public void testOrdersPageLoads() {
        HomePage home = login();
        Assert.assertTrue(home.isLoaded(), "Home page failed to load");

        // Navigate to Orders (assume home has a goToOrders method)
        OrdersPage orders = home.goToOrders();
        Assert.assertTrue(orders.isLoaded(), "Orders page failed to load");
    }

    @Test(groups = {"regression"}, description = "Filter orders by order ID")
    public void testFilterOrders() {
        HomePage home = login();
        OrdersPage orders = home.goToOrders();

        orders.searchOrder("ORD-1002");
        Assert.assertTrue(orders.getOrderCount() >= 1, "Expected at least 1 order matching ORD-1002");
    }
}
```

### Step 3: Register in `src/test/resources/testng.xml`
Add your new test class under the `<classes>` block:
```xml
<class name="tests.OrdersTest"/>
```

---

## 6. Data-Driven Testing (Excel + DataProvider)

To run the same test against multiple sets of data (e.g., 10 invalid credentials, bulk inputs):

### Step 1: Populate `src/test/resources/testdata.xlsx`
Create a sheet (e.g. `NewOrders`) with column headers:
| customerName | itemSku | quantity |
| :--- | :--- | :--- |
| Acme Corp | SKU-99 | 5 |
| Beta LLC | SKU-10 | 2 |

### Step 2: Hook up `@DataProvider` in your test
```java
@Test(dataProvider = "orderData", groups = {"regression"})
public void testBulkOrderCreation(Map<String, String> data) {
    String customer = data.get("customerName");
    String sku = data.get("itemSku");
    String qty = data.get("quantity");

    // Perform actions with data
}

@DataProvider(name = "orderData")
public Object[][] getOrderData() {
    return ExcelUtility.getDataProviderArray(
        "src/test/resources/testdata.xlsx", 
        "NewOrders"
    );
}
```

---

## 7. Handling Advanced Web Automation Scenarios

Add these patterns directly into your Page classes when handling complex SPA components:

### 1. Handling Select / Dropdowns
```java
import org.openqa.selenium.support.ui.Select;

public void selectDropdownByVisibleText(By locator, String text) {
    Select select = new Select(waitForVisible(locator));
    select.selectByVisibleText(text);
}
```

### 2. Handling Custom React/Custom UI Dropdowns
```java
public void selectCustomDropdown(By trigger, By optionToSelect) {
    click(trigger);
    click(optionToSelect);
}
```

### 3. JavaScript Click (for elements covered by sticky headers or overlays)
```java
import org.openqa.selenium.JavascriptExecutor;

public void jsClick(By locator) {
    WebElement el = waitForVisible(locator);
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
}
```

### 4. Scrolling an Element into View
```java
public void scrollToElement(By locator) {
    WebElement el = waitForVisible(locator);
    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
}
```

### 5. Waiting for Spinners / Loaders to Disappear
```java
public void waitForLoaderToDisappear(By loaderLocator) {
    wait.until(ExpectedConditions.invisibilityOfElementLocated(loaderLocator));
}
```

### 6. Mouse Hover (Actions)
```java
import org.openqa.selenium.interactions.Actions;

public void hoverOverElement(By locator) {
    WebElement el = waitForVisible(locator);
    new Actions(driver).moveToElement(el).perform();
}
```

### 7. Handling Browser Alerts
```java
public void acceptAlert() {
    wait.until(ExpectedConditions.alertIsPresent()).accept();
}
```

### 8. Handling Multiple Windows / Tabs
```java
public void switchToNewTab() {
    String originalWindow = driver.getWindowHandle();
    for (String handle : driver.getWindowHandles()) {
        if (!handle.equals(originalWindow)) {
            driver.switchTo().window(handle);
            break;
        }
    }
}
```

---

## 8. Running Tests & Maven Execution Recipes

Open terminal inside `c:\tatva-automation` and use any of these commands:

### Run Everything in `testng.xml`
```bash
mvn clean test
```

### Run Only Smoke Tests
```bash
mvn test -Dgroups=smoke
```

### Run Only Regression Tests
```bash
mvn test -Dgroups=regression
```

### Run a Single Test Class
```bash
mvn test -Dtest=LoginTest
```

### Run a Single Test Method
```bash
mvn test -Dtest=LoginTest#validLogin
```

### Run Headless in CI/Background
```bash
mvn test -Dheadless=true
```

### Run on a Different Browser
```bash
mvn test -Dbrowser=firefox
# or
mvn test -Dbrowser=edge
```

---

## 9. Debugging, Screenshots & Logs

### 1. Screenshots on Failure
`BaseTest.java` automatically triggers `ScreenshotUtility` when an assertion fails or an uncaught exception occurs:
- Location: `c:\tatva-automation\screenshots\`
- File name format: `<TestMethodName>_YYYYMMDD_HHmmss.png`
- You can inspect these images immediately to see what was on the screen at the exact second the failure occurred.

### 2. Execution Logs
- Location: `c:\tatva-automation\logs\`
- Use `System.out.println` or configure SLF4J / Logback to record trace details.

### 3. Surefire HTML Reports
After running `mvn test`, open:
`c:\tatva-automation\target\surefire-reports\index.html` in any browser to view test pass/fail metrics, duration, and error traces.

---

## 10. End-to-End Walkthrough: Adding a New Page & Test from Scratch

Follow this exact recipe whenever a developer gives you a new page to automate:

### Scenario: Automating the "Supplier Profile" page

#### 1. Inspect DOM & Create `src/main/java/pages/ProfilePage.java`
```java
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProfilePage extends BasePage {

    private final By profileHeader   = By.cssSelector("[data-testid='profile-header']");
    private final By companyNameInput= By.cssSelector("[data-testid='company-name-input']");
    private final By phoneInput      = By.cssSelector("[data-testid='phone-input']");
    private final By saveButton      = By.cssSelector("[data-testid='save-profile-btn']");
    private final By successAlert    = By.cssSelector("[data-testid='toast-success']");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isDisplayed(profileHeader);
    }

    public ProfilePage updateContactDetails(String companyName, String phone) {
        type(companyNameInput, companyName);
        type(phoneInput, phone);
        click(saveButton);
        return this;
    }

    public boolean isSuccessMessageDisplayed() {
        return isDisplayed(successAlert);
    }
}
```

#### 2. Connect Navigation in `src/main/java/pages/HomePage.java`
Add a method in `HomePage` so tests can transition naturally:
```java
private final By profileNavLink = By.cssSelector("[data-testid='nav-profile']");

public ProfilePage goToProfile() {
    click(profileNavLink);
    return new ProfilePage(driver);
}
```

#### 3. Create the Test in `src/test/java/tests/ProfileTest.java`
```java
package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;
import pages.ProfilePage;
import utilities.ConfigReader;

public class ProfileTest extends BaseTest {

    @Test(groups = {"regression"}, description = "Supplier can update profile details")
    public void testUpdateProfile() {
        HomePage home = new LoginPage(driver).open().login(
                ConfigReader.get("validUsername"),
                ConfigReader.get("validPassword")
        );
        Assert.assertTrue(home.isLoaded(), "Failed to reach home dashboard");

        ProfilePage profile = home.goToProfile();
        Assert.assertTrue(profile.isLoaded(), "Failed to load profile page");

        profile.updateContactDetails("Tatva Logistics Ltd", "+919876543210");
        Assert.assertTrue(profile.isSuccessMessageDisplayed(), "Success toast did not appear");
    }
}
```

#### 4. Add to `testng.xml`
```xml
<class name="tests.ProfileTest"/>
```

#### 5. Execute & Validate
```bash
mvn test -Dtest=ProfileTest
```

---

## 11. Troubleshooting & Common Exceptions Cheat Sheet

| Exception | Root Cause | Solution |
| :--- | :--- | :--- |
| **`NoSuchElementException`** | Element not in the DOM or selector is incorrect. | 1. Check if the selector matches the current DOM in DevTools.<br>2. Check if the element is inside an `<iframe>` or Shadow DOM.<br>3. Ensure you used `waitForVisible(locator)`. |
| **`TimeoutException`** | Element did not appear within `explicitWait` (20s). | 1. Check if an API/network call failed.<br>2. Increase `explicitWait` in `config.properties`.<br>3. Verify the locator isn't waiting on an obsolete tag. |
| **`ElementClickInterceptedException`** | Another element (modal, spinner, toast, sticky navbar) is on top of the target. | 1. Use `waitForLoaderToDisappear(...)`.<br>2. Scroll element into view with `scrollToElement(...)`.<br>3. Use fallback `jsClick(locator)`. |
| **`StaleElementReferenceException`** | Page re-rendered / DOM updated after element was located. | Do NOT store `WebElement` references as class variables. Always locate elements at the moment of action via `By` locators with `waitForVisible(...)`. |
| **`NullPointerException` on `driver`** | Forgot `super(driver)` in page constructor, or test class did not extend `BaseTest`. | Ensure page constructors call `super(driver);` and test classes extend `BaseTest`. |
| **SessionNotCreatedException** | Local browser version incompatible with driver. | Selenium 4.6+ handles this via Selenium Manager automatically. Update your local browser (Chrome/Edge/Firefox) to the latest stable release. |

---

*Keep this playbook as your standard operating procedure for writing, extending, and maintaining tests in TatvaOps.*
