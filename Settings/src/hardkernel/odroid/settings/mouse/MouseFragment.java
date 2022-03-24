package hardkernel.odroid.settings.mouse;

import android.os.Bundle;
import android.widget.Toast;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import hardkernel.odroid.settings.EnvProperty;

public class MouseFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "MouseFragment";
    private static final String KEY_MOUSE_ACCELERATION_SWITCH = "mouse_accel_switch";
    private static final String KEY_MOUSE_RIGHT_BUTTON_SWITCH = "mouse_right_select";
    private static final String MOUSE_ACCELERATION = "persist.mouse_acceleration";
    private static final String MOUSE_RIGHT = "persist.mouse.right";
    private TwoStatePreference mouseAccelPref;
    private TwoStatePreference mouseRightPref;

    private static boolean mouseAccel = true;
    private static String mouseRight;

    public static MouseFragment newInstance() {
        return new MouseFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
    }

    @Override
    public void onCreatePreferences(Bundle saveInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.mouse, null);
        mouseAccelPref = (TwoStatePreference) findPreference(KEY_MOUSE_ACCELERATION_SWITCH);
        mouseRightPref = (TwoStatePreference) findPreference(KEY_MOUSE_RIGHT_BUTTON_SWITCH);

        refreshStatus();
    }

    private void refreshStatus() {
        mouseAccel = EnvProperty.getBoolean(MOUSE_ACCELERATION, true);
        mouseAccelPref.setChecked(mouseAccel);
        mouseRight = EnvProperty.get(MOUSE_RIGHT, "secondary");
        mouseRightPref.setChecked(mouseRight.equals("back"));
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
            case KEY_MOUSE_RIGHT_BUTTON_SWITCH:
                EnvProperty.set(MOUSE_RIGHT, mouseRightPref.isChecked()?
                        "back":
                        "secondary");
        }
        return super.onPreferenceTreeClick(preference);
    }
}
