package com.directlink.app;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// Pure Java implementation - no native library needed!
public class DirectLinkClient {
    private static String serverUrl = "http://10.0.0.2:3030";
    private static String authToken = null;
    private static String username = null;
    
    public static void init(String url) {
        serverUrl = url;
        Log.d("DirectLink", "Initialized with server: " + url);
    }
    
    public static String register(String username, String phone, String password) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("phone_number", phone);
            json.put("password", password);
            
            String response = sendPostRequest("/register", json.toString());
            return response;
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    public static String login(String phone, String password) {
        try {
            JSONObject json = new JSONObject();
            json.put("phone_number", phone);
            json.put("password", password);
            
            String response = sendPostRequest("/login", json.toString());
            return response;
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    public static String checkUser(String phone) {
        try {
            String response = sendGetRequest("/check?phone=" + java.net.URLEncoder.encode(phone, "UTF-8"));
            return response;
        } catch (Exception e) {
            return "{\"on_directlink\":false, \"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    public static String getContacts() {
        try {
            String response = sendGetRequest("/contacts");
            return response;
        } catch (Exception e) {
            return "[{\"error\":\"" + e.getMessage() + "\"}]";
        }
    }
    
    public static String createGroup(String name, String membersJson) {
        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("members", new JSONArray(membersJson));
            
            String response = sendPostRequest("/group", json.toString());
            return response;
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    public static void freeString(String ptr) {
        // No-op for Java version
    }
    
    // Helper methods
    private static String sendPostRequest(String path, String jsonBody) throws Exception {
        URL url = new URL(serverUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        
        if (authToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }
        
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(jsonBody.getBytes());
        os.flush();
        os.close();
        
        int responseCode = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream())
        );
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        // If login, extract token
        if (path.equals("/login")) {
            try {
                JSONObject obj = new JSONObject(response.toString());
                if (obj.has("token")) {
                    authToken = obj.getString("token");
                    username = obj.getString("username");
                }
            } catch (Exception e) {}
        }
        
        return response.toString();
    }
    
    private static String sendGetRequest(String path) throws Exception {
        URL url = new URL(serverUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        
        if (authToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }
        
        int responseCode = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream())
        );
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return response.toString();
    }
}
