package hardkernel.odroid.settings.npu;

import android.util.Log;

import hardkernel.odroid.settings.OdroidSettingsApplication;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.util.OdroidUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Governor {
    private String CUR_GOVERNOR;
    private String AVAILABLE_GOVERNORS;

    private static String TAG;

    public Governor(String tag) {
        TAG = tag;
        init_sys_node();
    }

    public String[] getGovernors() {
        String available_governors = getAvailable();
        return available_governors.split(" ");
    }

    public String getCurrent() {
        String governor = null;
        try {
            FileReader fileReader = new FileReader(CUR_GOVERNOR);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            governor = bufferedReader.readLine();
            bufferedReader.close();
            Log.d(TAG, governor);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return governor;
    }

    public void set(String governor) {
        try {
            FileWriter fileWriter = new FileWriter(CUR_GOVERNOR);
            BufferedWriter out = new BufferedWriter(fileWriter);

            out.write(governor);
            out.newLine();
            out.close();

            Log.d(TAG, "set governor : " + governor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void init_sys_node() {
        String sys_node;
        if (OdroidUtils.isOdroidM2()) {
            sys_node = OdroidSettingsApplication
                .getRes().getString(R.string.m2_npu);
        } else {
            sys_node = "/sys/class/devfreq/fdab0000.npu";
        }
        CUR_GOVERNOR = sys_node + "/governor";
        AVAILABLE_GOVERNORS = sys_node + "/available_governors";
    }

    private String getAvailable() {
        String available_governors = null;
        try {
            FileReader fileReader = new FileReader(AVAILABLE_GOVERNORS);
            BufferedReader bufferedReader= new BufferedReader(fileReader);

            available_governors = bufferedReader.readLine();
            bufferedReader.close();

            Log.d(TAG, available_governors);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return available_governors;
    }
}
