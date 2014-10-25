package com.mani.rssfeeder.services;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Xml;

import com.mani.rssfeeder.database.RssFeedDBManager;
import com.mani.rssfeeder.model.RssFeedItem;
import com.mani.rssfeeder.receiver.UpdateNewFeedReceiver;
import com.mani.rssfeeder.util.DateTimeUtil;
import com.mani.rssfeeder.util.RssFeedPreferenceManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by maniselvaraj on 28/9/14.
 */
public class RssFeederService extends IntentService {

    /** Update interval set to 10 minutes **/
    private static final long FEEDS_UPDATE_INTERVAL = 10 * 60 * DateUtils.SECOND_IN_MILLIS;

    /** Rss feed url **/
    private String mRssFeedUrl = "http://feeds.feedburner.com/TechCrunch/social";

    /** The default socket timeout in milliseconds */
    public static final int DEFAULT_TIMEOUT_MS = 5000;

    /** Key to obtain Last-Modified from response header. **/
    private final String LAST_MODIFIED_HEADER = "Last-Modified";

    /** XML Tags for parsing rss feed response **/
    private final String RSS_XML_ITEM_TAG = "item";
    private final String RSS_XML_TITLE_TAG = "title";
    private final String RSS_XML_DESCRIPTION_TAG = "description";
    private final String RSS_XML_PUBLISHED_DATE_TAG = "pubDate";

    private final String TAG = "RssFeederService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RssFeederService() {
        super("RssFeederService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent ");

        long lastFetchedTime = RssFeedPreferenceManager.getLong(
                RssFeedPreferenceManager.PreferenceKeys.LAST_FETCHED_TIME, -1);

        if ( lastFetchedTime > 0 &&
                (System.currentTimeMillis() - lastFetchedTime) < FEEDS_UPDATE_INTERVAL ) {
            Log.d(TAG, " Ignoring this sync cycle as this is triggered within the minimum time to check.");
            setAlarm();
            return;
        }

        // Use HTTP HEAD request to get the last modified time and then
        // decide whether to get the latest feeds.
        try {
            long lastModifiedTime = RssFeedPreferenceManager.getLong(
                    RssFeedPreferenceManager.PreferenceKeys.LAST_PUBLISHED_DATE, -1 );
            if (-1 == lastModifiedTime) {
                //Very first time last modified time would be zero. Go ahead and get feeds.
                Log.d(TAG, " Very first time get last modified time and get feeds as well. ");
                long lastModified = getLastModifiedUsingHeadRequest();

                //Update the last modified time.
                RssFeedPreferenceManager.setLong(
                        RssFeedPreferenceManager.PreferenceKeys.LAST_PUBLISHED_DATE, lastModified);
                getFeeds();
            } else {
                long currentLastModified = getLastModifiedUsingHeadRequest();
                if ( currentLastModified > lastModifiedTime) {
                    //Update the last modified time.
                    RssFeedPreferenceManager.setLong(
                            RssFeedPreferenceManager.PreferenceKeys.LAST_PUBLISHED_DATE, currentLastModified );

                    //Some new feeds are available. Get feeds now.
                    getFeeds();
                } else {
                    Log.d(TAG, " No new feeds available. ");
                }
            }

        } catch (IOException e) {
            // Do nothing. Retry in next 10 minute.
            e.printStackTrace();
        }

        //Update the last fetched time.
        RssFeedPreferenceManager.setLong(RssFeedPreferenceManager.PreferenceKeys.LAST_FETCHED_TIME,
                System.currentTimeMillis());

        //Set the alarm again to make it as repeating.
        setAlarm();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setAlarm() {
        Intent syncIntent = new Intent(this, RssFeederService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
                this, 0, syncIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        //Add the update interval to system clock's elapsed real time.
        long triggerInMillis = SystemClock.elapsedRealtime() + FEEDS_UPDATE_INTERVAL;

        final AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //Used alarm mode as ELAPSED_REALTIME to avoid waking up the device in sleep.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.ELAPSED_REALTIME, triggerInMillis, pendingIntent);
        } else {
            am.set(AlarmManager.ELAPSED_REALTIME, triggerInMillis, pendingIntent);
        }
    }

