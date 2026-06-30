package com.directlink.app;

public class Contact {
    private String username;
    private String phone_number;  // Changed from phoneNumber to match server
    private String qr_id;          // Added qr_id from server
    private boolean online;

    public Contact(String username, String phone_number, String qr_id, boolean online) {
        this.username = username;
        this.phone_number = phone_number;
        this.qr_id = qr_id;
        this.online = online;
    }

    public String getUsername() { return username; }
    public String getPhoneNumber() { return phone_number; }
    public String getQrId() { return qr_id; }
    public boolean isOnline() { return online; }
    
    public void setOnline(boolean online) { this.online = online; }
}
