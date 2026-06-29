package com.directlink.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketService extends Service {

    private okhttp3.WebSocket webSocket;
    private String currentUsername;
    private String serverUrl;
    private static final String CHANNEL_ID = "DirectLinkChannel";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        startForeground(NOTIFICATION_ID, getNotification("Connecting..."));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("username", "");
        serverUrl = prefs.getString("server_url", "https://construct-blend-instant-alfred.trycloudflare.com");

        if (!currentUsername.isEmpty()) {
            connectWebSocket();
        }

        return START_STICKY;
    }

    private void connectWebSocket() {
        String wsUrl = serverUrl.replace("https://", "wss://").replace("http://", "ws://");
        String fullUrl = wsUrl + "/ws?username=" + currentUsername;

        updateNotification("Connecting...");

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(fullUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                updateNotification("Connected ✅");
                Log.d("WebSocketService", "Connected as: " + currentUsername);
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

                        updateNotification("📨 Message from " + from + ": " + content);

                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(WebSocketService.this, "📨 " + from + ": " + content, Toast.LENGTH_SHORT).show();
                        });

                        MessageDatabase db = new MessageDatabase(WebSocketService.this);
                        db.saveMessage(currentUsername, from, from, content, timestamp);

                        com.directlink.app.NotificationManager.getInstance().onMessageReceived(from, content, timestamp);

                        Intent broadcastIntent = new Intent("NEW_MESSAGE");
                        broadcastIntent.putExtra("from", from);
                        broadcastIntent.putExtra("content", content);
                        broadcastIntent.putExtra("timestamp", timestamp);
                        sendBroadcast(broadcastIntent);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                updateNotification("Disconnected");
                Log.d("WebSocketService", "Disconnected");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                updateNotification("Error: " + t.getMessage());
                Log.e("WebSocketService", "Error: " + t.getMessage());

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!currentUsername.isEmpty()) {
                        connectWebSocket();
                    }
                }, 5000);
            }
        });
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "DirectLink Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private android.app.Notification getNotification(String text) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("🚀 DirectLink")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_agenda)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        return builder.build();
    }

    private void updateNotification(String text) {
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, getNotification(text));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Service stopping");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
