package hardkernel.odroid.settings.gpu;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Governor {
    private final static String CUR_GOVERNOR = "/sys/class/devfreq/fde60000.gpu/governor";
    private final static String AVAILABLE_GOVERNORS = "/sys/class/devfreq/fde60000.gpu/available_governors";

    private static String TAG;

    public Governor(String tag) {
        TAG = tag;
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
