package hardkernel.odroid.settings.gpu;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import hardkernel.odroid.settings.R;

import hardkernel.odroid.settings.RadioPreference;
//import hardkernel.odroid.settings.ConfigEnv;
import hardkernel.odroid.settings.gpu.GPU;
import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;

public class GovernorFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "GovernorFragment";
    public static GPU gpu = null;

    public static GovernorFragment newInstance() { return new GovernorFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (gpu == null)
            gpu = GPU.getGPU(TAG);
        updatePreferenceFragment();
    }

    private void updatePreferenceFragment() {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);

        screen.setTitle(R.string.gpu_governor);
        setPreferenceScreen(screen);

        String[] governorList = gpu.governor.getGovernors();

        for (final String governor : governorList) {
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(governor);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(governor);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            if (gpu.governor.getCurrent().equals(governor)) {
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
                gpu.governor.set(selectedGovernor);
                saveGovernor(selectedGovernor);
                radioPreference.setChecked(true);
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void saveGovernor(String governor) {
        /*
            ConfigEnv.setGpuGovernor(governor);
            */
    }
}
