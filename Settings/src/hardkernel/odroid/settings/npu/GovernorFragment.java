package hardkernel.odroid.settings.npu;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import hardkernel.odroid.settings.R;

import hardkernel.odroid.settings.RadioPreference;
import hardkernel.odroid.settings.ConfigEnv;
import hardkernel.odroid.settings.npu.NPU;
import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;

public class GovernorFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "GovernorFragment";
    public static NPU npu = null;

    public static GovernorFragment newInstance() { return new GovernorFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (npu == null)
            npu = NPU.getNPU(TAG);
        updatePreferenceFragment();
    }

    private void updatePreferenceFragment() {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);

        screen.setTitle(R.string.npu_governor);
        setPreferenceScreen(screen);

        String[] governorList = npu.governor.getGovernors();

        for (final String governor : governorList) {
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(governor);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(governor);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            if (npu.governor.getCurrent().equals(governor)) {
                radioPreference.setChecked(true);
            }
            screen.addPreference(radioPreference);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference)preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                String selectedGovernor = radioPreference.getKey();
                npu.governor.set(selectedGovernor);
                saveGovernor(selectedGovernor);
                radioPreference.setChecked(true);
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void saveGovernor(String governor) {
            ConfigEnv.setNpuGovernor(governor);
    }
}
