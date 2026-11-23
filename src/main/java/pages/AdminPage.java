package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utilities.ConfigReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Page Object representing the TatvaOps Admin Portal (admin.ops.withtatva.ai).
 * Handles switching to Password Login, admin authentication, dashboard verification,
 * navigating to OTP Records, and extracting the latest OTP code.
 */
public class AdminPage extends BasePage {

    // --- Tab Switcher (OTP Login vs Password Login) ---
    private final By passwordLoginTab = By.xpath(
            "//button[contains(., 'Password Login')] | " +
            "//*[contains(text(), 'Password Login')]");

    // --- Locators for Admin Password Login ---
    private final By emailInput = By.xpath(
            "//input[@id='email' or @type='email' or @name='email' or contains(@placeholder, 'Email') or contains(@placeholder, 'email')]");
    private final By passwordInput = By.xpath(
            "//input[@id='password' or @type='password' or @name='password' or contains(@placeholder, 'Password') or contains(@placeholder, 'password')]");
    // Only match type='submit' — avoids clicking "OTP Login" / "Password Login" tab toggles
    private final By loginButton = By.xpath("//button[@type='submit']");

    // --- Locators for Admin Dashboard & Navigation ---
    private final By otpRecordMenu = By.xpath(
            "//a[@href='/otp-records'] | " +
            "//a[contains(., 'OTP Records') or contains(., 'OTP Record') or contains(., 'OTP')] | " +
            "//aside//a[contains(., 'OTP')] | " +
            "//nav//span[contains(., 'OTP')]/..");

    // Post-login-only indicators — the login page has <h2>Admin Dashboard</h2> so we must NOT match that
    private final By dashboardIndicator = By.xpath(
            "//a[@href='/otp-records'] | " +
            "//a[@href='/dashboard'] | " +
            "//button[@aria-label='Logout' or @title='Logout']");

    // --- Locators for OTP Table ---
    private final By otpTableRows = By.xpath("//table//tbody//tr");

    public AdminPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigates to the Admin Portal URL.
     */
    public AdminPage open() {
        driver.get(ConfigReader.get("adminUrl"));
        return this;
    }

    /**
     * Enters admin credentials and clicks login.
     * Clicks the 'Password Login' tab if present (since Admin defaults to OTP Login).
     */
    public AdminPage login(String email, String password) {
        // Step 1: Switch to Password Login tab if active view is OTP login
        try {
            WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(passwordLoginTab));
            tab.click();
            System.out.println("[AdminPage] Clicked 'Password Login' tab.");
        } catch (Exception e) {
            System.out.println("[AdminPage] Password Login tab click skipped: " + e.getMessage());
        }

        // Step 2: Wait for email input to appear, then fill credentials
        WebElement emailEl = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        emailEl.clear();
        emailEl.sendKeys(email);
        System.out.println("[AdminPage] Entered email: " + email);

        WebElement passEl = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        passEl.clear();
        passEl.sendKeys(password);
        System.out.println("[AdminPage] Entered password.");

        // Step 3: Find and click the VISIBLE submit button only.
        // Using findElements + visibility filter avoids accidentally clicking
        // the "OTP Login" or "Password Login" tab-toggle buttons.
        WebElement submitBtn = wait.until(d -> {
            for (WebElement btn : d.findElements(loginButton)) {
                if (btn.isDisplayed()) {
                    return btn;
                }
            }
            return null;
        });
        submitBtn.click();
        System.out.println("[AdminPage] Clicked submit button. Waiting for dashboard...");

        // Step 4: Wait for a post-login-only element to confirm authentication.
        // The sidebar link to /otp-records ONLY exists after successful login.
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(otpRecordMenu),
                ExpectedConditions.presenceOfElementLocated(dashboardIndicator)
        ));
        System.out.println("[AdminPage] Dashboard loaded. URL: " + driver.getCurrentUrl());
        return this;
    }

    /**
     * Verifies that the admin dashboard loaded successfully.
     */
    public boolean isDashboardLoaded() {
        try {
            // Only check for truly post-login elements — URL always contains "admin"
            return isDisplayed(otpRecordMenu) || isDisplayed(dashboardIndicator);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Navigates to the OTP Records section from the left navigation panel.
     */
    public AdminPage navigateToOtpRecords() {
        waitForClickable(otpRecordMenu);
        click(otpRecordMenu);
        wait.until(ExpectedConditions.presenceOfElementLocated(otpTableRows));
        return this;
    }

    /**
     * Locates the latest OTP associated with the registered mobile number.
     * Uses WebDriverWait to wait for the row containing the mobile number.
     *
     * @param mobileNumber the registered mobile number (e.g. 8959896246)
     * @return the extracted latest OTP code
     */
    public String getLatestOtpForMobile(String mobileNumber) {
        Pattern otpPattern = Pattern.compile("\\b\\d{4,6}\\b");
        By targetRow = By.xpath("//table//tbody//tr[contains(., '" + mobileNumber + "')]");

        try {
            WebElement row = wait.until(ExpectedConditions.presenceOfElementLocated(targetRow));
            String rowText = row.getText();
            Matcher matcher = otpPattern.matcher(rowText);
            while (matcher.find()) {
                String token = matcher.group();
                if (!mobileNumber.contains(token)) {
                    return token;
                }
            }
        } catch (Exception e) {
            // Refresh table once if newly created OTP hasn't appeared yet
            driver.navigate().refresh();
            WebElement row = wait.until(ExpectedConditions.presenceOfElementLocated(targetRow));
            String rowText = row.getText();
            Matcher matcher = otpPattern.matcher(rowText);
            while (matcher.find()) {
                String token = matcher.group();
                if (!mobileNumber.contains(token)) {
                    return token;
                }
            }
        }

        throw new RuntimeException("Could not find OTP for mobile number: " + mobileNumber + " in Admin OTP Records.");
    }
}
