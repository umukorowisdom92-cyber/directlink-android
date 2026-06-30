package com.directlink.app;

import java.util.HashMap;
import java.util.Map;

public class NotificationManager {
    private static NotificationManager instance;
    private Map<String, Integer> unreadCounts = new HashMap<>();
    private Map<String, String> lastMessages = new HashMap<>();
    private Map<String, String> lastTimestamps = new HashMap<>();
    private static MainActivity mainActivity;
    private int totalUnread = 0;

    private NotificationManager() {}

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public static void setMainActivity(MainActivity activity) {
        mainActivity = activity;
    }

    public static void onMessageReceived(String sender, String message, String timestamp) {
        NotificationManager mgr = getInstance();
        int currentCount = mgr.unreadCounts.getOrDefault(sender, 0);
        mgr.unreadCounts.put(sender, currentCount + 1);
        mgr.totalUnread++;

        mgr.lastMessages.put(sender, message);
        mgr.lastTimestamps.put(sender, timestamp);

        if (mainActivity != null) {
            mainActivity.updateChatListOnNewMessage(sender, message, timestamp);
            mainActivity.updateUnreadBadge();
        }
    }

    public static void clearUnread(String chatPartner) {
        NotificationManager mgr = getInstance();
        if (chatPartner != null && mgr.unreadCounts.containsKey(chatPartner)) {
            int removed = mgr.unreadCounts.remove(chatPartner);
            mgr.totalUnread = Math.max(0, mgr.totalUnread - removed);
        }
    }

    public static int getUnreadCount(String chatPartner) {
        NotificationManager mgr = getInstance();
        return mgr.unreadCounts.getOrDefault(chatPartner, 0);
    }

    public static int getTotalUnread() {
        NotificationManager mgr = getInstance();
        return mgr.totalUnread;
    }

    public static String getLastMessage(String chatPartner) {
        NotificationManager mgr = getInstance();
        return mgr.lastMessages.get(chatPartner);
    }

    public static String getLastTimestamp(String chatPartner) {
        NotificationManager mgr = getInstance();
        return mgr.lastTimestamps.get(chatPartner);
    }

    public static void refreshChatList() {
        if (mainActivity != null) {
            mainActivity.refreshChatList();
        }
    }
}
