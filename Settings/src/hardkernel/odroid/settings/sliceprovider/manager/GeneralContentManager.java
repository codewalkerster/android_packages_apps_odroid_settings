package hardkernel.odroid.settings.sliceprovider.manager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.util.Log;
import hardkernel.odroid.settings.sliceprovider.broadcastreceiver.NetflixEsnResponseBroadcastReceiver;
import hardkernel.odroid.settings.sliceprovider.utils.MediaSliceUtil;


public class GeneralContentManager {
    private static final String TAG = GeneralContentManager.class.getSimpleName();
    private static final String ACTION_NETFLIX_ESN_RESPONSE =
            "com.netflix.ninja.intent.action.ESN_RESPONSE";
    private static final String ACTION_NETFLIX_ESN = "com.netflix.ninja.intent.action.ESN";
    private static final String PACKAGE_NETFLIX = "com.netflix.ninja";
    private static final String PERMISSION_NETFLIX_ESN = "com.netflix.ninja.permission.ESN";
    private static volatile GeneralContentManager mGeneralContentManager;
    private Context mContext;
    private String mNetflixEsn;
    private NetflixEsnResponseBroadcastReceiver mEsnReceiver =
            new NetflixEsnResponseBroadcastReceiver();

    public static boolean isInit() {
        return mGeneralContentManager != null;
    }

    public static GeneralContentManager getGeneralContentManager(final Context context) {
        if (mGeneralContentManager == null) {
            synchronized (GeneralContentManager.class) {
                if (mGeneralContentManager == null) {
                    mGeneralContentManager = new GeneralContentManager(context);
                }
            }
        }
        return mGeneralContentManager;
    }

    public static void shutdown(final Context context) {
        if (mGeneralContentManager != null) {
            synchronized (GeneralContentManager.class) {
                if (mGeneralContentManager != null) {
                    mGeneralContentManager.unregisterReceiver();
                    mGeneralContentManager = null;
                }
            }
        }
    }

    private GeneralContentManager(final Context context) {
        mContext = context;
        registerReceiver();
        refresh();
    }

    private void registerReceiver() {
        IntentFilter esnIntentFilter = new IntentFilter(ACTION_NETFLIX_ESN_RESPONSE);
        mContext.registerReceiver(mEsnReceiver, esnIntentFilter, PERMISSION_NETFLIX_ESN, null, Context.RECEIVER_EXPORTED);
    }

    private void unregisterReceiver() {
        try {
            mContext.unregisterReceiver(mEsnReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Netflix ESN receiver has been unregistered");
        }
    }

    public boolean refresh() {
        sendNetflixEsnQuery();
        return false;
    }

    private void sendNetflixEsnQuery() {
        Intent esnQueryIntent = new Intent(ACTION_NETFLIX_ESN);
        esnQueryIntent.setPackage(PACKAGE_NETFLIX);
        esnQueryIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        mContext.sendBroadcastAsUser(esnQueryIntent, UserHandle.getUserHandleForUid(0));
    }

    public void setNetflixEsn(String esn) {
        mNetflixEsn = esn;
    }

    public String getNetflixEsn() {
        return mNetflixEsn;
    }
}
