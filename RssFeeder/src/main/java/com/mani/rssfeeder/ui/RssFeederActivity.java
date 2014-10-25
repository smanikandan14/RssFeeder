package com.mani.rssfeeder.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

import com.mani.rssfeeder.R;


public class RssFeederActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssfeeder_layout);
        // Create the ListFragment and add it as main container for the activity.
        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentById(android.R.id.content) == null) {
            RssFeederFragment list = new RssFeederFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }
}
