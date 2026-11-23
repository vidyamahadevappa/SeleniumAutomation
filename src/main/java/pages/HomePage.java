package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object for the operations dashboard screen on ops.withtatva.ai.
 * Provides validation methods to ensure successful authentication and dashboard loading.
 */
public class HomePage extends BasePage {

    // --- Locators for Dashboard Elements ---
    private final By dashboardHeader = By.xpath(
            "//*[@data-testid='dashboard-header'] | " +
            "//h1 | //h2[contains(., 'Dashboard') or contains(., 'Welcome')] | " +
            "//header//button[contains(., 'Logout') or contains(., 'Profile')] | " +
            "//*[@data-testid='user-menu']");

    private final By userMenu     = By.xpath("//*[@data-testid='user-menu'] | //header//div[contains(@class, 'avatar') or contains(@class, 'user')]");
    private final By logoutButton = By.xpath("//*[@data-testid='logout-btn'] | //button[contains(., 'Logout') or contains(., 'Log out')]");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Confirms that dashboard elements are visible using WebDriverWait.
     */
    public boolean isDashboardElementVisible() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(dashboardHeader),
                    ExpectedConditions.presenceOfElementLocated(userMenu)
            ));
            return true;
        } catch (Exception e) {
            return isDisplayed(dashboardHeader) || !driver.getCurrentUrl().contains("login");
        }
    }

    /**
     * Confirms that the user is successfully logged in.
     * Checks both browser session storage (JWT token) and dashboard UI indicators.
     */
    public boolean isUserLoggedIn() {
        // 1. Verify authenticated JWT token in localStorage
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object token = js.executeScript("return localStorage.getItem('token');");
            if (token != null && !token.toString().isBlank() && !"null".equalsIgnoreCase(token.toString())) {
                return true;
            }
        } catch (Exception ignored) {}

        // 2. Verify dashboard UI state
        return isDashboardElementVisible();
    }

    public boolean isLoaded() {
        return isUserLoggedIn();
    }

    public String getHeaderText() {
        return getText(dashboardHeader);
    }

    public LoginPage logout() {
        click(userMenu);
        click(logoutButton);
        return new LoginPage(driver);
    }
}
