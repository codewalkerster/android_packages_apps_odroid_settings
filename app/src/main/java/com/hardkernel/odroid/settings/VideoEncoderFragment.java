package com.hardkernel.odroid.settings;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;
import com.hardkernel.odroid.settings.EnvProperty;
import com.hardkernel.odroid.settings.R;
import com.hardkernel.odroid.settings.RadioPreference;
import com.hardkernel.odroid.settings.dtbo.Overlay;
import com.hardkernel.odroid.settings.util.DroidUtils;

public class VideoEncoderFragment extends LeanbackAddBackPreferenceFragment {
    final String TAG= "VideoEncoderFragment";

    private String[] encoderNames;
    private String[] encoderValues;
    private static String currentEncoder;
    private static final String ENCODER_RADIO_GROUP = "encoder_group";
    private PreferenceCategory categoryPref;
    private static long totalSize = 0;
    private static final long MEM_2G = 2147483648L;

    public static VideoEncoderFragment newInstance() {
        return new VideoEncoderFragment();
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        encoderNames = getArrayString(R.array.media_encoder_names);
        encoderValues = getArrayString(R.array.media_encoder_values);
        currentEncoder = EnvProperty.getFromFile("media.settings.xml");

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstance, String rootKey) {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);
        screen.setTitle(R.string.encoder_title);
        categoryPref = new PreferenceCategory(themedContext);
        categoryPref.setTitle(R.string.default_encoder);
        screen.addPreference(categoryPref);

        for (int i=0; i < encoderNames.length; i++) {
            if (DroidUtils.isOdroidN2()
                    && encoderNames[i].contains("H265")
                    && (getTotalMemSize() < MEM_2G))
                continue;
            final String value = encoderValues[i];
            final RadioPreference radio = new RadioPreference(themedContext);
            radio.setKey(value);
            radio.setPersistent(false);
            radio.setTitle(encoderNames[i]);
            radio.setRadioGroup(ENCODER_RADIO_GROUP);
            radio.setLayoutResource(R.layout.preference_reversed_widget);

            if (value.equals(currentEncoder)) {
                radio.setChecked(true);
            }

            categoryPref.addPreference(radio);
        }
        setPreferenceScreen(screen);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            final RadioPreference radio = (RadioPreference)preference;
            radio.clearOtherRadioPreferences(categoryPref);
            if (radio.isChecked()) {
                String key = radio.getKey().toString();
                if (!key.equals(currentEncoder)) {
                    String title = radio.getTitle().toString();
                    Toast.makeText(getContext(),
                            "Changed Video Encoder to "
                            + title
                            + ", and it will be applied after reboot!",
                            Toast.LENGTH_LONG).show();
                    currentEncoder = key;
                    EnvProperty.save("media.settings.xml", key,
                            "Change video encoder to " + radio.getTitle());
                    if (DroidUtils.isOdroidN2()
                            && title.contains("H265")) {
                        String dtbo = Overlay.get();
                        if (!dtbo.contains("codec_mm_cma")) {
                            dtbo += " codec_mm_cma";
                            Overlay.set(dtbo);
                        }
                    }
                }
            } else {
                radio.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private long getTotalMemSize() {
        if (totalSize != 0)
            return totalSize;

        ActivityManager am = (ActivityManager)getContext()
            .getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memInfo);
        totalSize = memInfo.totalMem;

        return totalSize;
    }
}
