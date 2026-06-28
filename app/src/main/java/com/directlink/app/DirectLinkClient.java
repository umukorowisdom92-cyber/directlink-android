package com.directlink.app;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DirectLinkClient {
    // CLOUDFLARE TUNNEL URL - WORKING!
    private static String serverUrl = "https://plains-fun-alice-educational.trycloudflare.com";
    private static String authToken = null;
    private static String username = null;

    public static void init(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Server URL cannot be empty");
        }
        url = url.trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        serverUrl = url;
        Log.d("DirectLink", "Server set to: " + serverUrl);
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getContacts() throws Exception {
        return sendGetRequest("/contacts");
    }

    public static String getFriendRequests() throws Exception {
        return sendGetRequest("/friend_requests");
    }

    public static String sendFriendRequest(String toUsername) throws Exception {
        JSONObject json = new JSONObject();
        json.put("to_username", toUsername);
        return sendPostRequest("/friend_request", json.toString());
    }

    public static String acceptFriendRequest(String requestId) throws Exception {
        JSONObject json = new JSONObject();
        json.put("request_id", requestId);
        json.put("action", "accept");
        return sendPostRequest("/friend_request/respond", json.toString());
    }

    public static String rejectFriendRequest(String requestId) throws Exception {
        JSONObject json = new JSONObject();
        json.put("request_id", requestId);
        json.put("action", "reject");
        return sendPostRequest("/friend_request/respond", json.toString());
    }

    public static String register(String username, String phone, String password) throws Exception {
        username = username.trim();
        phone = phone.trim();
        password = password.trim();

        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("phone_number", phone);
        json.put("password", password);
        return sendPostRequest("/register", json.toString());
    }

    public static String login(String phone, String password) throws Exception {
        phone = phone.trim();
        password = password.trim();

        JSONObject json = new JSONObject();
        json.put("phone_number", phone);
        json.put("password", password);
        String response = sendPostRequest("/login", json.toString());

        try {
            JSONObject obj = new JSONObject(response);
            if (obj.has("token")) {
                authToken = obj.getString("token");
                username = obj.getString("username");
                Log.d("DirectLink", "Auth token set: " + authToken);
            }
        } catch (Exception e) {}

        return response;
    }

    public static String checkUser(String phone) throws Exception {
        phone = phone.trim();
        return sendGetRequest("/check?phone=" + java.net.URLEncoder.encode(phone, "UTF-8"));
    }

    public static void freeString(String ptr) {}

    private static String sendPostRequest(String path, String jsonBody) throws Exception {
        if (serverUrl == null || serverUrl.isEmpty()) {
            throw new IllegalStateException("Server URL not set. Call init() first.");
        }

        URL url = new URL(serverUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        if (authToken != null && !authToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }

        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(jsonBody.getBytes("UTF-8"));
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(conn.getErrorStream())
            );
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            conn.disconnect();
            throw new Exception("Server error " + responseCode + ": " + errorResponse.toString());
        }

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream())
        );
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        return response.toString();
    }

    private static String sendGetRequest(String path) throws Exception {
        if (serverUrl == null || serverUrl.isEmpty()) {
            throw new IllegalStateException("Server URL not set. Call init() first.");
        }

        URL url = new URL(serverUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        if (authToken != null && !authToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(conn.getErrorStream())
            );
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            conn.disconnect();
            throw new Exception("Server error " + responseCode + ": " + errorResponse.toString());
        }

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream())
        );
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        return response.toString();
    }
}
