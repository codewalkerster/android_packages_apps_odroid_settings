package hardkernel.odroid.settings.sliceprovider.dialog;

import static com.android.tv.twopanelsettings.slices.SlicesConstants.EXTRA_PREFERENCE_KEY;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.content.Context;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import hardkernel.odroid.settings.R;
import hardkernel.odroid.settings.sliceprovider.manager.DisplayCapabilityManager;
import hardkernel.odroid.settings.sliceprovider.manager.DisplayCapabilityManager.HdrFormat;
import hardkernel.odroid.settings.sliceprovider.utils.MediaSliceUtil;
import java.util.concurrent.TimeUnit;
import android.util.Log;

public class PreferredModeChangeDialogActivity extends Activity {
  private static String TAG = PreferredModeChangeDialogActivity.class.getSimpleName();
  private static final String COUNTDOWN_PLACEHOLDER = "COUNTDOWN_PLACEHOLDER";
  private static final String RESOLUTION_PLACEHOLDER = "RESOLUTION_PLACEHOLDER";
  private AlertDialog mAlertDialog;
  private Runnable mRestoreCallback = () -> {};
  private DisplayCapabilityManager mDisplayCapabilityManager;
  private static int DEFAULT_COUNTDOWN_SECONDS = 15;
  private CountDownTimer mCountDownTimer;
  private int countdownInSeconds = 0;
  private boolean mDisplayModeWasChanged;
  private boolean wasHdrPolicyChanged;
  HdrFormat targetPreferredFormat;
  HdrFormat currentPreferredFormat;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDisplayCapabilityManager =
        DisplayCapabilityManager.getDisplayCapabilityManager(getApplicationContext());

    currentPreferredFormat = mDisplayCapabilityManager.getPreferredFormat();
    targetPreferredFormat =
        HdrFormat.fromKey(getIntent().getStringExtra(EXTRA_PREFERENCE_KEY));
    if (currentPreferredFormat == targetPreferredFormat) finish();
    String currentMode = mDisplayCapabilityManager.getCurrentMode();
    Log.d(TAG, "onCreate currentPreferredFormat:" + currentPreferredFormat
            + " targetPreferredFormat:" + targetPreferredFormat + " currentMode:" + currentMode);
    mDisplayCapabilityManager.setPreferredFormat(targetPreferredFormat);

    mRestoreCallback =
        () -> {
          Log.d(TAG, "mRestoreCallback currentPreferredFormat:" + currentPreferredFormat);
          mDisplayCapabilityManager.setPreferredFormat(currentPreferredFormat);
          mDisplayCapabilityManager.notifyChangeSlice(getContentResolver());
        };
    initAlertDialog();
    showDialog();
  }

  private void initAlertDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogBackground);
    builder.setCancelable(false);
    builder.setPositiveButton(
        R.string.preferred_mode_change_dialog_ok_msg,
        (dialog, which) -> {
          if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
          }
          finish();
          // click ok
          //first clear user preferred mode
          mDisplayCapabilityManager.clearUserPreferredDisplayMode();
          mDisplayCapabilityManager.setPreferredFormat(targetPreferredFormat);
          mDisplayCapabilityManager.setHdrPriority(targetPreferredFormat);
        });
    builder.setNegativeButton(
        R.string.preferred_mode_change_dialog_cancel_msg,
        (dialog, which) -> {
          if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
          }
          // click cancel
          mRestoreCallback.run();
          finish();
        });

    builder.setTitle(getString(R.string.preferred_mode_change_dialog_title));
    builder.setMessage(
        mDisplayModeWasChanged
            ? getString(R.string.preferred_mode_change_and_display_mode_change_dialog_desc)
                .replace(
                    RESOLUTION_PLACEHOLDER,
                    mDisplayCapabilityManager.getTitleByMode(
                        mDisplayCapabilityManager.getCurrentMode()))
            : getString(R.string.preferred_mode_change_dialog_desc));

    mAlertDialog = builder.create();
    mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
  }

  private void showDialog() {
    mAlertDialog.show();
    mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus();
    countdownInSeconds = DEFAULT_COUNTDOWN_SECONDS;
    mCountDownTimer =
        new CountDownTimer(
            TimeUnit.SECONDS.toMillis(DEFAULT_COUNTDOWN_SECONDS), TimeUnit.SECONDS.toMillis(1L)) {

          final Button cancelButton = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

          @Override
          public void onTick(long millisUntilFinished) {
            cancelButton.setText(
                getString(R.string.preferred_mode_change_dialog_cancel_msg)
                    .replace(COUNTDOWN_PLACEHOLDER, String.valueOf(countdownInSeconds)));
            if (countdownInSeconds != 0) {
              countdownInSeconds--;
            }
          }

          @Override
          public void onFinish() {
            mAlertDialog.dismiss();
            mRestoreCallback.run();
            finish();
            Log.d(TAG,"showDialog timeout");
          }
        };
    mCountDownTimer.start();
  }

}
