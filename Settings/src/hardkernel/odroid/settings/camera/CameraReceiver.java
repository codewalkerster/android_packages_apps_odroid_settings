package hardkernel.odroid.settings.camera;

public class CameraReceiver {
    private static final String TAG = "CameraReceiver";

    public static void onReceive() {
        OV5647.setIrFilterStateToSysfs(OV5647.getIrFilterStateFromProperty());
    }
}
