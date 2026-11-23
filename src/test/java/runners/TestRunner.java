package runners;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import tests.OtpLoginTest;

/**
 * Programmatic test runner to execute tests from main() inside IDE.
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" Starting TatvaOps OTP Login Automation Runner ");
        System.out.println("==================================================");

        try {
            TestNG testng = new TestNG();
            testng.setVerbose(2);

            TestListenerAdapter tla = new TestListenerAdapter();
            testng.addListener(tla);

            // Run OtpLoginTest directly for focused execution
            System.out.println("[INFO] Executing tests.OtpLoginTest...");
            testng.setTestClasses(new Class[] { OtpLoginTest.class });

            testng.run();

            System.out.println("==================================================");
            System.out.println(" Test Execution Summary:");
            System.out.println(" Passed : " + tla.getPassedTests().size());
            System.out.println(" Failed : " + tla.getFailedTests().size());
            System.out.println(" Skipped: " + tla.getSkippedTests().size());
            System.out.println("==================================================");

        } catch (Throwable t) {
            System.err.println("[FATAL] Error running tests: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
