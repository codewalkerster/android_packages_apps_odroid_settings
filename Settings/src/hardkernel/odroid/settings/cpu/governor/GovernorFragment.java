package hardkernel.odroid.settings.cpu.governor;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import hardkernel.odroid.settings.R;

import hardkernel.odroid.settings.RadioPreference;
import hardkernel.odroid.settings.ConfigEnv;
import hardkernel.odroid.settings.cpu.CPU;
import hardkernel.odroid.settings.LeanbackAddBackPreferenceFragment;

public class GovernorFragment extends LeanbackAddBackPreferenceFragment {
    private static final String TAG = "GovernorFragment";
    public static CPU cpu = null;

    public static GovernorFragment newInstance() { return new GovernorFragment(); }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (cpu == null)
            cpu = CPU.getCPU(TAG, CPU.Cluster.Big);
        updatePreferenceFragment();
    }

    private void updatePreferenceFragment() {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);

        switch(cpu.cluster) {
            case Big:
                screen.setTitle(R.string.big_core_governor);
                break;
            case Middle:
                screen.setTitle(R.string.middle_core_governor);
                break;
            case Little:
                screen.setTitle(R.string.little_core_governor);
                break;
            default:
                screen.setTitle(R.string.cpu);
        }

        setPreferenceScreen(screen);

        String[] governorList = cpu.governor.getGovernors();

        for (final String governor : governorList) {
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(governor);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(governor);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            if (cpu.governor.getCurrent().equals(governor)) {
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
                cpu.governor.set(selectedGovernor);
                saveGovernor(selectedGovernor);
                radioPreference.setChecked(true);
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void saveGovernor(String governor) {
        switch (cpu.cluster) {
            case Big:
                ConfigEnv.setBigCpuGovernor(governor);
                break;
            case Middle:
                ConfigEnv.setMiddleCpuGovernor(governor);
                break;
            case Little:
                ConfigEnv.setLittleCpuGovernor(governor);
                break;
            default:
                ConfigEnv.setLittleCpuGovernor(governor);
                break;
        }
    }
}
