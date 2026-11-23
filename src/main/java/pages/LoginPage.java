package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utilities.ConfigReader;

import java.util.List;

/**
 * Page Object for the TatvaOps operations website (ops.withtatva.ai).
 * Handles clicking the login feature, entering mobile number, requesting OTP,
 * verifying the OTP screen, entering OTP, and submitting verification.
 * 
 * Uses visible-element filtering to handle responsive (desktop/mobile) DOM duplicates gracefully.
 */
public class LoginPage extends BasePage {

    // --- Header User Profile Icon (Top-Right) ---
    private final By headerUserButton = By.xpath(
            "//header//button[@aria-label='User' or @aria-label='Vendor'] | " +
            "//button[@aria-label='User' or @aria-label='Vendor'] | " +
            "//header//button[contains(@class, 'rounded-full') and .//*[name()='svg']]");

    // --- Dropdown Menu 'Login' Option ---
    private final By menuLoginOption = By.xpath(
            "//div[contains(@class, 'w-56')]//button[contains(., 'Login')] | " +
            "//header//div[contains(@class, 'absolute')]//button[contains(., 'Login')] | " +
            "//button[contains(., 'Login')] | " +
            "//span[normalize-space()='Login']/ancestor::button");

    // --- Sign In Modal Elements ---
    private final By mobileInput = By.xpath(
            "//input[@id='signin-phone'] | " +
            "//input[@placeholder='Enter mobile number'] | " +
            "//input[@type='tel']");

    private final By sendOtpButton = By.xpath(
            "//button[contains(., 'Send OTP')] | " +
            "//button[.//span[contains(text(), 'Send OTP')]]");

    private final By otpInput = By.xpath(
            "//input[@id='signin-otp'] | " +
            "//input[@placeholder='Enter 6-digit OTP']");

    private final By verifyOtpButton = By.xpath(
            "//button[contains(., 'Verify & continue') or contains(., 'Verify')] | " +
            "//button[.//span[contains(text(), 'Verify')]]");

    // --- Error Indicator (must NOT match success toasts like "OTP sent successfully") ---
    private final By inlineErrorAlert = By.xpath(
            "//div[(contains(@class, 'bg-red') or contains(@class, 'text-red') or contains(@class, 'bg-destructive'))] | " +
            "//*[@data-testid='login-error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(ConfigReader.get("baseUrl"));
        return this;
    }

    /**
     * Clicks the user avatar in the top-right header, then clicks 'Login' from the popup menu.
     * Selects the visible Login button to handle responsive duplicate elements cleanly.
     */
    public LoginPage clickTopRightLogin() {
        // Step 1: Click the User icon in header
        waitForVisible(headerUserButton);
        click(headerUserButton);

        // Step 2: Find and click the visible 'Login' option
        wait.until(d -> {
            List<WebElement> options = d.findElements(menuLoginOption);
            for (WebElement option : options) {
                if (option.isDisplayed()) {
                    option.click();
                    return true;
                }
            }
            return false;
        });

        // Step 3: Wait for mobile input to appear in modal
        wait.until(d -> {
            for (WebElement el : d.findElements(mobileInput)) {
                if (el.isDisplayed()) return true;
            }
            return false;
        });
        return this;
    }

    /**
     * Enters the registered mobile number into the visible mobile input field.
     */
    public LoginPage enterMobileNumber(String mobileNumber) {
        WebElement phoneEl = wait.until(d -> {
            for (WebElement el : d.findElements(mobileInput)) {
                if (el.isDisplayed()) return el;
            }
            return null;
        });
        phoneEl.clear();
        phoneEl.sendKeys(mobileNumber);
        return this;
    }

    /**
     * Clicks the visible 'Send OTP' button.
     */
    public LoginPage clickSendOtp() {
        WebElement sendBtn = wait.until(d -> {
            for (WebElement el : d.findElements(sendOtpButton)) {
                if (el.isDisplayed()) return el;
            }
            return null;
        });
        sendBtn.click();

        // Wait until OTP input field appears
        wait.until(d -> {
            for (WebElement el : d.findElements(otpInput)) {
                if (el.isDisplayed()) return true;
            }
            return false;
        });
        return this;
    }

    /**
     * Helper to enter mobile number and request OTP in one step.
     */
    public LoginPage requestOtp(String mobileNumber) {
        enterMobileNumber(mobileNumber);
        clickSendOtp();
        return this;
    }

    /**
     * Validates that the OTP verification screen is visible.
     */
    public boolean isOtpScreenDisplayed() {
        for (WebElement el : driver.findElements(otpInput)) {
            if (el.isDisplayed()) return true;
        }
        return false;
    }

    /**
     * Enters the retrieved OTP into the visible OTP input field.
     */
    public LoginPage enterOtp(String otp) {
        WebElement input = wait.until(d -> {
            for (WebElement el : d.findElements(otpInput)) {
                if (el.isDisplayed()) return el;
            }
            return null;
        });
        input.clear();
        input.sendKeys(otp);
        return this;
    }

    /**
     * Clicks the visible Verify / Submit OTP button and waits for the modal to close.
     */
    public HomePage submitOtp() {
        // Find and click the visible "Verify & continue" button
        WebElement verifyBtn = wait.until(d -> {
            for (WebElement btn : d.findElements(verifyOtpButton)) {
                if (btn.isDisplayed()) {
                    return btn;
                }
            }
            return null;
        });
        verifyBtn.click();
        System.out.println("[LoginPage] Clicked 'Verify & continue' button.");

        // Wait for OTP modal to close (indicates verification completed)
        try {
            wait.until(d -> {
                for (WebElement el : d.findElements(otpInput)) {
                    if (el.isDisplayed()) return false;
                }
                return true;
            });
            System.out.println("[LoginPage] OTP modal closed after verification.");
        } catch (Exception e) {
            System.out.println("[LoginPage] OTP modal may still be visible: " + e.getMessage());
        }
        return new HomePage(driver);
    }

    /**
     * Checks if any login error message is displayed.
     */
    public boolean isErrorMessageDisplayed() {
        for (WebElement el : driver.findElements(inlineErrorAlert)) {
            if (el.isDisplayed()) return true;
        }
        return false;
    }

    /**
     * Retrieves the text of the displayed login error message, if any.
     */
    public String getErrorMessage() {
        for (WebElement el : driver.findElements(inlineErrorAlert)) {
            if (el.isDisplayed()) return el.getText();
        }
        return "";
    }

    public boolean isLoaded() {
        return isDisplayed(headerUserButton) || isDisplayed(mobileInput);
    }
}
