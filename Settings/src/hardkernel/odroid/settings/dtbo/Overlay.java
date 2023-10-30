package hardkernel.odroid.settings.dtbo;

import hardkernel.odroid.settings.ConfigEnv;
import hardkernel.odroid.settings.util.OdroidUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Overlay {
    private static final String Path = "/fat/rockchip/overlays/";

    private static String getPath() {
        if(OdroidUtils.isOdroidM1()) {
            return Path + "odroidm1/";
        } else {
            return Path + "odroidm1s/";
        }
    }
    private static ArrayList<String> overlaysList = null;

    public static ArrayList<String> getAvailableList() {
        if (overlaysList != null)
            return overlaysList;

        overlaysList = new ArrayList<>();
        File overlaysDir = new File(getPath());
        String overlays[] = overlaysDir.list();
        for(String overlay: overlays) {
            if(overlay.endsWith(".dtbo"))
                overlaysList.add(overlay.substring(0, overlay.length() - 5));
        }

        return overlaysList;
    }

    private static String overlayString = ConfigEnv.getOverlay();

    public static void sync() {
        overlayString = ConfigEnv.getOverlay();
    }

    private static ArrayList<String> getCurrentArray() {
        return new ArrayList(Arrays.asList(overlayString.split(" ")));
    }

    public static String getCurrent() {
        return overlayString;
    }

    public static void set(String dtbo, boolean state) {
        ArrayList<String> current = getCurrentArray();
        if(state) {
            current.add(dtbo);
        } else {
            for(int i = 0; i < current.size(); i++) {
                if(current.get(i).equals(dtbo)) {
                    current.remove(i);
                    break;
                }
            }
        }

        set(current);
    }

    private static void set(ArrayList<String> list) {
        Collections.sort(list);
        overlayString = String.join(" ", list);
        ConfigEnv.setOverlay(overlayString);
    }

    public static String getSize() {
        return ConfigEnv.getOverlaySize();
    }

    public static void setSize(String size) {
        ConfigEnv.setOverlaySize(size);
    }
}
