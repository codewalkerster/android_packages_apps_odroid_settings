package com.hardkernel.odroid.settings.update;

import android.content.res.Resources;
import android.os.Build;
import android.os.SystemProperties;

import com.hardkernel.odroid.settings.R;
import com.hardkernel.odroid.settings.MainApplication;

import java.util.HashMap;
import java.util.Map;

class packageName {
    private static final String HEADER = "updatepackage";
    private static final String MODEL;
    private static final String VARIANT = "eng";
    private static final String BRANCH;

    private int version = -1;

    static {
        Map<Integer, String> sdkVersion = new HashMap();
        sdkVersion.put(28, "9.0.0");

        int sdk = Integer.parseInt(SystemProperties.get("ro.build.version.sdk"));

        boolean is64Bit = Build.SUPPORTED_64_BIT_ABIS.length > 0;

        MODEL = SystemProperties.get("ro.hardware", "odroid");
        BRANCH = SystemProperties.get("ro.chip", "s922") + "_" +
                sdkVersion.get(sdk) + "_" +
                (is64Bit? "64_master": "master");
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
        version = parseBuildNumber(packageName);
    }

    private String getBranch() {
        return BRANCH;
    }

    private int parseBuildNumber (String packageName) {
        String[] s = packageName.split("-");
        if (s.length <= 4)
            return -1;

        if (!s[0].equals(HEADER) || !s[1].equals(MODEL) ||
                !s[2].equals(VARIANT) || !s[3].equals(getBranch()))
            return -1;

        return Integer.parseInt(s[4].split("\\.")[0]);
    }

    public String getName() {
        return buildName(version);
    }

    private String buildName(int buildNumber) {
        if (buildNumber == -1)
            return null;

        return HEADER + "-" + MODEL + "-" + VARIANT + "-" + getBranch() + "-"
                + Integer.toString(buildNumber) + ".zip";
    }
}
