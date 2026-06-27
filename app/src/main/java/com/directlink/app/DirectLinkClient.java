package com.directlink.app;

public class DirectLinkClient {
    static {
        try {
            System.loadLibrary("directlink_android_rust");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }
    
    public static native void init(String serverUrl);
    public static native String register(String username, String phone, String password);
    public static native String login(String phone, String password);
    public static native String checkUser(String phone);
    public static native String getContacts();
    public static native String createGroup(String name, String membersJson);
    public static native void freeString(String ptr);
}
