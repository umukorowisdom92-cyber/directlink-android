package com.directlink.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("NEW_MESSAGE".equals(intent.getAction())) {
            String from = intent.getStringExtra("from");
            String content = intent.getStringExtra("content");
            String timestamp = intent.getStringExtra("timestamp");

            DirectLinkNotificationManager.getInstance().onMessageReceived(from, content, timestamp);
        }
    }
}
