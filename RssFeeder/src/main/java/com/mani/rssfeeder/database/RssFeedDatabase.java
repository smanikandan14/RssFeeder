package com.mani.rssfeeder.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by maniselvaraj on 29/9/14.
 */
public class RssFeedDatabase extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "rssfeeds.db";

    public RssFeedDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table if not exists " + RssFeedDBConstants.RSS_FEED_TABLE +
                "(" + RssFeedDBConstants.RSS_FEED.FEED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RssFeedDBConstants.RSS_FEED.FEED_TITLE + " TEXT," +
                RssFeedDBConstants.RSS_FEED.FEED_DESCRIPTION + " TEXT," +
                RssFeedDBConstants.RSS_FEED.FEED_URL + " TEXT," +
                RssFeedDBConstants.RSS_FEED.FEED_DATE + " INTEGER, " +
                "UNIQUE ("+RssFeedDBConstants.RSS_FEED.FEED_TITLE+","+
                RssFeedDBConstants.RSS_FEED.FEED_DESCRIPTION+","+
                RssFeedDBConstants.RSS_FEED.FEED_URL+")"+
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // [NOT TAKEN CARE for the DEMO]
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + RssFeedDBConstants.RSS_FEED_TABLE);
    }
}
