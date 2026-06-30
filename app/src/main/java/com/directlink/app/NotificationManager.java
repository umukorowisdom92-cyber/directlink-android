package com.directlink.app;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.core.app.NotificationCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationManager {
    private static NotificationManager instance;
    private Context context;
    private static final String CHANNEL_ID = "directlink_channel";
    private static final int NOTIFICATION_ID = 1001;

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    private NotificationManager() {}

    public void init(Context context) {
        this.context = context.getApplicationContext();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "DirectLink Messages",
                android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new DirectLink messages");
            android.app.NotificationManager manager = 
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void showNotification(String title, String message) {
        if (context == null) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);

        android.app.NotificationManager manager = 
            (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public void onMessageReceived(String from, String content, String timestamp) {
        // Show notification
        showNotification("New message from " + from, content);
        
        // Update badge count in SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("DirectLinkPrefs", Context.MODE_PRIVATE);
        int currentBadge = prefs.getInt("badge_count", 0);
        prefs.edit().putInt("badge_count", currentBadge + 1).apply();
    }

    public int getBadgeCount() {
        if (context == null) return 0;
        SharedPreferences prefs = context.getSharedPreferences("DirectLinkPrefs", Context.MODE_PRIVATE);
        return prefs.getInt("badge_count", 0);
    }

    public void clearBadgeCount() {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences("DirectLinkPrefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("badge_count", 0).apply();
    }
}
