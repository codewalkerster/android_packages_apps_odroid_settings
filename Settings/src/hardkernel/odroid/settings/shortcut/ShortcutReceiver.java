package hardkernel.odroid.settings.shortcut;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShortcutReceiver extends BroadcastReceiver {
    private final String TAG = "ShortcutReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        ShortcutManager.initShortcuts(context);
    }
}
