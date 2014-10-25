package com.mani.rssfeeder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mani.rssfeeder.services.RssFeederService;

/**
 * Created by manikandan.selvaraju on 9/30/14.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent msgIntent = new Intent(context, RssFeederService.class);
            context.startService(msgIntent);
        }
    }
}
