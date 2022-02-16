/*
 * Copyright (c) 2020 UnifyID, Inc. All rights reserved.
 * Unauthorized copying or excerpting via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package id.unify.gaitauth_sample_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private static SharedPreferences sharedPreferences;

    private static final String SHARED_PREFERENCES = "id.unify.gaitauth_sample_app.SHARED_PREFERENCES";
    public static final String SDK_KEY = "id.unify.gaitauth_sample_app.SHARED_PREFERENCES.SDK_KEY";
    public static final String LAST_MODEL_PULL_TS = "id.unify.gaitauth_sample_app.SHARED_PREFERENCES.LAST_MODEL_PULL_TS";
    public static final String FEATURE_UPLOADED_COUNT = "id.unify.gaitauth_sample_app.SHARED_PREFERENCES.FEATURE_UPLOADED_COUNT";
    public static final String FEATURE_COLLECTED_COUNT = "id.unify.gaitauth_sample_app.SHARED_PREFERENCES.FEATURE_COLLECTED_COUNT";
    public static final String INSTALL_ID = "id.unify.gaitauth_sample_app.SHARED_PREFERENCES.INSTALL_ID";
    public static final String CLIENT_ID = "id.unify.gaitauth_sample_app.SHARED_PREFERENCES.CLIENT_ID";
    public static final String CUSTOMER_ID = "id.unify.gaitauth_sample_app.SHARED_PREFERENCES.CUSTOMER_ID";
    public static final String MODEL_ID = "id.unify.gaitauth_sample_app.SHARED_PREFERENCES.MODEL_ID";

    synchronized public static void initialize(Context context) {
        if (Preferences.sharedPreferences == null) {
            Preferences.sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES, 0);
        }
    }

    synchronized public static int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    synchronized public static void put(String key, int i) {
        sharedPreferences.edit().putInt(key, i).apply();
    }

    synchronized public static String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    synchronized public static void put(String key, String val) {
        sharedPreferences.edit().putString(key, val).apply();
    }

    synchronized public static long getLong(String key) {
        return sharedPreferences.getLong(key, 0);
    }

    synchronized public static void put(String key, long i) {
        sharedPreferences.edit().putLong(key, i).apply();
    }

    @SuppressLint("ApplySharedPref")
    synchronized public static void remove(String key) {
        sharedPreferences.edit().remove(key).commit();
    }

    @SuppressLint("ApplySharedPref")
    synchronized public static void clear() {
        sharedPreferences.edit().clear().commit();
    }
}
