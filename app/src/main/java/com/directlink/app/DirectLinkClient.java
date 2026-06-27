package com.directlink.app;

public class DirectLinkClient {
    private static boolean libraryLoaded = false;
    
    static {
        try {
            System.loadLibrary("directlink_android_rust");
            libraryLoaded = true;
            System.out.println("✅ Rust library loaded successfully!");
        } catch (UnsatisfiedLinkError e) {
            libraryLoaded = false;
            System.err.println("❌ Failed to load Rust library: " + e.getMessage());
        } catch (Exception e) {
            libraryLoaded = false;
            System.err.println("❌ Error loading Rust library: " + e.getMessage());
        }
    }
    
    public static boolean isLibraryLoaded() {
        return libraryLoaded;
    }
    
    // Native methods (only called if library is loaded)
    private static native void nativeInit(String serverUrl);
    private static native String nativeRegister(String username, String phone, String password);
    private static native String nativeLogin(String phone, String password);
    private static native String nativeCheckUser(String phone);
    private static native String nativeGetContacts();
    private static native String nativeCreateGroup(String name, String membersJson);
    private static native void nativeFreeString(String ptr);
    
    // Safe wrapper methods - handle errors gracefully
    public static void init(String serverUrl) {
        if (!libraryLoaded) {
            System.err.println("❌ Rust library not loaded, using Java fallback");
            return;
        }
        try {
            nativeInit(serverUrl);
        } catch (Exception e) {
            System.err.println("❌ init error: " + e.getMessage());
        }
    }
    
    public static String getContacts() {
        if (!libraryLoaded) {
            return "[{\"error\":\"Rust library not loaded\"}]";
        }
        try {
            return nativeGetContacts();
        } catch (Exception e) {
            return "[{\"error\":\"" + e.getMessage() + "\"}]";
        }
    }
    
    public static String register(String username, String phone, String password) {
        if (!libraryLoaded) {
            return "{\"error\":\"Rust library not loaded\"}";
        }
        try {
            return nativeRegister(username, phone, password);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    public static String login(String phone, String password) {
        if (!libraryLoaded) {
            return "{\"error\":\"Rust library not loaded\"}";
        }
        try {
            return nativeLogin(phone, password);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    public static String checkUser(String phone) {
        if (!libraryLoaded) {
            return "{\"error\":\"Rust library not loaded\"}";
        }
        try {
            return nativeCheckUser(phone);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    public static String createGroup(String name, String membersJson) {
        if (!libraryLoaded) {
            return "{\"error\":\"Rust library not loaded\"}";
        }
        try {
            return nativeCreateGroup(name, membersJson);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    public static void freeString(String ptr) {
        if (!libraryLoaded) return;
        try {
            nativeFreeString(ptr);
        } catch (Exception e) {
            // Ignore
        }
    }
}
