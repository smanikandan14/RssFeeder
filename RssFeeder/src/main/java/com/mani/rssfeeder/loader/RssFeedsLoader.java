package com.mani.rssfeeder.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mani.rssfeeder.database.RssFeedDBManager;
import com.mani.rssfeeder.model.RssFeedItem;
import com.mani.rssfeeder.receiver.UpdateNewFeedReceiver;

import java.util.List;

/**
 * Created by manikandan.selvaraju on 9/30/14.
 */
public class RssFeedsLoader extends AsyncTaskLoader<List<RssFeedItem>> {

    /** List of feedItems **/
    List<RssFeedItem> mRssFeedItems;

    // The observer to notify the Loader when there is a new feed.
    private UpdateNewFeedReceiver mUpdateNewFeedReceiver;

    private final String TAG = "RssFeedsLoader";

    public RssFeedsLoader(Context context) {
        super(context);
    }

    @Override
    public List<RssFeedItem> loadInBackground() {
        Log.d(TAG,"loadInBackground()");
        mRssFeedItems = RssFeedDBManager.getInstance().getFeeds();
        return mRssFeedItems;
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
        Log.d(TAG, "forceLoad()");
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.d(TAG,"onStartLoading()");

        if(mRssFeedItems != null) {
            deliverResult(mRssFeedItems);
        }

        if (mUpdateNewFeedReceiver == null) {
            mUpdateNewFeedReceiver = new UpdateNewFeedReceiver(this);
        }

        if (takeContentChanged() || mRssFeedItems == null) {
            // When the updatenewfeed receiver receives a new feeds notification, it will call
            // onContentChanged() on the Loader, which will cause the next call to
            // takeContentChanged() to return true. So force a new load.
            Log.d(TAG,"A content change has been detected, so force load!");
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        Log.d(TAG, "onStopLoading()");
        cancelLoad();
    }

    @Override
    protected void onReset() {
        Log.d(TAG, "onReset()");
        onStopLoading();

        // The Loader is being reset, so we should stop listening for new feeds broadcast.
        if (mUpdateNewFeedReceiver != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUpdateNewFeedReceiver);
            mUpdateNewFeedReceiver = null;
        }

    }

    @Override
    public void onCanceled(List<RssFeedItem> apps) {
        // Attempt to cancel the current asynchronous load.
        super.onCanceled(apps);
        Log.d(TAG,"onCanceled()");
    }
}
