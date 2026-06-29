package com.directlink.app;

import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;

public class NotificationManager {
    private static NotificationManager instance;
    private Map<String, Integer> unreadCounts = new HashMap<>();
    private Map<String, String> lastMessages = new HashMap<>();
    private Map<String, String> lastTimestamps = new HashMap<>();
    private MainActivity mainActivity;

    private NotificationManager() {}

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void setMainActivity(MainActivity activity) {
        this.mainActivity = activity;
    }

    public void onMessageReceived(String sender, String message, String timestamp) {
        // Update unread count
        int currentCount = unreadCounts.getOrDefault(sender, 0);
        unreadCounts.put(sender, currentCount + 1);
        
        // Update last message
        lastMessages.put(sender, message);
        lastTimestamps.put(sender, timestamp);
        
        // Notify MainActivity to update chat list
        if (mainActivity != null) {
            mainActivity.updateChatListOnNewMessage(sender, message, timestamp);
        }
    }

    public void clearUnread(String sender) {
        unreadCounts.put(sender, 0);
        if (mainActivity != null) {
            mainActivity.refreshChatList();
        }
    }

    public int getUnreadCount(String sender) {
        return unreadCounts.getOrDefault(sender, 0);
    }

    public String getLastMessage(String sender) {
        return lastMessages.getOrDefault(sender, "Tap to chat");
    }

    public String getLastTimestamp(String sender) {
        return lastTimestamps.getOrDefault(sender, "Now");
    }

    public void reset() {
        unreadCounts.clear();
        lastMessages.clear();
        lastTimestamps.clear();
    }
}
