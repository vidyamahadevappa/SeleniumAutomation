package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page object for the post-login home / dashboard.
 *
 * Locators are PLACEHOLDERS — replace with real data-testid selectors.
 */
public class HomePage extends BasePage {

    // --- Locators (UPDATE to match real DOM) ---
    private final By dashboardHeader = By.cssSelector("[data-testid='dashboard-header']");
    private final By userMenu        = By.cssSelector("[data-testid='user-menu']");
    private final By logoutButton    = By.cssSelector("[data-testid='logout-btn']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isDisplayed(dashboardHeader);
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
