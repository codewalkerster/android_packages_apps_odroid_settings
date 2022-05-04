package hardkernel.odroid.settings.camera;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import hardkernel.odroid.settings.EnvProperty;

public class OV5647 {
    private static final String TAG = "OV5647";
    private static final String irFilterStatePath = "/sys/kernel/debug/regulator/ov5647_irfilter/enable";
    private static final String irFilterStateProp = "persist.ov5647.irfilter";
    private static File irFilterState = null;

    private static final String irFilterPath = "/sys/kernel/debug/regulator/ov5647_irfilter";

    public static boolean isIrFilterAvailable() {
        File irPath = new File(irFilterPath);
        return irPath.exists();
    }

    public static boolean getIrFilterStateFromProperty() {
        String filterState = EnvProperty.get(irFilterStateProp, "0");

        return filterState.equals("1");
    }

    public static boolean getIrFilterStateFromSysfs() {
        if (irFilterState == null)
            irFilterState = new File(irFilterStatePath);

        String state = "";
        try {
            FileInputStream fin = new FileInputStream(irFilterState);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            state = reader.readLine();
            fin.close();
        } catch (IOException e) {
            Log.i(TAG, "ReadFrom File Exception: " + e);
            e.printStackTrace();
        }

        return state.equals("1");
    }

    public static void setIrFilterStateToSysfs(Boolean state) {
        if (irFilterState == null)
            irFilterState = new File(irFilterStatePath);

        try {
            FileWriter fileWriter = new FileWriter(irFilterState);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.write(state? "1": "0");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setIrFilterStateToProperty(Boolean state) {
        EnvProperty.set(irFilterStateProp, state ? "1" : "0");
    }
}
