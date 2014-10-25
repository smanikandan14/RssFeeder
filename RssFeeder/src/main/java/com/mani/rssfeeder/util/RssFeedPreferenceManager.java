package com.mani.rssfeeder.util;

import android.content.SharedPreferences;

public class RssFeedPreferenceManager {
	private static SharedPreferences preferences;

	public static class PreferenceKeys {
		public static String LAST_PUBLISHED_DATE = "last_published_date";
        public static String LAST_FETCHED_TIME = "last_fetched_time";
	}

	public static void initializePreferenceManager(SharedPreferences _preferences) {
		preferences = _preferences;
	}

    public static boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public static void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static Long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    public static void setLong(String key, long value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }
}
