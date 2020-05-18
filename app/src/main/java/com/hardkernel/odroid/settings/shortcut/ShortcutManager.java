package com.hardkernel.odroid.settings.shortcut;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.KeyEvent;
import android.view.WindowManager;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class ShortcutManager {
    /*
     * pkg[] - shorcut function key index
     * 0 - F7 ~ 3 - F10
     */
    private static String pkg[] = new String[4];

    private static SharedPreferences pref;
    private static final String PREFERENCE_NAME = "shortcut_preference";

    private static WindowManager wm;
    private static PackageManager pm;

    private static void init(Context context) {
        if (pm == null)
            pm = context.getPackageManager();
        if (wm == null)
            wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (pref == null)
            pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public static void onReceived(Context context) {
        initShortcuts(context);
    }

    private static void initShortcuts(Context context) {
        init(context);

        for (int i=0;i<4; i++)
            pkg[i] = pref.getString("shortcut_f" + (i+7), "No shortcut");

        ArrayList<String> appList = getAvailableAppList(context);
        for (int i = 0; i < 4; i++) {
            if (pkg[i].equals("home")) {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.setPackage("home");
                wm.setApplicationShortcut(KeyEvent.KEYCODE_F7 + i, home);
                continue;
            }
            for (String app : appList) {
                if (app.equals(pkg[i])) {
                    wm.setApplicationShortcut(KeyEvent.KEYCODE_F7 + i, pm.getLaunchIntentForPackage(app));
                }
            }
        }
    }

    public static String pkgAt(int index) {
        return pkg[index];
    }

    public static CharSequence pkgNameAt(int index) {
        ApplicationInfo app;

        try {
            if (pkg[index].equals("home"))
                return  "HOME";
            app = pm.getApplicationInfo(pkg[index], PackageManager.GET_META_DATA);
        } catch(Exception e) {
            pkg[index] = "No shortcut";
            return "No shortcut";
        }
        return app.loadLabel(pm);
    }

    private static List<ApplicationInfo> appList = null;

    public static ArrayList<String> getAvailableAppList(Context context) {
        init(context);
        appList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<String> launchApps = new ArrayList<>();
        for (ApplicationInfo appInfo: appList) {
            Intent launchApp = pm.getLaunchIntentForPackage(appInfo.packageName);
            if (launchApp != null)
                launchApps.add(appInfo.packageName);
        }

        launchApps.add("home");

        return launchApps;
    }

    public static List<ApplicationInfo> getAppList() {
        return appList;
    }

    public static void setShortcutPreference(int keycode, String app) {
        Editor edit = pref.edit();

        String shortcut_pref =
                "shortcut_f" + ((keycode - KeyEvent.KEYCODE_F1) + 1);

        if (app == null) {
            wm.setApplicationShortcut(keycode, null);
            edit.putString(shortcut_pref, "No shortcut");
            pkg[keycode - KeyEvent.KEYCODE_F7] = "No shortcut";
        } else if (app.equals("home")) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setPackage(app);
            wm.setApplicationShortcut(keycode, home);
            edit.putString(shortcut_pref, app);
            pkg[keycode - KeyEvent.KEYCODE_F7] = app;
        } else {
            wm.setApplicationShortcut(keycode, pm.getLaunchIntentForPackage(app));
            edit.putString(shortcut_pref, app);
            pkg[keycode - KeyEvent.KEYCODE_F7] = app;
        }
        edit.commit();
    }
}