package com.hardkernel.odroid.settings.update;

import android.os.Build;

class packageName {
    private static final String HEADER = "updatepackage";
    private static final String MODEL = "odroidn2";
    private static final String VARIANT = "eng";
    private static final String BRANCH = "s922_9.0.0_master";
    private static final String BRANCH_64 = "s922_9.0.0_64_master";

    private int version = -1;

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
        String branch = BRANCH;
        final boolean is64bit = Build.SUPPORTED_64_BIT_ABIS.length > 0;
        if (is64bit)
            branch = BRANCH_64;
        return branch;
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
