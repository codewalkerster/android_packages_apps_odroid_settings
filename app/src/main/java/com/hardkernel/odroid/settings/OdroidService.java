package com.hardkernel.odroid.settings;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RecoverySystem;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.hardkernel.odroid.settings.update.DownloadReceiver;
import com.hardkernel.odroid.settings.update.installReceiver;

import java.io.File;

public class OdroidService extends Service {

    final static String Channel_id = "UpdateNotification";
    final public static int Notification_id = 0x201920;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String cmd = intent.getStringExtra("cmd");

        switch (cmd) {
            case "installPackage":
                installPackage(intent);
                break;
            case "updatePackageFromOnline":
                updatePackageFromOnline();
                break;
        }
        return super.onStartCommand(intent,flags,startId);
    }

    private void updatePackageFromOnline() {
        Intent downloadIntent = new Intent(this, DownloadReceiver.class);
        downloadIntent.setAction("DOWNLOAD_PACKAGE");

        createNotificationChannel();

        String message = "Do you want to download new update package?\n"
                + "It would take a few minutes or hours depends on your network speed.\n";
        buildNNotify("New update package is found!", message,
                "Download", downloadIntent);
    }

    private void installPackage(Intent intent) {
        File packageFile = (File)intent.getExtras().getSerializable("packageFile");

        Bundle packageBundle = new Bundle();
        try {
            RecoverySystem.verifyPackage(packageFile, null, null);
        } catch (Exception e) {
            Toast.makeText(this,
                    "The package file seems to be corrupted!!\n" +
                            "Please select another package file ...",
                    Toast.LENGTH_LONG).show();
        }

        packageBundle.putSerializable("packageFile", packageFile);
        Intent installIntent = new Intent(this, installReceiver.class);
        installIntent.setAction("android.intent.action.PACKAGE_INSTALL");
        installIntent.putExtra(installReceiver.INSTALL_NOTIFICATION_ID,
                installReceiver.INSTALL_NOTIFICATION_VALUE);
        installIntent.putExtra("packageFile", packageBundle);

        createNotificationChannel();

        buildNNotify("Odroid-Setting Update", "Start Update",
                "Update", installIntent);
    }

    private void buildNNotify(String contentTitle, String contentMsg,
                              String actionTitle, Intent intent) {

        PendingIntent actionIntent = PendingIntent.getBroadcast(
                this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Channel_id)
                .setSmallIcon(R.drawable.ic_system_update)
                .setContentTitle(contentTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentMsg))
                .setContentText(contentMsg)
                .addAction(R.drawable.ic_system_update, actionTitle, actionIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(Notification_id, builder.build());
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(Channel_id,
                Channel_id, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Update Notification channel.");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
