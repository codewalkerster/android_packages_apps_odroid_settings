package com.droidlogic.tv.settings.sliceprovider.broadcastreceiver;

import static com.android.tv.twopanelsettings.slices.SlicesConstants.EXTRA_PREFERENCE_KEY;
import static android.app.slice.Slice.EXTRA_TOGGLE_STATE;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.provider.Settings;
import android.view.WindowManager;


import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.MediaSliceConstants;
import com.droidlogic.tv.settings.sliceprovider.utils.MediaSliceUtil;
import com.droidlogic.tv.settings.sliceprovider.manager.HdmiCecContentManager;


public class HdmiCecSliceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = HdmiCecSliceBroadcastReceiver.class.getSimpleName();
    private ProgressDialog mProgress;
    private static final int MSG_ENABLE_CEC_SWITCH = 0;
    private static final int TIME_DELAYED = 5000;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENABLE_CEC_SWITCH:
                    if (MediaSliceUtil.CanDebug()) Log.d(TAG, "enable cec switch");
                    if (mProgress != null && mProgress.isShowing()) {
                        mProgress.dismiss();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "onReceive " + intent);
        boolean isChecked;
        mProgress = new ProgressDialog(context);
        mProgress.setMessage(context.getString(R.string.cec_status_update));
        mProgress.setIndeterminate(false);
        mProgress.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        switch (action) {
            case MediaSliceConstants.ACTION_HDMI_SWITCH_CEC_CHANGED:
                isChecked = intent.getBooleanExtra(EXTRA_TOGGLE_STATE, true);
                getHdmiCecContentManager(context).setHdmiCecEnabled(isChecked);
                if (mProgress != null && !mProgress.isShowing()) {
                    mProgress.show();
                }
                mHandler.sendEmptyMessageDelayed(MSG_ENABLE_CEC_SWITCH, TIME_DELAYED);
                break;
            case MediaSliceConstants.ACTION_HDMI_VOLUME_CONTROL_CHANGED:
                isChecked = intent.getBooleanExtra(EXTRA_TOGGLE_STATE, true);
                getHdmiCecContentManager(context).setVolumeControlStatus(isChecked ? 1 : 0);
                break;
            default:
                break;
        }
    }

    private HdmiCecContentManager getHdmiCecContentManager(Context context) {
        return HdmiCecContentManager.getHdmiCecContentManager(context);
    }

}
