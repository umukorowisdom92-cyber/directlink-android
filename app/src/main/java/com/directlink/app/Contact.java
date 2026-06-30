package com.directlink.app;

public class Contact {
    private String username;
    private String phoneNumber;
    private boolean online;

    public Contact(String username, String phoneNumber, boolean online) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.online = online;
    }

    public String getUsername() { return username; }
    public String getPhoneNumber() { return phoneNumber; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
}
