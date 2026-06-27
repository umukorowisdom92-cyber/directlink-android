package com.directlink.app;

public class ChatItem {
    private String name;
    private String lastMessage;
    private String time;
    private int badgeCount;
    private boolean online;

    public ChatItem(String name, String lastMessage, String time, int badgeCount, boolean online) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
        this.badgeCount = badgeCount;
        this.online = online;
    }

    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public int getBadgeCount() { return badgeCount; }
    public boolean isOnline() { return online; }
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
