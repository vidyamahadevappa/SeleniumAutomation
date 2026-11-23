package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads key/value pairs from config.properties.
 * Resolves via classpath first, then falls back to relative and absolute
 * workspace paths.
 */
public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        InputStream is = null;
        try {
            // 1. Try ClassLoader (standard for resources in src/test/resources)
            is = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties");
            if (is == null) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
            }

            // 2. Try relative file path
            if (is == null) {
                File relFile = new File("src/test/resources/config.properties");
                if (relFile.exists()) {
                    is = new FileInputStream(relFile);
                }
            }

            // 3. Try workspace root fallback
            if (is == null) {
                File absFile = new File("c:/tatva-automation/src/test/resources/config.properties");
                if (absFile.exists()) {
                    is = new FileInputStream(absFile);
                }
            }

            if (is != null) {
                properties.load(is);
            } else {
                System.err.println("[WARN] config.properties could not be located via classpath or file paths.");
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load config.properties: " + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Key '" + key + "' not found in config.properties");
        }
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue).trim();
    }
}
