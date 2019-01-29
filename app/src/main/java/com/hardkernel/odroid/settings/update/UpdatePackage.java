package com.hardkernel.odroid.settings.update;

import java.io.File;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.RecoverySystem;
import android.util.Log;
import android.widget.Toast;

import com.hardkernel.odroid.settings.update.DownloadReceiver;

class UpdatePackage {
    private static final String TAG = "UpdatePackage";

    private static long packgeId = -1;
    private static long version_checkId = -1; /* TODO: use it when check version */

    packageName name;

    UpdatePackage(String name) {
        this.name = new packageName(name);
    }

    UpdatePackage(int buildNumber) {
        name = new packageName(buildNumber);
    }

    static File getDownloadDir(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    }

    public static Uri localUri(Context context) {
        return Uri.parse("file://" + getDownloadDir(context) + "/update.zip");
    }

    public static long downloadedPackageId() { return packgeId; }
    public static long version_checkId() { return version_checkId; }

    /*
     * Check Update package version to update latest version.
     */
    public static void checkLatestVersion(Context context, DownloadManager downloadManager) {
        String remote = updateManager.getRemoteURL();

        /* Remove if the same file is exist */
        new File (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                updateManager.LATEST_VERSION).delete();
        try {
            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(remote + updateManager.LATEST_VERSION));
            request.setVisibleInDownloadsUi(false);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setDestinationInExternalFilesDir(context,
                    Environment.DIRECTORY_DOWNLOADS,
                    updateManager.LATEST_VERSION);

            DownloadReceiver.enqueue = downloadManager.enqueue(request);
        } catch (IllegalArgumentException e) {
            Toast.makeText(context,
                    "URL must be HTTPS/HTTPS forms.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Request to download update package if necessary
     */
    public void requestDownload(Context context, DownloadManager downloadManager) {
        String name = this.name.getName();
        if (name == null)
            return;

        Uri uri = Uri.parse(updateManager.getRemoteURL() + name);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("Downloading new update package");
        request.setDescription(uri.getPath());
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(localUri(context));

        Log.d(TAG, "Requesting to download " + uri.getPath() + " to " + localUri(context));

        /* Remove if the same file is exist */
        File file = new File(localUri(context).getPath());
        if (file.exists())
            file.delete();

        DownloadReceiver.enqueue = packgeId = downloadManager.enqueue(request);
    }

    public static void installPackage (Context context, final File packageFile) {
        Log.e(TAG, "installPackage = " + packageFile.getPath());
        try {
            RecoverySystem.verifyPackage(packageFile, null, null);

            new AlertDialog.Builder(context)
                    .setTitle("Selected package file is verified")
                    .setMessage("Your Android can be updated, do you want to proceed?")
                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                RecoverySystem.installPackage(context,
                                        packageFile);
                            } catch (Exception e) {
                                Toast.makeText(context,
                                        "Error while install OTA package: " + e,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setCancelable(true)
                    .create().show();
        } catch (Exception e) {
            Toast.makeText(context,
                    "The package file seems to be corrupted!!\n" +
                            "Please select another package file...",
                    Toast.LENGTH_LONG).show();
        }
    }
}