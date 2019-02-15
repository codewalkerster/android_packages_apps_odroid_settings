package com.hardkernel.odroid.settings.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DownloadReceiver extends BroadcastReceiver {

    public static long enqueue;
    public static DownloadManager downloadManager = null;

    private static UpdatePackage updatePackage = null;
    public static Context context = null;

    final String TAG = "UpdateDownloadReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
        if ( id != enqueue) {
            Log.v(TAG, "Ignoring unrelated download " + id);
            return;
        }

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor = downloadManager.query(query);

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

        if (file.getName().equals(updateManager.LATEST_VERSION)) {
            try {
                StringBuilder text = new StringBuilder();

                BufferedReader br = new BufferedReader((new FileReader((file))));
                text.append(br.readLine());
                br.close();

                updatePackage = new UpdatePackage(text.toString());

                int currentVersion = 0;
                String[] version = Build.VERSION.INCREMENTAL.split("-");
                if (version.length < 4) {
                    Toast.makeText(context,
                            "Not able to detect the version number installed. "
                                    + "Remote package will be installed anyway!",
                            Toast.LENGTH_LONG).show();
                } else {
                    currentVersion = Integer.parseInt(version[3]);
                }

                if (currentVersion < updatePackage.name.getVersion()) {
                    updatePackageFromOnline();
                } else if (currentVersion > updatePackage.name.getVersion()) {
                    Toast.makeText(context,
                            "The current installed build number might be wrong",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context,
                            "Already latest Andorid image is installed.",
                            Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }
        } else if (id == updatePackage.downloadedPackageId()) {
            UpdatePackage.installPackage(context,
                    new File(updatePackage.localUri(context).getPath()));
        }
    }

    private void updatePackageFromOnline() {
        new AlertDialog.Builder(context)
                .setTitle("New update package is found!")
                .setMessage("Do you want to download new update package?\n"
                        + "It would take a few minutes or hours depends on your network speed.")
                .setPositiveButton("Download",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (sufficientSpace()) {
                                    updatePackage.requestDownload(context, downloadManager);
                                }
                            }
                        })
                .setCancelable(true)
                .create().show();
    }

    private boolean sufficientSpace() {
        StatFs stat = new StatFs(UpdatePackage.getDownloadDir(context).getPath());

        double available = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();

        if (available < updateManager.PACKAGE_MAXSIZE) {
            new AlertDialog.Builder(context)
                    .setTitle("Check free space")
                    .setMessage("Insufficient free space!\nAbout " +
                    updateManager.PACKAGE_MAXSIZE / 1024 / 1024 +
                    "MBytes free space is required.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return false;
        }

        return true;
    }
}
