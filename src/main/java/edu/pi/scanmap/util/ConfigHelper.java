package main.java.edu.pi.scanmap.util;

public class ConfigHelper {

    private ConfigHelper() {
    }

    public static String getPropertyOrEnv(final String key) {
        final String value = System.getProperty(key);
        if (value != null) {
            return value;
        }

        return System.getenv(key);
    }

    public static String getPropertyOrEnv(final String key, final String defaultValue) {
        final String value = getPropertyOrEnv(key);

        return (value == null) ? defaultValue : value;
    }
}
