package com.droidlogic.tv.settings.sliceprovider;

import androidx.slice.Slice;
import androidx.slice.SliceProvider;
import android.net.Uri;
import android.content.ContentResolver;
import android.provider.Settings;

import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder.RowBuilder;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.sliceprovider.manager.HdmiCecContentManager;
import com.droidlogic.tv.settings.sliceprovider.utils.MediaSliceUtil;
import com.droidlogic.tv.settings.sliceprovider.broadcastreceiver.HdmiCecSliceBroadcastReceiver;
import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;

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

    private static final String DROIDLOGIC_CEC_SUPPORT = "droidlogic_cec_support";
    private boolean mHasAutoPatch;

    @Override
    public boolean onCreateSliceProvider() {
        mHasAutoPatch = Settings.Global.getInt(getContext().getContentResolver(),
                DROIDLOGIC_CEC_SUPPORT, 0) == 1;
        return true;
    }

    @Override
    public Slice onBindSlice(final Uri sliceUri) {
        logDebug(TAG, false, "onBindSlice: " + sliceUri);
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
        logDebug(TAG, true, "createHdmiCecSlice volumePassthroughMode:" + volumePassthroughMode);
        if (mHasAutoPatch && PASSTHROUGH_MODE_ADD_WARNING == volumePassthroughMode) {
            boolean volumeControl = mHdmiCecContentManager.getVolumeControlStatus();
            String title = volumeControl ? getContext().getString(R.string.enabled)
                    : getContext().getString(R.string.disabled);
            psb.addPreference(
                    new RowBuilder()
                            .setKey(getContext().getString(R.string.hdmi_volume_control_key))
                            .setTitle(getContext().getString(R.string.hdmi_volume_control_title))
                            .setSubtitle(title)
                            .setInfoSummary(getContext().getString(R.string.cec_volume_control_description))
                            .addSwitch(
                                    generatePendingIntent(
                                            getContext(),
                                            MediaSliceConstants.ACTION_HDMI_VOLUME_CONTROL_CHANGED,
                                            HdmiCecSliceBroadcastReceiver.class),
                                    mHdmiCecContentManager.getVolumeControlStatus()));
        }
        if (SettingsConstant.isSoundbarFeature()) {
            psb.addPreference(
                    new RowBuilder()
                            .setKey(getContext().getString(R.string.hdmi_soundbar_mode_key))
                            .setTitle(getContext().getString(R.string.hdmi_soundbar_mode_title))
                            .setInfoSummary(getContext().getString(R.string.hdmi_soundbar_mode_description))
                            .setSubtitle(mHdmiCecContentManager.getSoundbarModeStatus() ?
                                    getContext().getString(R.string.enabled) : getContext().getString(R.string.disabled))
                            .addSwitch(
                                    generatePendingIntent(
                                            getContext(),
                                            MediaSliceConstants.ACTION_HDMI_SOUNDBAR_MODE_CONTROL_CHANGED,
                                            HdmiCecSliceBroadcastReceiver.class),
                                    mHdmiCecContentManager.getSoundbarModeStatus()));
        }
        if (!mHdmiCecContentManager.isTvDevice() && mHasAutoPatch) {
            psb.addPreference(
                    new RowBuilder()
                            .setKey(getContext().getString(R.string.hdmi_set_menu_language_key))
                            .setTitle(getContext().getString(R.string.hdmi_set_menu_language_title))
                            .setInfoSummary(getContext().getString(R.string.hdmi_set_menu_language_description))
                            .setSubtitle(mHdmiCecContentManager.isSetMenuLanguageEnabled() ?
                                    getContext().getString(R.string.enabled) : getContext().getString(R.string.disabled))
                            .addSwitch(
                                    generatePendingIntent(
                                            getContext(),
                                            MediaSliceConstants.ACTION_SET_MENU_LANGUAGE_CHANGED,
                                            HdmiCecSliceBroadcastReceiver.class),
                                    mHdmiCecContentManager.isSetMenuLanguageEnabled()));
        }
        return psb.build();
    }
}
