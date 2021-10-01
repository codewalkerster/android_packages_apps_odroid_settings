package hardkernel.odroid.settings.gpu;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.widget.Toast;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;

import hardkernel.odroid.settings.RadioPreference;
import hardkernel.odroid.settings.ConfigEnv;
import hardkernel.odroid.settings.gpu.GPU;

public class FrequencyFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "FrequencyFragment";
    public static GPU gpu = null;

    public static FrequencyFragment newInstance() { return new FrequencyFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (gpu == null) {
            gpu = GPU.getGPU(TAG);
        }
        updatePreferenceFragment();
    }

    private void updatePreferenceFragment() {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);

        screen.setTitle(R.string.gpu_clock);
        setPreferenceScreen(screen);

        String[] frequencyList = gpu.frequency.getFrequencies();

        for (final String frequency : frequencyList) {
            if (Integer.parseInt(frequency) < gpu.frequency.getPolicyMin())
                continue;

            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(frequency);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(frequency);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            if (gpu.frequency.getMax().equals(frequency)) {
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
                String selectedFrequency = radioPreference.getKey();
                if (gpu.frequency.getPolicyMax()
                        < Integer.valueOf(selectedFrequency)) {
                    Toast.makeText(getContext(),
                            "Selected Clock will be applied after reboot",
                            Toast.LENGTH_LONG).show();
                }
                gpu.frequency.setMax(selectedFrequency);
                saveFrequency(selectedFrequency);
                radioPreference.setChecked(true);
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void saveFrequency(String frequency) {
            ConfigEnv.setGpuFreq(frequency);
    }
}
