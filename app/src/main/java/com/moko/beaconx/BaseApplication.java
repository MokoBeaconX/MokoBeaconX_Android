package com.moko.beaconx;

import android.app.Application;
import android.content.Intent;

import com.moko.beaconx.service.MokoService;
import com.moko.support.MokoSupport;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MokoSupport.getInstance().init(getApplicationContext());
    }
}
