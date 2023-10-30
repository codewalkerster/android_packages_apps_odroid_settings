package hardkernel.odroid.settings.dtbo;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.RadioPreference;
import java.util.ArrayList;

public class DTBOSelectFragment extends LeanbackAddBackPreferenceFragment {

    private static final String TAG = "DTBOSelectFragment";

    public static DTBOSelectFragment newInstance() {
        return new DTBOSelectFragment();
    }

    private static ArrayList<String> availableList;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        availableList = Overlay.getAvailableList();
        context = getPreferenceManager().getContext();
        refreshStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void refreshStatus() {
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        String title = context.getString(R.string.overlays);

        screen.setTitle(title);
        setPreferenceScreen(screen);

        Overlay.sync();

        for(String overlay: availableList) {
            RadioPreference radio = createOverlayRadio(overlay);
            screen.addPreference(radio);
        }
    }

    private RadioPreference createOverlayRadio(String overlay) {
        RadioPreference radio = new RadioPreference(context);
        radio.setKey(overlay);
        radio.setTitle(overlay);
        radio.setLayoutResource(R.layout.preference_reversed_widget);
        radio.setPersistent(false);

        radio.setChecked(Overlay.getCurrent().contains(overlay));

        return radio;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        RadioPreference radio = (RadioPreference)preference;
        Overlay.set(radio.getKey(), radio.isChecked());
        return true;
    }
}
