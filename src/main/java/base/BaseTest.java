package base;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import utilities.BrowserFactory;
import utilities.ScreenshotUtility;

import java.lang.reflect.Method;

/**
 * Parent of all test classes. Handles WebDriver setup/teardown per test method
 * and captures a screenshot automatically when a test fails.
 */
public class BaseTest {

    protected WebDriver driver;

    @BeforeMethod
    public void setUp(Method method) {
        driver = BrowserFactory.initDriver();
        System.out.println("[START] " + method.getName());
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            String path = ScreenshotUtility.capture(driver, result.getName());
            System.out.println("[FAIL] " + result.getName() + " | screenshot: " + path);
        } else {
            System.out.println("[PASS] " + result.getName());
        }
        BrowserFactory.quitDriver();
    }
}
