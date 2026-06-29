package com.directlink.app;

public class ChatItem {
    public static final int TYPE_CHAT = 0;
    public static final int TYPE_FRIEND_REQUEST = 1;

    private int type;
    private String name;
    private String phone;
    private String lastMessage;
    private String time;
    private int badgeCount;
    private boolean online;
    private String requestId;

    public ChatItem(String name, String phone, String lastMessage, String time, int badgeCount, boolean online) {
        this.type = TYPE_CHAT;
        this.name = name;
        this.phone = phone;
        this.lastMessage = lastMessage;
        this.time = time;
        this.badgeCount = badgeCount;
        this.online = online;
    }

    public ChatItem(String name, String phone, String requestId) {
        this.type = TYPE_FRIEND_REQUEST;
        this.name = name;
        this.phone = phone;
        this.requestId = requestId;
        this.lastMessage = "📨 Friend request";
        this.time = "Now";
        this.badgeCount = 1;
        this.online = false;
    }

    public int getType() { return type; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public int getBadgeCount() { return badgeCount; }
    public boolean isOnline() { return online; }
    public String getRequestId() { return requestId; }

    public void setBadgeCount(int badgeCount) { this.badgeCount = badgeCount; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setTime(String time) { this.time = time; }
    public void setOnline(boolean online) { this.online = online; }

    public String getAvatarText() {
        if (name == null || name.isEmpty()) return "?";
        return String.valueOf(name.charAt(0)).toUpperCase();
    }

    public int getAvatarColor() {
        int hash = name.hashCode();
        int[] colors = {0xFF3F51B5, 0xFFE91E63, 0xFF4CAF50, 0xFFFF9800, 0xFF9C27B0, 0xFF00BCD4};
        return colors[Math.abs(hash) % colors.length];
    }
}
