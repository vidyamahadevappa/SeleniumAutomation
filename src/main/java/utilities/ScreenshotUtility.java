package utilities;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Captures screenshots into the /screenshots directory.
 * File name pattern: <testName>_<timestamp>.png
 */
public class ScreenshotUtility {

    private static final String SCREENSHOT_DIR = "screenshots";
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    public static String capture(WebDriver driver, String testName) {
        if (driver == null) {
            return null;
        }
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String fileName = testName + "_" + LocalDateTime.now().format(TS) + ".png";
            Path dir = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(dir);
            Path dest = dir.resolve(fileName);
            Files.copy(src.toPath(), dest);
            return dest.toString();
        } catch (Exception e) {
            System.err.println("Screenshot capture failed: " + e.getMessage());
            return null;
        }
    }
}
