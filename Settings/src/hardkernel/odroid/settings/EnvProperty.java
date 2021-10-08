package hardkernel.odroid.settings;

import android.os.SystemProperties;

public class EnvProperty {
    public static int getInt(String key, int defaultValue) {
        return SystemProperties.getInt(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return SystemProperties.getBoolean(key, defaultValue);
    }

    public static String get (String key) {
        return SystemProperties.get(key);
    }

    public static String get (String key, String defaultValue) {
        return SystemProperties.get(key, defaultValue);
    }

    public static boolean set (String key, boolean value) {
        SystemProperties.set(key, String.valueOf(value));

        return true;
    }

    public static boolean set (String key, String value) {
        SystemProperties.set(key, value);

        return true;
    }
}
