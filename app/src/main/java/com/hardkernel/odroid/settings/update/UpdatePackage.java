package com.hardkernel.odroid.settings.update;

import java.io.File;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.hardkernel.odroid.settings.OdroidService;

public class UpdatePackage {
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
    public static void checkLatestVersion(Context context) {
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

            DownloadReceiver.enqueue = DownloadReceiver
                    .getDownloadManager(context).enqueue(request);
        } catch (IllegalArgumentException e) {
            Toast.makeText(context,
                    "URL must be HTTPS/HTTPS forms.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Request to download update package if necessary
     */
    public void requestDownload(Context context) {
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

        DownloadReceiver.enqueue = packgeId = DownloadReceiver
                .getDownloadManager(context).enqueue(request);
    }

    public static void installPackage (final Context context, final File packageFile) {
        Log.e(TAG, "installPackage = " + packageFile.getPath());

        Intent intent = new Intent(
                context,
                OdroidService.class);
        intent.putExtra("cmd", "installPackage");
        Bundle bundle = new Bundle();
        bundle.putSerializable("packageFile", packageFile);
        intent.putExtras(bundle);
        context.startService(intent);
    }
}
