package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;
import utilities.ConfigReader;
import utilities.ExcelUtility;

import java.util.Map;

public class LoginTest extends BaseTest {

    @Test(groups = {"smoke"}, description = "Valid supplier login lands on dashboard")
    public void validLogin() {
        LoginPage login = new LoginPage(driver).open();
        Assert.assertTrue(login.isLoaded(), "Login page did not load");

        HomePage home = login.login(
                ConfigReader.get("validUsername"),
                ConfigReader.get("validPassword"));

        Assert.assertTrue(home.isLoaded(), "Dashboard did not load after valid login");
    }

    @Test(groups = {"regression"}, description = "Invalid credentials show an error",
          dataProvider = "invalidCredentials")
    public void invalidLogin(Map<String, String> data) {
        LoginPage login = new LoginPage(driver).open();
        login.loginExpectingFailure(data.get("email"), data.get("password"));
        Assert.assertTrue(login.isErrorDisplayed(),
                "Expected error message for input row: " + data);
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        // Reads rows from the 'InvalidLogins' sheet of testdata.xlsx
        return ExcelUtility.getDataProviderArray(
                "src/test/resources/testdata.xlsx", "InvalidLogins");
    }
}
