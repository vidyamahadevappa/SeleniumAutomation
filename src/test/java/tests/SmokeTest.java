package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import utilities.ConfigReader;

public class SmokeTest extends BaseTest {

    @Test(groups = {"smoke"}, description = "Application is reachable and login page renders")
    public void appIsReachable() {
        LoginPage login = new LoginPage(driver).open();
        Assert.assertTrue(login.isLoaded(), "Login page failed to render");
        Assert.assertTrue(driver.getCurrentUrl().contains(
                ConfigReader.get("baseHost", "vercel.app")),
                "Unexpected URL: " + driver.getCurrentUrl());
    }
}
