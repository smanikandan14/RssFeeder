package com.mani.rssfeeder.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mani.rssfeeder.services.RssFeederService;
import com.mani.rssfeeder.util.RssFeedPreferenceManager;

/**
 * Created by maniselvaraj on 29/9/14.
 */
public class RssFeederApplication  extends Application {

    private static Context applicationContext;
    private SharedPreferences sharedPreferences;

    public void onCreate() {
        super.onCreate();
        applicationContext = this.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        RssFeedPreferenceManager.initializePreferenceManager(sharedPreferences);

        //Start the sync service only once when application is coming alive first time.
        Intent msgIntent = new Intent(this, RssFeederService.class);
        startService(msgIntent);
    }

    public static Context getContext() {
        return applicationContext;
    }


}
