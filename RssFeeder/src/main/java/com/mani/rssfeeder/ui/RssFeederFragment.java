package com.mani.rssfeeder.ui;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ListView;
import android.widget.Toast;

import com.mani.rssfeeder.R;
import com.mani.rssfeeder.adapter.RssFeedListAdapter;
import com.mani.rssfeeder.loader.RssFeedsLoader;
import com.mani.rssfeeder.model.RssFeedItem;

import java.util.List;

/**
 * Created by maniselvaraj on 29/9/14.
 */
public class RssFeederFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<RssFeedItem>> {

    private RssFeedListAdapter mAdapter;

    // id specific to the ListFragment's LoaderManager
    private static final int LOADER_ID = 1;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getListView().setDivider(null);
        getListView().setSelector(new ColorDrawable(0x0));
        mAdapter = new RssFeedListAdapter();
        setEmptyText(getResources().getString(R.string.no_feeds));
        setListAdapter(mAdapter);
        setListShown(false);

        //Initialize a Loader with id '1'. If the Loader with this id already
        // exists, then the LoaderManager will reuse the existing Loader.
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        final Animation aAnim = new AlphaAnimation(1.0f, 0.8f);
        aAnim.setFillAfter(true);
        v.startAnimation(aAnim);

        //Open browser with the url.
        RssFeedItem feedItem = (RssFeedItem) mAdapter.getItem(position);
        if (feedItem.getHyperlink() != null) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(feedItem.getHyperlink()));
            startActivity(browserIntent);
        } else {
            Toast.makeText(this.getActivity(),"Resource of feed not found",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<List<RssFeedItem>> onCreateLoader(int i, Bundle bundle) {
        return new RssFeedsLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<RssFeedItem>> listLoader, List<RssFeedItem> rssFeedItems) {

        //Loading progress will be shown until at least one feed is obtained.
        if( rssFeedItems != null && rssFeedItems.size() > 0) {
            mAdapter.setFeedItems(rssFeedItems);
            setListShown(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<RssFeedItem>> listLoader) {
        mAdapter.setFeedItems(null);
    }
}
