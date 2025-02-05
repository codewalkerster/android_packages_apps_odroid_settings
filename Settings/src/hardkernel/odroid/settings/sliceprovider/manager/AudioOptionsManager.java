package hardkernel.odroid.settings.sliceprovider.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.hdmi.HdmiControlManager;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;

import com.droidlogic.app.AudioEffectManager;
import com.droidlogic.app.DroidAudioManager;


import static hardkernel.odroid.settings.util.DroidUtils.logDebug;

public class AudioOptionsManager {
    private static final String TAG = AudioOptionsManager.class.getSimpleName();

    private static final String FORCE_DDP_SWITCH = "db_id_sound_force_ddp_switch";

    private static final int ENABLED = 1;
    private static final int DISABLED = 0;

    private Context mContext;
    private static volatile AudioOptionsManager mAudioOptionsManager;
    private AudioManager mAudioManager;
    private DroidAudioManager mDroidAudioManager;

    public static boolean isInit() {
        return mAudioOptionsManager != null;
    }

    public static AudioOptionsManager getAudioOptionsManager(final Context context) {
        if (mAudioOptionsManager == null) {
            synchronized (AudioOptionsManager.class) {
                if (mAudioOptionsManager == null) {
                    mAudioOptionsManager = new AudioOptionsManager(context);
                }
            }
        }
        return mAudioOptionsManager;
    }

    public static void shutdown(final Context context) {
        if (mAudioOptionsManager != null) {
            synchronized (AudioOptionsManager.class) {
                if (mAudioOptionsManager != null) {
                    mAudioOptionsManager = null;
                }
            }
        }
    }

    private AudioOptionsManager(final Context context) {
        mContext = context;
        mAudioManager = context.getSystemService(AudioManager.class);
        mDroidAudioManager = DroidAudioManager.getInstance(context);
    }

    public boolean isAudioSupportMs12System() {
        boolean isSupportMs12 = mDroidAudioManager.isAudioSupportMs12System();
        Log.d(TAG, "get prepareArgs existingName: " + isSupportMs12);
        return isSupportMs12;
    }

    public boolean isDolbyMATEnabled() {
        return !mDroidAudioManager.getForceDDPEnable();
    }

    public void setDolbyMATEnabled(boolean enabled) {
        mDroidAudioManager.setForceDDPEnable(!enabled);
    }
}
