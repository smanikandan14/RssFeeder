package com.mani.rssfeeder.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mani.rssfeeder.app.RssFeederApplication;
import com.mani.rssfeeder.model.RssFeedItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maniselvaraj on 29/9/14.
 */
public class RssFeedDBManager {

    private static SQLiteDatabase rssFeedDB;
    private static RssFeedDatabase rssFeedDBCreator;
    private static RssFeedDBManager instance;

    public static RssFeedDBManager getInstance() {
        if( instance == null ) {
            synchronized (RssFeedDBManager.class) {
                if (instance == null) {
                    instance = new RssFeedDBManager();
                }
            }
        }
        return instance;
    }

    public RssFeedDBManager() {
        rssFeedDBCreator = new RssFeedDatabase(RssFeederApplication.getContext());
        open();
    }

    public void open() throws SQLException {
        rssFeedDB = rssFeedDBCreator.getWritableDatabase();
    }

    public static void release() {
        close();
    }

    public static void close() {
        if( rssFeedDB != null) {
            rssFeedDB.close();
            rssFeedDB = null;
        }
        if(rssFeedDBCreator != null) {
            rssFeedDBCreator.close();
            rssFeedDBCreator = null;
        }
    }

    public long addFeedItem(RssFeedItem feedItem) throws SQLException {
        long insertId = 0;
        ContentValues values = new ContentValues();
        values.put(RssFeedDBConstants.RSS_FEED.FEED_TITLE, feedItem.getTitle());
        values.put(RssFeedDBConstants.RSS_FEED.FEED_DESCRIPTION, feedItem.getDescription());
        values.put(RssFeedDBConstants.RSS_FEED.FEED_URL, feedItem.getHyperlink());
        values.put(RssFeedDBConstants.RSS_FEED.FEED_DATE, feedItem.getDate());

        try {
            insertId = rssFeedDB.insertOrThrow(RssFeedDBConstants.RSS_FEED_TABLE, null,values);
        } catch (SQLException ex) {
            throw ex;
        }
        return insertId;
    }

    public List<RssFeedItem> getFeeds() {
        Cursor cursor = rssFeedDB.query(RssFeedDBConstants.RSS_FEED_TABLE,
                null, null, null, null, null, RssFeedDBConstants.RSS_FEED.FEED_DATE + " DESC");

        List<RssFeedItem> feedItemList = new ArrayList<RssFeedItem>();

        if (cursor != null ) {
            if  (cursor.moveToFirst()) {
                do {
                    RssFeedItem feedItem = new RssFeedItem();
                    String title = cursor.getString(cursor.getColumnIndex(RssFeedDBConstants.RSS_FEED.FEED_TITLE));
                    String description = cursor.getString(cursor.getColumnIndex(RssFeedDBConstants.RSS_FEED.FEED_DESCRIPTION));
                    String url = cursor.getString(cursor.getColumnIndex(RssFeedDBConstants.RSS_FEED.FEED_URL));
                    long timeInMillis = cursor.getLong(cursor.getColumnIndex(RssFeedDBConstants.RSS_FEED.FEED_DATE));

                    feedItem.setTitle(title);
                    feedItem.setDescription(description);
                    feedItem.setHyperlink(url);
                    feedItem.setDate(timeInMillis);
                    feedItemList.add(feedItem);
                } while (cursor.moveToNext());
            }
        }

        if(cursor != null) {
            cursor.close();
        }

        return feedItemList;

    }

    public long getLatestFeedPublishedDate() {
        Cursor cursor = rssFeedDB.query(RssFeedDBConstants.RSS_FEED_TABLE,
                null, null, null, null, null, RssFeedDBConstants.RSS_FEED.FEED_DATE+" DESC LIMIT 1");

        long publishedDate = 0;
        if (cursor != null ) {
            if (cursor.moveToFirst()) {
                publishedDate = cursor.getLong(cursor.getColumnIndex(RssFeedDBConstants.RSS_FEED.FEED_DATE));
            }
        }

        if(cursor != null) {
            cursor.close();
        }

        return publishedDate;
    }
}

