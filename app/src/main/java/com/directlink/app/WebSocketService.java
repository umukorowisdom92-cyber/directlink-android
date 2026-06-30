package com.directlink.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONObject;

public class WebSocketService extends Service {
    private static final String TAG = "WebSocketService";
    private boolean isConnected = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "WebSocketService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if ("CONNECT".equals(action)) {
            connect();
        } else if ("DISCONNECT".equals(action)) {
            disconnect();
        }
        return START_STICKY;
    }

    private void connect() {
        // Simplified - will implement WebSocket later
        isConnected = true;
        Log.d(TAG, "WebSocket connected");
    }

    private void disconnect() {
        isConnected = false;
        Log.d(TAG, "WebSocket disconnected");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }
}
