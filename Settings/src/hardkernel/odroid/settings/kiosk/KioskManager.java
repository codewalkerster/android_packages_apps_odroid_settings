package hardkernel.odroid.settings.kiosk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import hardkernel.odroid.settings.EnvProperty;

public class KioskManager {
    private static String pkg;

    private static SharedPreferences pref;
    private static final String PREFERENCE_NAME = "kiosk_preference";

    private static PackageManager pm;

    private static void init(Context context) {
        if (pm == null)
            pm = context.getPackageManager();
        if (pref == null)
            pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public static void onReceive(Context context) {
        launchKioskTarget(context);
    }

    public static void launchKioskTarget(Context context) {
        init(context);

        ArrayList<String> appList = getAvailableAppList(context);

        pkg = pref.getString("kiosk_target", "No kiosk target");

        if (pkg.equals("No kiosk target"))
            return;

        for (String app : appList) {
            if (app.equals(pkg)) {
                Intent launchApp= pm.getLaunchIntentForPackage(app);
                launchApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(launchApp);
            }
        }
    }

    public static String pkg() {
        return pkg;
    }

    public static CharSequence pkgName() {
        ApplicationInfo app;

        try {
            app = pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA);
        } catch(Exception e) {
            pkg = "No kiosk target";
            return "No kiosk target";
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

        return launchApps;
    }

    public static List<ApplicationInfo> getAppList() {
        return appList;
    }

    public static void setKioskPreference(String app) {
        Editor edit = pref.edit();

        String kiosk_pref = "kiosk_target";

        if (app == null) {
            edit.putString(kiosk_pref, "No kiosk target");
            pkg = "No kiosk target";
        } else {
            edit.putString(kiosk_pref, app);
            pkg = app;
        }
        edit.commit();
    }
}
