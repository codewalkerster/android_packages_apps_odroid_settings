package hardkernel.odroid.settings.mouse;

import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;
import android.util.Log;
import android.widget.Toast;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import hardkernel.odroid.settings.EnvProperty;

public class MouseFragment extends LeanbackAddBackPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "MouseFragment";
    private static final String KEY_MOUSE_ACCELERATION_SWITCH = "mouse_accel_switch";
    private static final String KEY_MOUSE_RIGHT_BUTTON_LIST = "mouse_right_select";
    private static final String MOUSE_ACCELERATION = "persist.mouse_acceleration";
    private static final String MOUSE_RIGHT = "persist.mouse.right";
    private TwoStatePreference mouseAccelPref;
    private ListPreference mouseRightListPref;

    public static boolean mouseAccel = true;
    public static String mouseRight;

    public static MouseFragment newInstance() {
        return new MouseFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle saveInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.mouse, null);
        mouseAccelPref = (TwoStatePreference) findPreference(KEY_MOUSE_ACCELERATION_SWITCH);

        mouseAccel = EnvProperty.getBoolean(MOUSE_ACCELERATION, true);
        mouseAccelPref.setChecked(mouseAccel);

        mouseRightListPref = (ListPreference) findPreference(KEY_MOUSE_RIGHT_BUTTON_LIST);

        mouseRight = EnvProperty.get(MOUSE_RIGHT, "secondary");
        mouseRightListPref.setValue(mouseRight);

        mouseRightListPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null)
            return super.onPreferenceTreeClick(preference);

        switch (key) {
            case KEY_MOUSE_ACCELERATION_SWITCH:
                mouseAccel = mouseAccelPref.isChecked();
                EnvProperty.set(MOUSE_ACCELERATION, String.valueOf(mouseAccel));
                Toast.makeText(getContext(),
                        "Mouse Acceleration will be "
                        + (mouseAccel ? "enabled":"disabled")
                        + " after reboot!",
                        Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object obj) {
        Log.i(TAG, "onPreferenceChange:" + obj);
        if ((ListPreference) preference == mouseRightListPref) {
            if (mouseRight != (String) obj) {
                EnvProperty.set(MOUSE_RIGHT, (String) obj);
                mouseRight = (String) obj;
            }
        }

        return true;
    }
}
