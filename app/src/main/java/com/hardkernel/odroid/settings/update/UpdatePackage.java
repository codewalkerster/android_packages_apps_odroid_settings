package com.hardkernel.odroid.settings.update;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.hardkernel.odroid.settings.OdroidService;

public class UpdatePackage {
    private static final String TAG = "UpdatePackage";

    private static long packgeId = -1;
    private static long version_checkId = -1; /* TODO: use it when check version */

    public static final String DOWNLOAD_DIR = "/storage/emulated/0/Download";
    packageName name;

    UpdatePackage(String name) {
        this.name = new packageName(name);
    }

    UpdatePackage(int buildNumber) {
        name = new packageName(buildNumber);
    }

    public static Uri localUri() {
        return Uri.parse("file://" + DOWNLOAD_DIR + "/update.zip");
    }

    public static File copyToCache(File src) {
        File dest = new File("/cache/update.zip");
        if (dest.exists()) {
            dest.delete();
        }
        try {
            Path result = Files.move(src.toPath(), dest.toPath());
            if (result.toString().equals("/cache/update.zip")) {
                Log.d(TAG, "Move update file to /cache partition");
            } else {
                Log.d(TAG, "Move failed");
            }
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            e.printStackTrace();
        }

        return dest;
    }

    public static long downloadedPackageId() { return packgeId; }
    public static long version_checkId() { return version_checkId; }

    /*
     * Check Update package version to update latest version.
     */
    public static void checkLatestVersion(Context context) {
        String remote = updateManager.getRemoteURL();

        if (remote.indexOf("https://") != 0) {
            StringBuffer str = new StringBuffer(remote);
            remote = "";
            remote = str.insert(0, "https://").toString();
        }

        /* Remove if the same file is exist */
        String latest_version = updateManager.getLatestVersion();

        new File (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                latest_version).delete();
        try {
            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(remote + latest_version));
            request.setVisibleInDownloadsUi(false);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setDestinationInExternalFilesDir(context,
                    Environment.DIRECTORY_DOWNLOADS,
                    latest_version);

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
        request.setDestinationUri(localUri());

        Log.d(TAG, "Requesting to download " + uri.getPath() + " to " + localUri());

        /* Remove if the same file is exist */
        File file = new File(localUri().getPath());
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
        bundle.putSerializable("packageFile", copyToCache(packageFile));
        intent.putExtras(bundle);
        context.startService(intent);
    }
}
