package com.droidlogic.tv.settings.sliceprovider;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.app.tvsettings.TvSettingsEnums;
import androidx.slice.Slice;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder.RowBuilder;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.broadcastreceiver.HdrSliceBroadcastReceiver;
import com.droidlogic.tv.settings.sliceprovider.dialog.*;
import com.droidlogic.tv.settings.sliceprovider.manager.DisplayCapabilityManager;
import com.droidlogic.tv.settings.sliceprovider.manager.DisplayCapabilityManager.HdrFormat;
import com.droidlogic.tv.settings.sliceprovider.manager.DisplayCapabilityManager.HdrFormatConfig;
import com.droidlogic.tv.settings.sliceprovider.utils.MediaSliceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HdrSliceProvider extends MediaSliceProvider {
    private static final String TAG = HdrSliceProvider.class.getSimpleName();
    private static final String DYNAMIC_RANGE_PLACEHOLDER = "DYNAMIC_RANGE_PLACEHOLDER";
    private static final String ACTION_HDMI_PLUGGED = "android.intent.action.HDMI_PLUGGED";

    private static final boolean DEBUG = true;
    private static final String KEY_RESET = "DISPLAY_RESET";

    private Handler mHandler = new Handler();
    private DisplayCapabilityManager mDisplayCapabilityManager;
    private IntentFilter mIntentFilter;

    @Override
    public boolean onCreateSliceProvider() {
        mIntentFilter = new IntentFilter(ACTION_HDMI_PLUGGED);
        mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        getContext().registerReceiver(new HdrSliceBroadcastReceiver(), mIntentFilter);
        if (MediaSliceUtil.CanDebug()) Log.d(TAG, "onCreateSliceProvider");
        return true;
    }

    @Override
    public void shutdown() {
        DisplayCapabilityManager.shutdown();
        mDisplayCapabilityManager = null;
        super.shutdown();
    }

    @Override
    public Slice onBindSlice(final Uri sliceUri) {
        Log.d(TAG, "onBindSlice: " + sliceUri);
        switch (MediaSliceUtil.getFirstSegment(sliceUri)) {
            case MediaSliceConstants.MATCH_CONTENT_PATH:
                return createHdrMatchContentSlice(sliceUri);
            case MediaSliceConstants.RESOLUTION_PATH:
                return createHdrResolutionSlice(sliceUri);
            case MediaSliceConstants.HDR_AND_COLOR_FORMAT_PATH:
                return createHdrAndColorFormatSlice(sliceUri);
            case MediaSliceConstants.HDR_FORMAT_PREFERENCE_PATH:
                return createHdrFormatPreferenceSlice(sliceUri);
            case MediaSliceConstants.COLOR_ATTRIBUTE_PATH:
                return createColorAttributeSlice(sliceUri);
            case MediaSliceConstants.DOLBY_VISION_MODE_PATH:
                return createDolbyVisionModeSlice(sliceUri);
            default:
                return null;
        }
    }

    private Slice createHdrMatchContentSlice(final Uri sliceUri) {
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(getContext(), sliceUri);
        psb.addScreenTitle(
                new RowBuilder().setTitle(getContext().getString(R.string.hdr_match_content_title)));

        if (!initDisplayCapabilityManager(sliceUri)) {
            return psb.build();
        } else {
            tryRefreshDisplayCapabilityManager(sliceUri);
        }

        // SDR should not show content on this page
        if (mDisplayCapabilityManager.getPreferredFormat() == HdrFormat.SDR) {
            return psb.build();
        }

        psb.addPreference(
                new RowBuilder()
                        .setKey(getContext().getString(R.string.hdr_match_content_sink_key))
                        .setTitle(getMatchSinkTitle())
                        .setInfoSummary(
                                getContext()
                                        .getString(R.string.hdr_match_content_sink_help_text)
                                        .replace(
                                                DYNAMIC_RANGE_PLACEHOLDER,
                                                hdrFormatToTitle(mDisplayCapabilityManager.getPreferredFormat())))
                        .addRadioButton(
                                generatePendingIntent(
                                        getContext(),
                                        MediaSliceConstants.ACTION_MATCH_CONTENT_POLICY_CHANGED,
                                        MatchContentChangeDialogActivity.class),
                                !mDisplayCapabilityManager.isHdrPolicySource(),
                                getContext().getString(R.string.hdr_match_content_type_radio_group_name)));

        psb.addPreference(
                new RowBuilder()
                        .setKey(getContext().getString(R.string.hdr_match_content_source_key))
                        .setTitle(getContext().getString(R.string.hdr_match_content_source_title))
                        .setInfoSummary(getContext().getString(R.string.hdr_match_content_help_text))
                        .addRadioButton(
                                generatePendingIntent(
                                        getContext(),
                                        MediaSliceConstants.ACTION_MATCH_CONTENT_POLICY_CHANGED,
                                        MatchContentChangeDialogActivity.class),
                                mDisplayCapabilityManager.isHdrPolicySource(),
                                getContext().getString(R.string.hdr_match_content_type_radio_group_name)));

        return psb.build();
    }

    public String getMatchSinkTitle() {
        if (HdrFormat.HDR == mDisplayCapabilityManager.getPreferredFormat()) {
            return getContext().getString(R.string.hdr_format_always_hdr_title);
        } else if (HdrFormat.DOLBY_VISION == mDisplayCapabilityManager.getPreferredFormat()) {
            return getContext().getString(R.string.hdr_format_always_dolby_vision_title);
        }
        return null;
    }

    private boolean initDisplayCapabilityManager(final Uri callbackUri) {
        if (mDisplayCapabilityManager != null) {
            return true;
        }

        if (!DisplayCapabilityManager.isInit()) {
            final Context context = getContext();
            // Handler is ues to avoid executing IO operation in UI thread.
            mHandler.post(
                    () -> {
                        DisplayCapabilityManager.getDisplayCapabilityManager(context);
                        context.getContentResolver().notifyChange(callbackUri, null);
                    });
            return false;
        } else {
            mDisplayCapabilityManager =
                    DisplayCapabilityManager.getDisplayCapabilityManager(getContext());
            return true;
        }
    }

    private Slice createHdrResolutionSlice(final Uri sliceUri) {
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(getContext(), sliceUri);

        if (!initDisplayCapabilityManager(sliceUri)) {
            return psb.build();
        } else {
            tryRefreshDisplayCapabilityManager(sliceUri);
        }

        // If it is the system preferred display mode,
        // This slice is implemented by TvSetting using Preference on AndroidT.
        if (mDisplayCapabilityManager.getSystemPreferredDisplayMode()) {
            Log.d(TAG, "TvSettings show!!");
            return null;
        }

        psb.addScreenTitle(
                new RowBuilder().setTitle(getContext().getString(R.string.hdr_resolution_title)));

        psb.setEmbeddedPreference(
                new RowBuilder()
                        .setTitle(getContext().getString(R.string.hdr_resolution_title))
                        .setSubtitle(
                                mDisplayCapabilityManager.getTitleByMode(mDisplayCapabilityManager.getCurrentMode())));

        if (!mDisplayCapabilityManager.isCvbsMode()) {
            createAutoBestHdrResolution(psb);
            if (!mDisplayCapabilityManager.isBestResolution()) {
                updateHdrResolutionDetails(psb);
            }
        } else {
            updateHdrResolutionDetails(psb);
        }

        return psb.build();
    }

    private void tryRefreshDisplayCapabilityManager(final Uri callbackUri) {
        if (DisplayCapabilityManager.isInit()) {
            final Context context = getContext();
            mHandler.post(
                    () -> {
                        if (mDisplayCapabilityManager.refresh()) {
                            context.getContentResolver().notifyChange(callbackUri, null);
                        }
                    });
        }
    }

    private void createAutoBestHdrResolution(PreferenceSliceBuilder psb) {
        psb.addPreference(
                new RowBuilder()
                        .setTitle(getContext().getString(R.string.hdr_auto_best_resolution_title))
                        .addSwitch(
                                generatePendingIntent(
                                        getContext(),
                                        MediaSliceConstants.ACTION_AUTO_BEST_RESOLUTIONS_ENABLED,
                                        HdrSliceBroadcastReceiver.class),
                                mDisplayCapabilityManager.isBestResolution()));
    }

    private void updateHdrResolutionDetails(PreferenceSliceBuilder psb) {
        String[] hdmiModes = mDisplayCapabilityManager.getHdmiModes();
        String currentMode = mDisplayCapabilityManager.getCurrentMode();
        if (hdmiModes == null) {
            Log.e(TAG, "hdmiModes is null !!");
            return;
        }

        Log.d(TAG, "hdmiModes:" + Arrays.toString(hdmiModes));
        Log.d(TAG, "currentMode:" + currentMode);

        for (int i = 0; i < hdmiModes.length; i++) {
            String[] titlesByMode = mDisplayCapabilityManager.getTitlesByMode(hdmiModes[i]);
            // modeSubtitle defaults to using an uncommon variable.
            String modeSubtitle = getContext().getString(R.string.hdr_resolution_cvbs_subtitle);
            if (!mDisplayCapabilityManager.isCvbsMode()) {
                modeSubtitle = titlesByMode[1];
            }

            psb.addPreference(
                    new RowBuilder()
                            .setKey(hdmiModes[i])
                            .setTitle(titlesByMode[0])
                            .setSubtitle(modeSubtitle)
                            .addRadioButton(
                                    generatePendingIntent(
                                            getContext(),
                                            MediaSliceConstants.SHOW_RESOLUTION_CHNAGE_WARNING,
                                            AdjustResolutionDialogActivity.class),
                                    currentMode.equals(hdmiModes[i]),
                                    getContext().getString(R.string.hdr_resolution_radio_group_name)));
        }
    }

    private Slice createHdrAndColorFormatSlice(Uri sliceUri) {
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(getContext(), sliceUri);
        if (!initDisplayCapabilityManager(sliceUri)) {
            return psb.build();
        } else {
            tryRefreshDisplayCapabilityManager(sliceUri);
        }

        if (mDisplayCapabilityManager.isCvbsMode()) {
            Log.d(TAG, "this is cvbsmode ~~");
            return null;
        }


        psb.addScreenTitle(
                new RowBuilder()
                        .setTitle(getContext().getString(R.string.dynamic_range_and_color_format_title)));

        // The sliceprovider hides the HDR priority and HDR policy when the
        // framework has access to the supported HDR output types.
        if (mDisplayCapabilityManager.getSupportedHdrOutputTypes() == 0) {
            psb.setEmbeddedPreference(
                    new RowBuilder()
                            .setTitle(getContext().getString(R.string.dynamic_range_and_color_format_title))
                            .setSubtitle(hdrFormatToTitle(mDisplayCapabilityManager.getPreferredFormat())));

            psb.addPreference(
                    new RowBuilder()
                            .setTitle(getContext().getString(R.string.dynamic_range_format_preference_title))
                            .setSubtitle(hdrFormatToTitle(mDisplayCapabilityManager.getPreferredFormat()))
                            .setTargetSliceUri(
                                    MediaSliceUtil.generateTargetSliceUri(
                                            MediaSliceConstants.HDR_FORMAT_PREFERENCE_PATH)));

            if (HdrFormat.SDR != mDisplayCapabilityManager.getPreferredFormat()) {
                psb.addPreference(
                        new RowBuilder()
                                .setTitle(getContext().getString(R.string.hdr_match_content_title))
                                .setSubtitle(
                                        mDisplayCapabilityManager.isHdrPolicySource()
                                                ? getContext().getString(R.string.hdr_match_content_source_title)
                                                : getMatchSinkTitle())
                                .setTargetSliceUri(
                                        MediaSliceUtil.generateTargetSliceUri(MediaSliceConstants.MATCH_CONTENT_PATH)));
            }
        }


        if (HdrFormat.DOLBY_VISION == mDisplayCapabilityManager.getPreferredFormat()) {
            String subtitle =
                    mDisplayCapabilityManager.isDolbyVisionModeLLPreferred()
                            ? getContext().getString(R.string.dolby_vision_mode_low_latency_title)
                            : getContext().getString(R.string.dolby_vision_mode_standard_title);
            Log.d(TAG, "DOLBY_VISION subtitle:" + subtitle);

            psb.setEmbeddedPreference(
                    new RowBuilder()
                            .setTitle(getContext().getString(R.string.dynamic_range_and_color_format_title))
                            .setSubtitle(subtitle));

            psb.addPreference(
                    new RowBuilder()
                            .setTitle(getContext().getString(R.string.dolby_vision_mode_title))
                            .setSubtitle(subtitle)
                            .setTargetSliceUri(
                                    MediaSliceUtil.generateTargetSliceUri(
                                            MediaSliceConstants.DOLBY_VISION_MODE_PATH)));
        }

        if (HdrFormat.DOLBY_VISION != mDisplayCapabilityManager.getPreferredFormat()) {
            List<String> colorAttrs = mDisplayCapabilityManager.getColorAttributes();
            String currentColorAttr = mDisplayCapabilityManager.getCurrentColorAttribute();
            currentColorAttr = currentColorAttr.trim();

            Log.d(TAG, "createHdrAndColorFormatSlice; colorAttrsList: " + colorAttrs);
            Log.d(TAG, "createHdrAndColorFormatSlice; currentColorAttr: " + currentColorAttr);

            psb.setEmbeddedPreference(
                    new RowBuilder()
                            .setTitle(getContext().getString(R.string.dynamic_range_and_color_format_title))
                            .setSubtitle(mDisplayCapabilityManager.getTitleByColorAttr(currentColorAttr)));

            psb.addPreference(
                    new RowBuilder()
                            .setTitle(getContext().getString(R.string.color_format_title))
                            .setSubtitle(
                                    mDisplayCapabilityManager.getTitleByColorAttr(currentColorAttr))
                            .setTargetSliceUri(
                                    MediaSliceUtil.generateTargetSliceUri(MediaSliceConstants.COLOR_ATTRIBUTE_PATH)));
        }
        updateDisplayResetButton(psb);
        return psb.build();
    }

    private Slice createHdrFormatPreferenceSlice(final Uri sliceUri) {
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(getContext(), sliceUri);

        psb.addScreenTitle(
                new RowBuilder()
                        .setTitle(getContext().getString(R.string.dynamic_range_format_preference_title)));

        if (!initDisplayCapabilityManager(sliceUri)) {
            return psb.build();
        } else {
            tryRefreshDisplayCapabilityManager(sliceUri);
        }

        HdrFormatConfig hdrFormatConfig = mDisplayCapabilityManager.getHdrFormatConfig();
        HdrFormat preferredFormat = mDisplayCapabilityManager.getPreferredFormat();
        if (MediaSliceUtil.CanDebug()) Log.d(TAG, "createHdrFormatPreferenceSlice preferredFormat:" + preferredFormat);

        if (hdrFormatConfig.getSupportedFormats().size() > 0) {
            psb.addPreferenceCategory(
                    new RowBuilder()
                            .setTitle(getContext().getString(R.string.hdr_supported_format_category_title)));
            for (HdrFormat format : hdrFormatConfig.getSupportedFormats()) {
                psb.addPreference(
                        new RowBuilder()
                                .setKey(format.getKey())
                                .setTitle(hdrFormatToTitle(format))
                                .setInfoSummary(hdrFormatToHelpText(format))
                                .addRadioButton(
                                        generatePendingIntent(
                                                getContext(),
                                                MediaSliceConstants.ACTION_SET_HDR_FORMAT,
                                                PreferredModeChangeDialogActivity.class),
                                        preferredFormat.equals(format),
                                        getContext().getString(R.string.hdr_format_select_type_radio_group_name)));
            }
        }

        if (hdrFormatConfig.getUnsupportedFormats().size() > 0) {
            psb.addPreferenceCategory(
                    new RowBuilder()
                            .setTitle(getContext().getString(R.string.hdr_unsupported_format_category_title)));
            for (HdrFormat format : hdrFormatConfig.getUnsupportedFormats()) {
                psb.addPreference(
                        new RowBuilder()
                                // use different key to force the preference being refreshed
                                .setKey(format.getKey() + "__unsupported")
                                .setTitle(hdrFormatToTitle(format))
                                .setEnabled(false));
            }
        }

        return psb.build();
    }

    private Slice createColorAttributeSlice(final Uri sliceUri) {
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(getContext(), sliceUri);

        psb.addScreenTitle(
                new RowBuilder().setTitle(getContext().getString(R.string.color_format_title)));

        if (!initDisplayCapabilityManager(sliceUri)) {
            return psb.build();
        } else {
            tryRefreshDisplayCapabilityManager(sliceUri);
        }

        // Dolby Vision should not show content on this page
        if (mDisplayCapabilityManager.getPreferredFormat() == HdrFormat.DOLBY_VISION) {
            return psb.build();
        }

        List<String> colorAttrs = mDisplayCapabilityManager.getColorAttributes();
        String currentColorAttr = mDisplayCapabilityManager.getCurrentColorAttribute();
        currentColorAttr = currentColorAttr.trim();

        Log.d(TAG, "createColorAttributeSlice; colorAttrsList: " + colorAttrs);
        Log.d(TAG, "createColorAttributeSlice; currentColorAttr: " + currentColorAttr);

        String currentMode = mDisplayCapabilityManager.getCurrentMode();
        for (String colorAttr : colorAttrs) {
            if (MediaSliceUtil.CanDebug()) {
                Log.d(TAG, "currentMode:" + currentMode + " colorAttr:" + colorAttr);
            }
            psb.addPreference(
                    new RowBuilder()
                            .setKey(colorAttr)
                            .setTitle(mDisplayCapabilityManager.getTitleByColorAttr(colorAttr))
                            .setInfoSummary(getContext().getString(R.string.color_format_info_summary))
                            .addRadioButton(
                                    generatePendingIntent(
                                            getContext(),
                                            MediaSliceConstants.ACTION_SET_COLOR_ATTRIBUTE,
                                            AdjustColorFormatDialogActivity.class),
                                    currentColorAttr.equals(colorAttr),
                                    getContext().getString(R.string.color_attribute_select_type_radio_group_name)));
        }
        return psb.build();
    }

    private Slice createDolbyVisionModeSlice(Uri sliceUri) {
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(getContext(), sliceUri);

        psb.addScreenTitle(
                new RowBuilder().setTitle(getContext().getString(R.string.dolby_vision_mode_title)));

        if (!initDisplayCapabilityManager(sliceUri)) {
            return psb.build();
        } else {
            tryRefreshDisplayCapabilityManager(sliceUri);
        }

        // Only at mode Dolby Vision should show content on this page
        if (!HdrFormat.DOLBY_VISION.equals(mDisplayCapabilityManager.getPreferredFormat())) {
            return psb.build();
        }
        boolean isDolbyVisionModeLL = mDisplayCapabilityManager.isDolbyVisionModeLLPreferred();
        Log.d(TAG, "isDolbyVisionModeLL: " + isDolbyVisionModeLL
                + " doesDolbyVisionSupportLL: " + mDisplayCapabilityManager.doesDolbyVisionSupportLL()
                + " doesDolbyVisionSupportStandard: " + mDisplayCapabilityManager.doesDolbyVisionSupportStandard());

        if (mDisplayCapabilityManager.doesDolbyVisionSupportLL()) {
            psb.addPreference(
                    new RowBuilder()
                            .setKey(getContext().getString(R.string.dolby_vision_mode_low_latency_key))
                            .setTitle(getContext().getString(R.string.dolby_vision_mode_low_latency_title))
                            .addRadioButton(
                                    generatePendingIntent(
                                            getContext(),
                                            MediaSliceConstants.ACTION_SET_DOLBY_VISION_MODE,
                                            DolbyVisionModeChangeActivity.class),
                                    isDolbyVisionModeLL,
                                    getContext().getString(R.string.dolby_vision_mode_select_type_radio_group_name)));
        }

        if (mDisplayCapabilityManager.doesDolbyVisionSupportStandard()) {
            psb.addPreference(
                    new RowBuilder()
                            .setKey(getContext().getString(R.string.dolby_vision_mode_standard_key))
                            .setTitle(getContext().getString(R.string.dolby_vision_mode_standard_title))
                            .addRadioButton(
                                    generatePendingIntent(
                                            getContext(),
                                            MediaSliceConstants.ACTION_SET_DOLBY_VISION_MODE,
                                            DolbyVisionModeChangeActivity.class),
                                    !isDolbyVisionModeLL,
                                    getContext().getString(R.string.dolby_vision_mode_select_type_radio_group_name)));
        }

        return psb.build();
    }

    private void updateDisplayResetButton(PreferenceSliceBuilder psb) {
        psb.addPreference(
                new RowBuilder()
                        .setKey(KEY_RESET)
                        .setTitle(getContext().getString(R.string.device_display_reset_Title))
                        .setInfoSummary(getContext().getString(R.string.device_display_reset_description))
                        .setActionId(TvSettingsEnums.DISPLAY_SOUND_ADVANCED_DISPLAY_GAME_MODE)
                        .setPendingIntent(
                                generatePendingIntent(
                                        getContext(),
                                        MediaSliceConstants.ACTION_DISPLAY_RESET,
                                        DisplayResetActivity.class)
                        )
        );
    }

    private String hdrFormatToTitle(HdrFormat hdrFormat) {
        switch (hdrFormat) {
            case SDR:
                return getContext().getString(R.string.hdr_format_sdr_title);
            case HDR:
                return getContext().getString(R.string.hdr_format_hdr_title);
            case DOLBY_VISION:
                return getContext().getString(R.string.hdr_format_dolby_vision_title);
            default:
                return null;
        }
    }

    private String hdrFormatToHelpText(HdrFormat hdrFormat) {
        switch (hdrFormat) {
            case SDR:
                return getContext().getString(R.string.hdr_format_sdr_help_text);
            case HDR:
                return getContext().getString(R.string.hdr_format_hdr_help_text);
            case DOLBY_VISION:
                return getContext().getString(R.string.hdr_format_dolby_vision_help_text);
            default:
                return null;
        }
    }
}
