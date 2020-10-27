package com.hardkernel.odroid.settings.update;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.hardkernel.odroid.settings.OdroidService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

public class DownloadReceiver extends BroadcastReceiver {

    public static long enqueue;
    private static DownloadManager downloadManager = null;

    private static UpdatePackage updatePackage = null;

    final String TAG = "UpdateDownloadReceiver";

    public static DownloadManager getDownloadManager(Context context) {
        if (downloadManager == null)
            downloadManager = (DownloadManager)context.getSystemService(context.DOWNLOAD_SERVICE);
        return downloadManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("DOWNLOAD_PACKAGE")) {
            downloadPackage(context);
            return;
        }

        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
        if ( id != enqueue) {
            Log.v(TAG, "Ignoring unrelated download " + id);
            return;
        }

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor = getDownloadManager(context).query(query);

        if (!cursor.moveToFirst()) {
            Log.e(TAG, "Not able to move the cursor for downloaded content.");
            return;
        }
        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        if ( DownloadManager.ERROR_INSUFFICIENT_SPACE == status) {
            Log.e(TAG, "Download is failed due to insufficient space");
            return;
        }
        if (DownloadManager.STATUS_SUCCESSFUL != status) {
            Log.e(TAG, "Download Failed");
            return;
        }

        /* Get URI of download file */
        Uri uri = Uri.parse(cursor.getString(
                cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));

        cursor.close();;

        File file = new File(uri.getPath());
        if (!file.exists()) {
            Log.e(TAG, "Not able to find downloaded file: " + uri.getPath());
            return;
        }

        String latest_version = updateManager.getLatestVersion();

        if (file.getName().equals(latest_version)) {
            try {
                StringBuilder text = new StringBuilder();

                BufferedReader br = new BufferedReader((new FileReader((file))));
                text.append(br.readLine());
                br.close();

                updatePackage = new UpdatePackage(text.toString());

                int currentVersion = Integer.parseInt(Build.VERSION.INCREMENTAL);

                if (currentVersion < updatePackage.name.getVersion()) {
                    updatePackageFromOnline(context);
                } else if (currentVersion > updatePackage.name.getVersion()) {
                    Toast.makeText(context,
                            "The current installed build number might be wrong",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context,
                            "The latest Android image is already installed.",
                            Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }
        } else if (id == updatePackage.downloadedPackageId()) {
            UpdatePackage.installPackage(context,
                    new File(updatePackage.localUri().getPath()));
        }
    }

    private void updatePackageFromOnline(final Context context) {
        Intent intent = new Intent(
                context,
                OdroidService.class);
        intent.putExtra("cmd", "updatePackageFromOnline");
        context.startService(intent);
    }

    private void downloadPackage(Context context) {
        if (sufficientSpace(context)) {
            updatePackage.requestDownload(context);
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(OdroidService.Notification_id);
    }

    private boolean sufficientSpace(Context context) {
        StatFs stat = new StatFs(UpdatePackage.DOWNLOAD_DIR);
        long available = stat.getAvailableBytes();

        if (available < updateManager.PACKAGE_MAXSIZE) {
            Toast.makeText(context,
                    "Check free space\n" +
                    "Insufficient free space\n" +
                    updateManager.PACKAGE_MAXSIZE /1024 /1024 +
                    "MBytes free space is required.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
