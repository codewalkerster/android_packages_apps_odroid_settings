package com.droidlogic.tv.settings.sliceprovider;

import android.net.Uri;
import android.util.Log;
import androidx.slice.Slice;
import androidx.slice.SliceProvider;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder.RowBuilder;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.manager.HdmiCecContentManager;
import com.droidlogic.tv.settings.sliceprovider.utils.MediaSliceUtil;
import com.droidlogic.tv.settings.sliceprovider.broadcastreceiver.HdmiCecSliceBroadcastReceiver;

import android.content.ContentResolver;
import android.provider.Settings;


public class HdmiCecContentSliceProvider extends MediaSliceProvider {
    private static final String TAG = HdmiCecContentSliceProvider.class.getSimpleName();
    private static final boolean DEBUG = true;
    private HdmiCecContentManager mHdmiCecContentManager;

    // android original solution
    private static final int PASSTHROUGH_MODE_ORIGINAL = 0;
    // Abort local adjusting and show warning when it's audio passthrough decoding
    private static final int PASSTHROUGH_MODE_ADD_WARNING = 1;
    // Only send cec volume keys when it's audio passthrough decoding
    private static final int PASSTHROUGH_MODE_ACCORD_WITH_DECODING = 2;

    @Override
    public boolean onCreateSliceProvider() {
        return true;
    }

    @Override
    public Slice onBindSlice(final Uri sliceUri) {
        if (MediaSliceUtil.CanDebug()) {
            Log.d(TAG, "onBindSlice: " + sliceUri);
        }
        switch (MediaSliceUtil.getFirstSegment(sliceUri)) {
            case MediaSliceConstants.HDMI_CEC_PATH:
                // fill in Netfilx Esn into general info purposely
                return createHdmiCecSlice(sliceUri);
            default:
                return null;
        }
    }

    @Override
    public void shutdown() {
        HdmiCecContentManager.shutdown(getContext());
        mHdmiCecContentManager = null;
        super.shutdown();
    }

    private Slice createHdmiCecSlice(Uri sliceUri) {
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(getContext(), sliceUri);

        if (!HdmiCecContentManager.isInit()) {
            mHdmiCecContentManager = HdmiCecContentManager.getHdmiCecContentManager(getContext());
        }

        psb.addPreference(
                new RowBuilder()
                        .setKey(getContext().getString(R.string.hdmi_cec_switch_key))
                        .setTitle(getContext().getString(R.string.hdmi_cec_switch_title))
                        .setSubtitle(mHdmiCecContentManager.isHdmiControlEnabledName())
                        .setInfoSummary(getContext().getString(R.string.settings_cec_explain))
                        .addSwitch(
                                generatePendingIntent(
                                        getContext(),
                                        MediaSliceConstants.ACTION_HDMI_SWITCH_CEC_CHANGED,
                                        HdmiCecSliceBroadcastReceiver.class),
                                mHdmiCecContentManager.isHdmiControlEnabled()));

        int volumePassthroughMode = getContext().getResources().
                                        getInteger(R.integer.config_cec_passthroughMode);
        Log.d(TAG, "createHdmiCecSlice volumePassthroughMode:" + volumePassthroughMode);
        if (PASSTHROUGH_MODE_ADD_WARNING == volumePassthroughMode) {
            boolean volumeControl = mHdmiCecContentManager.getVolumeControlStatus();
            String title = volumeControl ? getContext().getString(R.string.enabled)
                    : getContext().getString(R.string.disabled);
            psb.addPreference(
                    new RowBuilder()
                            .setKey(getContext().getString(R.string.hdmi_volume_control_key))
                            .setTitle(getContext().getString(R.string.hdmi_volume_control_title))
                            .setSubtitle(title)
                            .addSwitch(
                                    generatePendingIntent(
                                            getContext(),
                                            MediaSliceConstants.ACTION_HDMI_VOLUME_CONTROL_CHANGED,
                                            HdmiCecSliceBroadcastReceiver.class),
                                    mHdmiCecContentManager.getVolumeControlStatus()));
        }

        return psb.build();
    }
}
