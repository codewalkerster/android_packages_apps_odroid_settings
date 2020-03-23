package com.hardkernel.odroid.settings.update;

import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import com.hardkernel.odroid.settings.EnvProperty;
import com.hardkernel.odroid.settings.R;
import com.hardkernel.odroid.settings.MainApplication;
import com.hardkernel.odroid.settings.util.DroidUtils;

import java.util.HashMap;
import java.util.Map;

class packageName {
    final String TAG = "packageName";

    private static final String HEADER = "updatepackage";
    private static final String MODEL;
    private static final String ARCH;

    private int version = -1;

    static {
        Map<Integer, String> sdkVersion = new HashMap();
        sdkVersion.put(28, "9.0.0");

        int sdk = Integer.parseInt(EnvProperty.get("ro.build.version.sdk"));

        MODEL = EnvProperty.get("ro.hardware", "odroid");
        ARCH = DroidUtils.is64Bit() ? "64bit" : "32bit";
    }

    public packageName(String packageName) {
        setBuildNumber(packageName);
    }

    public packageName(int number) {
        version = number;
    }

    public int getVersion() {
        return version;
    }

    private void setBuildNumber(String packageName) {
        Log.e(TAG, "setBuildNumber(" + packageName + ")");
        version = parseBuildNumber(packageName);
        if (version == -1)
            Log.e(TAG, "wrong package name");
    }

    private String getArch() {
        return ARCH;
    }

    private int parseBuildNumber (String packageName) {
        String[] s = packageName.split("-");
        if (s.length <= 3)
            return -1;

        if (!s[0].equals(HEADER) || !s[1].equals(MODEL) ||
                !s[2].equals(getArch()))
            return -1;

        return Integer.parseInt(s[3].split("\\.")[0]);
    }

    public String getName() {
        return buildName(version);
    }

    private String buildName(int buildNumber) {
        if (buildNumber == -1)
            return null;

        return HEADER + "-" + MODEL + "-" + getArch() + "-"
                + Integer.toString(buildNumber) + ".zip";
    }
}
