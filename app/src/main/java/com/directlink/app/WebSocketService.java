package com.directlink.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class WebSocketService extends Service {
    private WebSocket webSocket;
    private String currentUsername;
    private String serverUrl;
    private boolean isConnected = false;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("username", "");
        serverUrl = prefs.getString("server_url", "https://founder-sector-palestinian-date.trycloudflare.com");
        connectWebSocket();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void connectWebSocket() {
        if (currentUsername == null || currentUsername.isEmpty()) {
            return;
        }

        String wsUrl = serverUrl.replace("https://", "wss://").replace("http://", "ws://");
        if (wsUrl.endsWith("/")) {
            wsUrl = wsUrl.substring(0, wsUrl.length() - 1);
        }
        String fullUrl = wsUrl + "/ws?username=" + currentUsername;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(fullUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                isConnected = true;
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject json = new JSONObject(text);
                    String type = json.getString("type");

                    if ("private_message".equals(type)) {
                        String from = json.getString("from");
                        String content = json.getString("content");
                        String timestamp = json.optString("timestamp", "");

                        // Show notification
                        showNotification(from, content);

                        // Update NotificationManager using static methods
                        com.directlink.app.NotificationManager.onMessageReceived(from, content, timestamp);
                        com.directlink.app.NotificationManager.refreshChatList();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                isConnected = false;
                // Reconnect after delay
                new android.os.Handler().postDelayed(() -> connectWebSocket(), 5000);
            }
        });
    }

    private void showNotification(String from, String message) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "directlink_channel",
                    "DirectLink Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "directlink_channel")
                .setSmallIcon(android.R.drawable.ic_menu_agenda)
                .setContentTitle("📩 " + from)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify(from.hashCode(), builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Service stopping");
        }
    }
}
