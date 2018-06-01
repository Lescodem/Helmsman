package com.mmmmar.helmsman;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingManager {

    @SuppressLint("StaticFieldLeak")
    private static SettingManager sInstance;

    private Context context;
    private String keyService;
    private String keyCharacteristic;

    public SettingManager(Context context) {
        this.context = context.getApplicationContext();
        keyService = context.getString(R.string.pf_key_service);
        keyCharacteristic = context.getString(R.string.pf_key_characteristic);
    }

    public static SettingManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SettingManager(context);
        }
        return sInstance;
    }

    public String getServiceValue() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(keyService, context.getString(R.string.uuid_service));
    }

    public String getCharacteristicValue() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(keyCharacteristic, context.getString(R.string.uuid_characteristic));
    }
}
