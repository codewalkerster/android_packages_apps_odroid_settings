package com.hardkernel.odroid.settings.update;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemProperties;


import com.hardkernel.odroid.settings.R;
import com.hardkernel.odroid.settings.MainApplication;

public class updateManager {
    public final static String OFFICIAL_URL;
    public final static String MIRROR_URL;

    public static final long PACKAGE_MAXSIZE = 500 * 1024 * 1024;   /* 500MB */
    public static final String LATEST_VERSION;

    static {
        boolean is64Bit = Build.SUPPORTED_64_BIT_ABIS.length > 0;
        LATEST_VERSION = is64Bit? "latestupdate_pie_64": "latestupdate_pie";

        int boardIdx = 0;
        String board = SystemProperties.get("ro.hardware", "odroidn2");

        Resources resource = MainApplication.getAppResources();
        String[] boardList = resource.getStringArray(R.array.model);
        for (;boardIdx < boardList.length; boardIdx++) {
            if (boardList[boardIdx].equals(board))
                break;
        }

        OFFICIAL_URL = resource.getStringArray(R.array.official_url)[boardIdx];
        MIRROR_URL = resource.getStringArray(R.array.mirror_url)[boardIdx];
    }

    public static final String KEY_OFFICIAL = "server_official";
    public static final String KEY_MIRROR = "server_mirror";
    public static final String KEY_CUSTOM = "server_custom";
    public static final String KEY_CHECK_UPDATE = "check_update";

    private static String server = KEY_MIRROR;
    private static String url = MIRROR_URL;

    public static String getRemoteURL() {
        return url;
    }

    public static String getLatestVersion() {
        return LATEST_VERSION;
    }

    public static void setRemoteURL(String newURL) {
        setPreference(SH_KEY_URL, newURL);
        url = newURL;
    }

    public static final String SHPREF_UPDATE_SERVER = "update_server";
    private static final String SH_KEY_SERVER = "server";
    private static final String SH_KEY_URL = "url";

    private static SharedPreferences pref = null;

    public static void setPreference(SharedPreferences sharedPreferences) {
        if (pref == null)
            pref = sharedPreferences;
    }

    public static void initServer() {
        server = pref.getString(SH_KEY_SERVER, KEY_MIRROR);
    }

    public static void initURL() {
        setRemoteURL(pref.getString(SH_KEY_URL, MIRROR_URL));
    }

    public static String getServer() {
        return server;
    }

    public static Boolean isCheckAtBoot() {
        return pref.getBoolean(KEY_CHECK_UPDATE, true);
    }

    public static void setCheckUpdate(boolean check) {
        setPreference(KEY_CHECK_UPDATE, check);
    }

    public static void setServer(String serverName) {
        setPreference(SH_KEY_SERVER, serverName);
        server = serverName;
    }

    private static void setPreference(String target, Object value) {
        final SharedPreferences.Editor editor = pref.edit();

        if (value instanceof String)
            editor.putString(target, (String)value);
        else if (value instanceof Boolean)
            editor.putBoolean(target, (boolean)value);
        editor.commit();
    }
}
