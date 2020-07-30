package com.tss.quikpawn.app;

import android.app.Application;
import android.content.Context;

import com.androidnetworking.AndroidNetworking;
import com.tss.quikpawn.PreferencesManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class QuikPawnApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        QuikPawnApplication.context = getApplicationContext();
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                . writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.initialize(getApplicationContext(),okHttpClient);
        PreferencesManager.initializeInstance(getApplicationContext());
    }
    private static Context context;


    public static Context getAppContext() {
        return QuikPawnApplication.context;
    }
}
