package com.protel.yesterday.data;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.protel.yesterday.YesterdayApp;

/**
 * Created by erdemmac on 06/11/15.
 */
public class AppData {
    private static boolean isFahrenheitType = get();

    public static boolean isFahrenheit() {
        return isFahrenheitType;
    }

    public static void flipType() {
        isFahrenheitType = !isFahrenheitType;
        save();
    }

    private static final String KEY_IS_FAHRENEIT = "IS_FAHRENEIT";

    private static void save() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(YesterdayApp.getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_FAHRENEIT, isFahrenheit());
        editor.apply();
    }

    private static boolean get() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(YesterdayApp.getContext());
        return sharedPreferences.getBoolean(KEY_IS_FAHRENEIT, false);
    }

}
