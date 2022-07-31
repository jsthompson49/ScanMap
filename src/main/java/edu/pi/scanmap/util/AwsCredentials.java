package main.java.edu.pi.scanmap.util;

public class AwsCredentials {

    private static final String ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
    private static final String SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";

    private final String accessKeyId;
    private final String secretAccessKey;

    public static AwsCredentials create() {
        final String accessKeyId = getPropertyOrEnv(ACCESS_KEY_ID);
        final String secretAccessKey = getPropertyOrEnv(SECRET_ACCESS_KEY);
        //System.out.println(String.format("id=%s secret=%s", accessKeyId, secretAccessKey));
        return new AwsCredentials(accessKeyId, secretAccessKey);
    }

    private AwsCredentials(String accessKeyId, String secretAccessKey) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    private static String getPropertyOrEnv(final String key) {
        final String value = System.getProperty(key);
        if (value != null) {
            return value;
        }

        return System.getenv(key);
    }
}
