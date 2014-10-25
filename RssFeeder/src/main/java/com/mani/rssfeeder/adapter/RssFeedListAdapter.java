package com.mani.rssfeeder.adapter;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mani.rssfeeder.R;
import com.mani.rssfeeder.app.RssFeederApplication;
import com.mani.rssfeeder.model.RssFeedItem;
import com.mani.rssfeeder.util.DateTimeUtil;

import java.util.List;

/**
 * Created by maniselvaraj on 29/9/14.
 */
public class RssFeedListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    List<RssFeedItem> feedItemList;

    public RssFeedListAdapter() {
        mInflater = LayoutInflater.from(RssFeederApplication.getContext());
    }

    public void setFeedItems(List<RssFeedItem> data) {
        this.feedItemList = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return feedItemList == null ? 0 : feedItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return feedItemList == null ? null : feedItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.rssfeeder_list_item_layout, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.feedTitle = (TextView) convertView.findViewById(R.id.feedTitle);
            holder.feedDescription = (TextView) convertView.findViewById(R.id.feedDescription);
            holder.feedDate = (TextView) convertView.findViewById(R.id.feedTime);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RssFeedItem feedItem = feedItemList.get(position);
        holder.feedTitle.setText(Html.fromHtml(feedItem.getTitle()));
        holder.feedDescription.setText(Html.fromHtml(feedItem.getDescription()));
        holder.feedDate.setText(DateTimeUtil.getFormattedString(feedItem.getDate()));
        return convertView;
    }

    class ViewHolder {
        TextView feedTitle;
        TextView feedDescription;
        TextView feedDate;
    }
}
