package hardkernel.odroid.settings.display.rotation;

import android.content.Context;

public class RotationReceiver {
    private final static String TAG = "RotationReceiver";

    public static void onReceive (Context context) {
        Rotation.setOrientation(Rotation.getOrientation(), context);
    }
}
