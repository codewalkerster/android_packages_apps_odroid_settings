package hardkernel.odroid.settings.sliceprovider.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.sliceprovider.MediaSliceConstants;
import hardkernel.odroid.settings.sliceprovider.manager.AudioOptionsManager;

import static android.app.slice.Slice.EXTRA_TOGGLE_STATE;
import static com.android.tv.twopanelsettings.slices.SlicesConstants.EXTRA_PREFERENCE_KEY;
import static hardkernel.odroid.settings.util.DroidUtils.logDebug;

public class AudioOptionsSliceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = AudioOptionsSliceBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        String userSetKey = intent.getStringExtra(EXTRA_PREFERENCE_KEY);
        logDebug(TAG, true, "onReceive " + intent + ", userSetKey: " + userSetKey);
        switch (action) {
            case MediaSliceConstants.AUDIO_ADVANCED_DOLBY_MAT:
                boolean isMat =
                        context.getString(R.string.audio_options_dolby_mat_key).equals(userSetKey);
                getAudioOptionsManager(context).setDolbyMATEnabled(isMat);

            default:
                break;
        }
        context.getContentResolver().notifyChange(MediaSliceConstants.DOLBY_PREFERRED_OUTPUT_URI, null);
    }

    private AudioOptionsManager getAudioOptionsManager(Context context) {
        return AudioOptionsManager.getAudioOptionsManager(context);
    }

}
