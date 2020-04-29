package com.tss.quikpawn.app;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.tss.quikpawn.PreferencesManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class QuikPawnApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                . writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.initialize(getApplicationContext(),okHttpClient);
        PreferencesManager.initializeInstance(getApplicationContext());
    }
}
