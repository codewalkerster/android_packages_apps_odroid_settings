package com.hardkernel.odroid.settings.update;

import android.content.SharedPreferences;

public class updateManager {
    public final static String OFFICIAL_URL =
            "https://dn.odroid.com/S922/Android/ODROID-N2/";
    public final static String MIRROR_URL =
            "https://www.odroid.in/mirror/dn.odroid.com/S922/Android/ODROID-N2/";

    public static final long PACKAGE_MAXSIZE = 500 * 1024 * 1024;   /* 500MB */
    public static final String LATEST_VERSION = "latestupdate_pie";

    public static final String KEY_OFFICIAL = "server_official";
    public static final String KEY_MIRROR = "server_mirror";
    public static final String KEY_CUSTOM = "server_custom";

    private static String server = KEY_MIRROR;
    private static String url = MIRROR_URL;

    public static String getRemoteURL() {
        return url;
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

    public static void setServer(String serverName) {
        setPreference(SH_KEY_SERVER, serverName);
        server = serverName;
    }

    private static void setPreference(String target, String value) {
        final SharedPreferences.Editor editor = pref.edit();
        editor.putString(target, value);
        editor.commit();
    }
}
