package com.mani.rssfeeder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.mani.rssfeeder.loader.RssFeedsLoader;

/**
 * Created by manikandan.selvaraju on 9/30/14.
 */
public class UpdateNewFeedReceiver extends BroadcastReceiver {

    private RssFeedsLoader mFeedsLoader;

    public static final String ACTION_UPDATE_NEW_FEEDS = "action_NEW_FEEDS";

    public UpdateNewFeedReceiver(RssFeedsLoader loader) {
        mFeedsLoader = loader;
        IntentFilter filter = new IntentFilter(ACTION_UPDATE_NEW_FEEDS);
        LocalBroadcastManager.getInstance(mFeedsLoader.getContext()).registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mFeedsLoader.onContentChanged();
    }
}
