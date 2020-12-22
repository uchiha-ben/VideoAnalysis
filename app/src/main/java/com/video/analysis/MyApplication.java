package com.video.analysis;

import android.app.Application;

import androidx.multidex.MultiDex;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
    }
}
