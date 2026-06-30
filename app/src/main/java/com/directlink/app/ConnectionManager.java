package com.directlink.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {
    private static ConnectionManager instance;
    private static final String TAG = "DirectLink";
    
    public static final String SERVER_URL = "https://founder-sector-palestinian-date.trycloudflare.com";
    
    private String authToken = null;
    private String currentUsername = null;
    private Context context;
    
    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }
    
    private ConnectionManager() {}
    
    public void init(Context context) {
        this.context = context.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("DirectLinkPrefs", Context.MODE_PRIVATE);
        authToken = prefs.getString("auth_token", null);
        currentUsername = prefs.getString("username", null);
    }
    
    public boolean isLoggedIn() {
        return authToken != null && !authToken.isEmpty();
    }
    
    public String getUsername() {
        return currentUsername;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    // ============================================================
    // AUTHENTICATION
    // ============================================================
    
    public JSONObject register(String username, String phone, String password) throws Exception {
        JSONObject json = new JSONObject();
        json.put("username", username.trim());
        json.put("phone_number", phone.trim());
        json.put("password", password.trim());
        
        String response = sendPostRequest("/register", json.toString(), null);
        JSONObject result = new JSONObject(response);
        
        if (result.has("token")) {
            authToken = result.getString("token");
            currentUsername = username.trim();
            saveAuth();
        }
        return result;
    }
    
    public JSONObject login(String phone, String password) throws Exception {
        JSONObject json = new JSONObject();
        json.put("phone_number", phone.trim());
        json.put("password", password.trim());
        
        String response = sendPostRequest("/login", json.toString(), null);
        JSONObject result = new JSONObject(response);
        
        if (result.has("token")) {
            authToken = result.getString("token");
            currentUsername = result.getString("username");
            saveAuth();
        }
        return result;
    }
    
    public void logout() {
        authToken = null;
        currentUsername = null;
        clearAuth();
    }
    
    private void saveAuth() {
        if (context != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences("DirectLinkPrefs", Context.MODE_PRIVATE).edit();
            editor.putString("auth_token", authToken);
            editor.putString("username", currentUsername);
            editor.apply();
        }
    }
    
    private void clearAuth() {
        if (context != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences("DirectLinkPrefs", Context.MODE_PRIVATE).edit();
            editor.remove("auth_token");
            editor.remove("username");
            editor.apply();
        }
    }
    
    // ============================================================
    // CONTACTS
    // ============================================================
    
    public List<Contact> getContacts() throws Exception {
        String response = sendGetRequest("/contacts", authToken);
        JSONArray array = new JSONArray(response);
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Contact contact = new Contact(
                obj.getString("username"),
                obj.getString("phone_number"),
                obj.optBoolean("online", false)
            );
            contacts.add(contact);
        }
        return contacts;
    }
    
    // ============================================================
    // FRIEND REQUESTS
    // ============================================================
    
    public JSONArray getFriendRequests() throws Exception {
        String response = sendGetRequest("/friend_requests", authToken);
        return new JSONArray(response);
    }
    
    public JSONObject sendFriendRequest(String toUsername) throws Exception {
        JSONObject json = new JSONObject();
        json.put("to_username", toUsername);
        String response = sendPostRequest("/friend_request", json.toString(), authToken);
        return new JSONObject(response);
    }
    
    public JSONObject acceptFriendRequest(String requestId) throws Exception {
        JSONObject json = new JSONObject();
        json.put("request_id", requestId);
        json.put("action", "accept");
        String response = sendPostRequest("/friend_request/respond", json.toString(), authToken);
        return new JSONObject(response);
    }
    
    public JSONObject rejectFriendRequest(String requestId) throws Exception {
        JSONObject json = new JSONObject();
        json.put("request_id", requestId);
        json.put("action", "reject");
        String response = sendPostRequest("/friend_request/respond", json.toString(), authToken);
        return new JSONObject(response);
    }
    
    public JSONObject checkUser(String phone) throws Exception {
        String response = sendGetRequest("/check?phone=" + java.net.URLEncoder.encode(phone, "UTF-8"), null);
        return new JSONObject(response);
    }
    
    // ============================================================
    // NETWORK HELPERS
    // ============================================================
    
    private String sendPostRequest(String path, String jsonBody, String token) throws Exception {
        URL url = new URL(SERVER_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(jsonBody.getBytes("UTF-8"));
        os.flush();
        os.close();
        
        int responseCode = conn.getResponseCode();
        BufferedReader reader;
        if (responseCode >= 400) {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();
        
        if (responseCode >= 400) {
            throw new Exception("Server error " + responseCode + ": " + response.toString());
        }
        return response.toString();
    }
    
    private String sendGetRequest(String path, String token) throws Exception {
        URL url = new URL(SERVER_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        
        int responseCode = conn.getResponseCode();
        BufferedReader reader;
        if (responseCode >= 400) {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();
        
        if (responseCode >= 400) {
            throw new Exception("Server error " + responseCode + ": " + response.toString());
        }
        return response.toString();
    }
}