    private void getFeeds() throws IOException {
        List<RssFeedItem> feedItems = getTechCrunchFeeds();
        if (feedItems != null) {
            for(RssFeedItem item : feedItems) {
                try {
                    RssFeedDBManager.getInstance().addFeedItem(item);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            // Once we have the new feeds list, update the loader by broadcasting local message.
            // Local Broadcast manager is used which makes sense, as this broadcast information
            // doesn't leave the app.
            Intent broadCastIntent = new Intent(UpdateNewFeedReceiver.ACTION_UPDATE_NEW_FEEDS);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadCastIntent);
        }
    }

    private long getLastModifiedUsingHeadRequest() throws IOException {

        URL parsedUrl = new URL(mRssFeedUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) parsedUrl.openConnection();
        urlConnection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
        urlConnection.setReadTimeout(DEFAULT_TIMEOUT_MS);
        urlConnection.setRequestMethod("HEAD"); //Request method set to HEAD

        int responseCode = urlConnection.getResponseCode();
        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could not be retrieved.
            // Signal to the caller that something was wrong with the connection.
            urlConnection.disconnect();
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }

        long lastModified = getLastModifiedTimeFromHeaders(urlConnection);
        urlConnection.disconnect();
        return lastModified;
    }

    private long getLastModifiedTimeFromHeaders(HttpURLConnection urlConnection) {
        String lastModified = null;
        for (Map.Entry<String, List<String>> header : urlConnection.getHeaderFields().entrySet()) {
            if (header.getKey() != null) {
                if (header.getKey().equals(LAST_MODIFIED_HEADER)) {
                    lastModified = header.getValue().get(0);
                }
            }
        }
        return lastModified == null ? -1 : DateTimeUtil.getTimeInMillis(lastModified);
    }

    private List<RssFeedItem> getTechCrunchFeeds() throws IOException {
        List<RssFeedItem> feedItemsList = null;
        URL parsedUrl = new URL(mRssFeedUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) parsedUrl.openConnection();
        urlConnection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
        urlConnection.setReadTimeout(DEFAULT_TIMEOUT_MS);
        urlConnection.setRequestMethod("GET"); //Request method set to GET
        InputStream in = null;
        try {
            in = new BufferedInputStream(urlConnection.getInputStream());
            feedItemsList = parseResponse(in);
        } catch (XmlPullParserException e) {
            //Do nothing return empty list;
        } catch (IOException e) {
            //Do nothing return empty list;
        } finally {
            //Disconnect the connection
            urlConnection.disconnect();
        }

        return feedItemsList;
    }

    private List<RssFeedItem> parseResponse(InputStream inputStream) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, null);

        List<RssFeedItem> feedItemsList = null;
        RssFeedItem feedItem = null;

        long latestPublishedDate = RssFeedDBManager.getInstance().getLatestFeedPublishedDate();

        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                    feedItemsList = new ArrayList<RssFeedItem>();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals(RSS_XML_ITEM_TAG)){
                        feedItem = new RssFeedItem();
                    } else if (feedItem != null){
                        if (name.equals(RSS_XML_TITLE_TAG)){
                            feedItem.setTitle(parser.nextText());
                        } else if (name.equals(RSS_XML_DESCRIPTION_TAG)){
                            cleanUpDescription(feedItem, parser.nextText());
                        } else if (name.equals(RSS_XML_PUBLISHED_DATE_TAG)){
                            feedItem.setDate(DateTimeUtil.getTimeInMillis(parser.nextText()));
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase(RSS_XML_ITEM_TAG) &&
                            feedItemsList != null &&
                            feedItem != null){
                        //Add to the list only if the published date is larger than the published
                        // date of latest feed found in database.
                        if ( feedItem.getDate() > latestPublishedDate) {
                            feedItemsList.add(feedItem);
                        }
                    }

            }
            eventType = parser.next();
        }

        return feedItemsList;
    }

    private void cleanUpDescription(RssFeedItem feedItem, String description) {
        if (description != null && feedItem != null) {
            setHyperLinkToFeed(feedItem, description);
            //Remove <img......./>
            description = description.replaceAll("<img(.*?)/>", "");
            //Remove <a ......> </a>
            description = description.replaceAll("<a(.*?)</a>", "");
            feedItem.setDescription(description);
        }
    }

    private void setHyperLinkToFeed(RssFeedItem feedItem, String description) {
        //Pattern - looks for "href=....." value.
        String pattern = "href=\"(.*?)\"";
        Matcher m = Pattern.compile(pattern).matcher(description);
        String url = null;
        if (m.find()) {
            url = m.group(1);
        }

        if ( url != null) {
            feedItem.setHyperlink(url);
        }

    }
}
