package hardkernel.odroid.settings.sliceprovider.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;

import hardkernel.odroid.settings.sliceprovider.utils.MediaSliceUtil;

public class BaseDialogActivity extends Activity {
    private static String STATE_DIALOG_CREATED_KEY = "state_dialog_created";
    private static String TAG = "BaseDialogActivity";
    protected AlertDialog mAlertDialog;

    /* access modifiers changed from: protected */
    public boolean isDialogCreated(Bundle bundle) {
        if (bundle == null || !bundle.getBoolean(STATE_DIALOG_CREATED_KEY, false)) {
            return false;
        }
        Log.i(TAG, "Dialog has been created. No further action is needed.");
        return true;
    }

    /* access modifiers changed from: protected */
    public void saveCurrentState(Bundle bundle) {
        Log.i(TAG, "Save current dialog state");
        AlertDialog alertDialog = this.mAlertDialog;
        if (alertDialog == null || !alertDialog.isShowing()) {
            bundle.putBoolean(STATE_DIALOG_CREATED_KEY, false);
        } else {
            bundle.putBoolean(STATE_DIALOG_CREATED_KEY, true);
        }
    }
}
