package hardkernel.odroid.settings.sliceprovider;

import android.net.Uri;
import androidx.slice.Slice;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder.RowBuilder;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.sliceprovider.broadcastreceiver.AudioOptionsSliceBroadcastReceiver;
import hardkernel.odroid.settings.sliceprovider.dialog.AVSyncTuningActivity;
import hardkernel.odroid.settings.sliceprovider.dialog.AdjustResolutionDialogActivity;
import hardkernel.odroid.settings.sliceprovider.dialog.DolbyVisionModeChangeActivity;
import hardkernel.odroid.settings.sliceprovider.manager.AudioOptionsManager;
import hardkernel.odroid.settings.sliceprovider.manager.HdmiCecContentManager;
import hardkernel.odroid.settings.sliceprovider.utils.MediaSliceUtil;

import static hardkernel.odroid.settings.util.DroidUtils.logDebug;

public class AudioOptionsSliceProvider extends MediaSliceProvider {
    private static final String TAG = AudioOptionsSliceProvider.class.getSimpleName();
    private AudioOptionsManager mAudioOptionsManager;

    @Override
    public boolean onCreateSliceProvider() {
        return true;
    }

    @Override
    public Slice onBindSlice(final Uri sliceUri) {
        logDebug(TAG, true, "onBindSlice: " + sliceUri);
        switch (MediaSliceUtil.getFirstSegment(sliceUri)) {
            case MediaSliceConstants.AUDIO_EXT_PATH:
                return createAudioOptionsSlice(sliceUri);
            case MediaSliceConstants.DOLBY_PREFERRED_OUTPUT_PATH:
                return createDolbyPreferredOutputSlice(sliceUri);
            default:
                return null;
        }
    }

    @Override
    public void shutdown() {
        AudioOptionsManager.shutdown(getContext());
        mAudioOptionsManager = null;
        super.shutdown();
    }

    private Slice createAudioOptionsSlice(Uri sliceUri) {
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(getContext(), sliceUri);
        psb.addScreenTitle(
                new RowBuilder()
                        .setTitle(getContext().getString(R.string.audio_options_title)));
        psb.setEmbeddedPreference(
                new RowBuilder()
                        .setTitle(getContext().getString(R.string.audio_options_title)));

        if (!AudioOptionsManager.isInit()) {
            mAudioOptionsManager = AudioOptionsManager.getAudioOptionsManager(getContext());
        }

        if (mAudioOptionsManager.isAudioSupportMs12System()) {
            psb.addPreference(
                    new RowBuilder()
                            .setKey(getContext().getString(R.string.audio_options_dolby_preferred_output_key))
                            .setTitle(getContext().getString(R.string.audio_options_dolby_preferred_output_title))
                            .setSubtitle(mAudioOptionsManager.isDolbyMATEnabled() ?
                                    getContext().getString(R.string.audio_options_dolby_mat_title)
                                    : getContext().getString(R.string.audio_options_dolby_digital_plus_title))
                            .setTargetSliceUri(
                                    MediaSliceUtil.generateTargetSliceUri(MediaSliceConstants.DOLBY_PREFERRED_OUTPUT_PATH)));
        }

        psb.addPreference(
                new RowBuilder()
                        .setKey(getContext().getString(R.string.audio_options_av_sync_key))
                        .setTitle(getContext().getString(R.string.audio_options_av_sync_title))
                        .setPendingIntent(
                                generatePendingIntent(
                                        getContext(),
                                        MediaSliceConstants.SHOW_AUDIO_AV_SYNC_TUNING_WARNING,
                                        AVSyncTuningActivity.class)
                        )
        );
        return psb.build();
    }


    private Slice createDolbyPreferredOutputSlice(Uri sliceUri) {
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(getContext(), sliceUri);

        psb.addScreenTitle(
                new RowBuilder().setTitle(getContext().getString(R.string.audio_options_dolby_preferred_output_title)));

        if (!AudioOptionsManager.isInit()) {
            mAudioOptionsManager = AudioOptionsManager.getAudioOptionsManager(getContext());
        }

        psb.addPreference(
                new RowBuilder()
                        .setKey(getContext().getString(R.string.audio_options_dolby_mat_key))
                        .setTitle(getContext().getString(R.string.audio_options_dolby_mat_title))
                        .addRadioButton(
                                generatePendingIntent(
                                        getContext(),
                                        MediaSliceConstants.AUDIO_ADVANCED_DOLBY_MAT,
                                        AudioOptionsSliceBroadcastReceiver.class),
                                mAudioOptionsManager.isDolbyMATEnabled()));

        psb.addPreference(
                new RowBuilder()
                        .setKey(getContext().getString(R.string.audio_options_dolby_digital_plus_key))
                        .setTitle(getContext().getString(R.string.audio_options_dolby_digital_plus_title))
                        .addRadioButton(
                                generatePendingIntent(
                                        getContext(),
                                        MediaSliceConstants.AUDIO_ADVANCED_DOLBY_MAT,
                                        AudioOptionsSliceBroadcastReceiver.class),
                                !mAudioOptionsManager.isDolbyMATEnabled()));
        return psb.build();
    }
}
