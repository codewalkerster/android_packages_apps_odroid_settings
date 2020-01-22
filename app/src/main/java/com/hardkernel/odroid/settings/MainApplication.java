package com.hardkernel.odroid.settings;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

public class MainApplication extends Application {
    private static Application sApplication;

    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    public static Resources getAppResources() {
        return sApplication.getApplicationContext().getResources();
    }
}