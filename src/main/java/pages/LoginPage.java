package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page object for the login screen.
 *
 * IMPORTANT: The locators below are PLACEHOLDERS. Since TatvaOps is an SPA,
 * class names are often hashed/generated and change between builds. Replace
 * these with the stable data-testid attributes you are requesting from the
 * developers (e.g. By.cssSelector("[data-testid='login-email']")).
 */
public class LoginPage extends BasePage {

    // --- Locators (UPDATE to match real DOM / data-testid attributes) ---
    private final By emailInput    = By.cssSelector("[data-testid='login-email']");
    private final By passwordInput = By.cssSelector("[data-testid='login-password']");
    private final By loginButton   = By.cssSelector("[data-testid='login-submit']");
    private final By errorMessage  = By.cssSelector("[data-testid='login-error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(utilities.ConfigReader.get("baseUrl"));
        return this;
    }

    public HomePage login(String email, String password) {
        type(emailInput, email);
        type(passwordInput, password);
        click(loginButton);
        return new HomePage(driver);
    }

    public void loginExpectingFailure(String email, String password) {
        type(emailInput, email);
        type(passwordInput, password);
        click(loginButton);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }

    public String getErrorText() {
        return getText(errorMessage);
    }

    public boolean isLoaded() {
        return isDisplayed(emailInput) && isDisplayed(loginButton);
    }
}
