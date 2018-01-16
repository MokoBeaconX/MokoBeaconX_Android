package com.moko.beaconx.utils;

import android.content.Context;
import android.content.SharedPreferences.Editor;

import com.moko.beaconx.AppConstants;

public class SPUtiles {
    public static void clearAllData(Context context) {
        Editor editor = context.getSharedPreferences(AppConstants.SP_NAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }

    public static void setStringValue(Context context, String key, String value) {
        Editor editor = context.getSharedPreferences(AppConstants.SP_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getStringValue(Context context, String key, String defValue) {
        return context.getSharedPreferences(AppConstants.SP_NAME, Context.MODE_PRIVATE).getString(key, defValue);
    }

    public static void setBooleanValue(Context context, String key, boolean value) {
        Editor editor = context.getSharedPreferences(AppConstants.SP_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBooleanValue(Context context, String key, boolean defValue) {
        return context.getSharedPreferences(AppConstants.SP_NAME, Context.MODE_PRIVATE).getBoolean(key, defValue);
    }

    public static void setIntValue(Context context, String key, int value) {
        Editor editor = context.getSharedPreferences(AppConstants.SP_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getIntValue(Context context, String key, int defValue) {
        return context.getSharedPreferences(AppConstants.SP_NAME, Context.MODE_PRIVATE).getInt(key, defValue);
    }

    public static void setFloatValue(Context context, String key, float value) {
        Editor editor = context.getSharedPreferences(AppConstants.SP_NAME, Context.MODE_PRIVATE).edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static float getFloatValue(Context context, String key, float defValue) {
        return context.getSharedPreferences(AppConstants.SP_NAME, Context.MODE_PRIVATE).getFloat(key, defValue);
    }
}
