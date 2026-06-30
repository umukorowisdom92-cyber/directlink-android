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

    public void clearUnread(String chatPartner) {
        if (chatPartner != null && unreadCounts.containsKey(chatPartner)) {
            int removed = unreadCounts.remove(chatPartner);
            totalUnread = Math.max(0, totalUnread - removed);
        }
    }

    public int getUnreadCount(String chatPartner) {
        return unreadCounts.getOrDefault(chatPartner, 0);
    }

    public int getTotalUnread() {
        return totalUnread;
    }

    public String getLastMessage(String chatPartner) {
        return lastMessages.get(chatPartner);
    }

    public String getLastTimestamp(String chatPartner) {
        return lastTimestamps.get(chatPartner);
    }

    public void refreshChatList() {
        if (mainActivity != null) {
            mainActivity.refreshChatList();
        }
    }
}
