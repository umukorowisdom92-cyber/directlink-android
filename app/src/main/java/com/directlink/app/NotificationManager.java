package com.directlink.app;

import java.util.HashMap;
import java.util.Map;

public class NotificationManager {
    private static NotificationManager instance;
    private Map<String, Integer> unreadCounts = new HashMap<>();
    private Map<String, String> lastMessages = new HashMap<>();
    private Map<String, String> lastTimestamps = new HashMap<>();
    private MainActivity mainActivity;
    private int totalUnread = 0;

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
        int currentCount = unreadCounts.getOrDefault(sender, 0);
        unreadCounts.put(sender, currentCount + 1);
        totalUnread++;
        
        lastMessages.put(sender, message);
        lastTimestamps.put(sender, timestamp);
        
        if (mainActivity != null) {
            mainActivity.updateChatListOnNewMessage(sender, message, timestamp);
            mainActivity.updateUnreadBadge();
        }
    }

    public void clearUnread(String sender) {
        int count = unreadCounts.getOrDefault(sender, 0);
        totalUnread = Math.max(0, totalUnread - count);
        unreadCounts.put(sender, 0);
        if (mainActivity != null) {
            mainActivity.refreshChatList();
            mainActivity.updateUnreadBadge();
        }
    }

    public int getUnreadCount(String sender) {
        return unreadCounts.getOrDefault(sender, 0);
    }

    public int getTotalUnreadCount() {
        return totalUnread;
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
        totalUnread = 0;
    }
}
