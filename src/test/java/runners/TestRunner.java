package runners;

import org.testng.TestNG;

import java.util.Collections;

/**
 * Optional programmatic runner. TestNG is normally driven via testng.xml
 * (see src/test/resources/testng.xml) either from Maven Surefire or the IDE.
 *
 * This class lets you run the suite from a plain main() if needed, e.g.
 * during quick local debugging inside Antigravity.
 */
public class TestRunner {

    public static void main(String[] args) {
        TestNG testng = new TestNG();
        testng.setTestSuites(Collections.singletonList("src/test/resources/testng.xml"));
        testng.run();
    }
}
