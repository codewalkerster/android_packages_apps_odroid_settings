package hardkernel.odroid.settings.bluetooth;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.SettingsPreferenceFragment;
import hardkernel.odroid.settings.EnvProperty;

import com.android.internal.logging.nano.MetricsProto;

@Keep
public class BluetoothFragment extends SettingsPreferenceFragment {
    private static final String TAG = "BluetoothFragment";
    private static final String KEY_BLUETOOTH_SERVICE = "bluetooth_service";
    static final String PERSIST_BLUETOOTH_SERVICE_DOWN = "persist.bt.service.down";

    private TwoStatePreference mPreferenceBluetoothService;
    private boolean mIsBtServiceDown;

    public static BluetoothFragment newInstance() {
        return new BluetoothFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.bluetooth, null);
        mIsBtServiceDown = EnvProperty.getBoolean(PERSIST_BLUETOOTH_SERVICE_DOWN, true);

        mPreferenceBluetoothService = (TwoStatePreference) findPreference(KEY_BLUETOOTH_SERVICE);
        mPreferenceBluetoothService.setChecked(!mIsBtServiceDown);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mPreferenceBluetoothService) {
            mIsBtServiceDown = !mPreferenceBluetoothService.isChecked();
            EnvProperty.set(PERSIST_BLUETOOTH_SERVICE_DOWN, mIsBtServiceDown);

            Toast.makeText(getContext(),
                    R.string.bluetooth_service_state,
                    Toast.LENGTH_LONG).show();
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SOUND;
    }

}
