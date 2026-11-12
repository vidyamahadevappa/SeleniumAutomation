package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;
import utilities.ConfigReader;

public class DashboardTest extends BaseTest {

    private HomePage loginAsSupplier() {
        return new LoginPage(driver).open().login(
                ConfigReader.get("validUsername"),
                ConfigReader.get("validPassword"));
    }

    @Test(groups = {"regression"}, description = "Dashboard header is visible after login")
    public void dashboardLoads() {
        HomePage home = loginAsSupplier();
        Assert.assertTrue(home.isLoaded(), "Dashboard not loaded");
    }

    @Test(groups = {"regression"}, description = "Logout returns user to login page")
    public void logoutReturnsToLogin() {
        HomePage home = loginAsSupplier();
        LoginPage login = home.logout();
        Assert.assertTrue(login.isLoaded(), "Did not return to login page after logout");
    }
}
