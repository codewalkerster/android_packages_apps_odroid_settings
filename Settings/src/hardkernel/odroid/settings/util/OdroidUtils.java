package hardkernel.odroid.settings.util;

import hardkernel.odroid.settings.EnvProperty;

public class OdroidUtils {
    public static boolean isOdroidM1() {
        return getModel().equals("odroidm1");
    }

    public static boolean isOdroidM1S() {
        return getModel().equals("odroidm1s");
    }

    public static boolean isOdroidM2() {
        return getModel().equals("odroidm2");
    }

    private static String model = null;

    private static String getModel() {
        if (model != null)
            return model;

        model = EnvProperty.get("ro.product.name", "odroidm1");

        return model;
    }
}
