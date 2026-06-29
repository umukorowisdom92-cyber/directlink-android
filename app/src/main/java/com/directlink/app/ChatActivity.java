package com.directlink.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private Button sendButton;
    private LinearLayout messagesContainer;
    private ScrollView scrollView;
    private TextView statusText;

    private WebSocket webSocket;
    private String currentUsername;
    private String chatPartner;
    private String serverUrl;
    private MessageDatabase messageDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatPartner = getIntent().getStringExtra("username");
        currentUsername = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE)
                .getString("username", "");
        serverUrl = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE)
                .getString("server_url", "https://construct-blend-instant-alfred.trycloudflare.com");

        if (chatPartner == null || chatPartner.isEmpty()) {
            Toast.makeText(this, "Error: No chat partner", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle("Chat with " + chatPartner);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagesContainer = findViewById(R.id.messagesContainer);
        scrollView = findViewById(R.id.scrollView);
        statusText = findViewById(R.id.statusText);

        // Initialize database
        messageDatabase = new MessageDatabase(this);

        // Load existing messages
        loadMessagesFromDatabase();

        // Clear unread count for this chat partner when opening chat
        NotificationManager.getInstance().clearUnread(chatPartner);

        // Connect to WebSocket
        connectWebSocket();

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });

        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                return true;
            }
            return false;
        });

        addSystemMessage("Started chatting with " + chatPartner);
    }

    private void loadMessagesFromDatabase() {
        List<MessageDatabase.MessageItem> messages = messageDatabase.getMessages(currentUsername, chatPartner);
        for (MessageDatabase.MessageItem msg : messages) {
            displayMessage(msg.sender, msg.message, msg.timestamp);
        }
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void connectWebSocket() {
        String wsUrl = serverUrl.replace("https://", "wss://").replace("http://", "ws://");
        // Remove trailing slash if exists
        if (wsUrl.endsWith("/")) {
            wsUrl = wsUrl.substring(0, wsUrl.length() - 1);
        }
        String fullUrl = wsUrl + "/ws?username=" + currentUsername;

        statusText.setText("Connecting...");
        statusText.setTextColor(getColor(android.R.color.holo_orange_dark));

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(fullUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                runOnUiThread(() -> {
                    statusText.setText("Connected ✅");
                    statusText.setTextColor(getColor(android.R.color.holo_green_dark));
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(text);
                        String type = json.getString("type");

                        if ("private_message".equals(type)) {
                            String from = json.getString("from");
                            String content = json.getString("content");
                            String timestamp = json.optString("timestamp", getCurrentTimestamp());

                            // Save to database
                            if (currentUsername != null && chatPartner != null) {
                                messageDatabase.saveMessage(currentUsername, chatPartner, from, content, timestamp);
                            }

                            // Display the message in chat
                            displayMessage(from, content, timestamp);

                            // If message is from chat partner, clear badge
                            if (from.equals(chatPartner)) {
                                NotificationManager.getInstance().clearUnread(chatPartner);
                            }
                        } else if ("online_status".equals(type)) {
                            String username = json.getString("username");
                            boolean online = json.getBoolean("online");
                            if (username.equals(chatPartner)) {
                                statusText.setText(online ? "🟢 Online" : "⚪ Offline");
                                statusText.setTextColor(online ?
                                        getColor(android.R.color.holo_green_dark) :
                                        getColor(android.R.color.darker_gray));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                runOnUiThread(() -> {
                    statusText.setText("Disconnected");
                    statusText.setTextColor(getColor(android.R.color.holo_red_dark));
                });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                runOnUiThread(() -> {
                    statusText.setText("Error: " + t.getMessage());
                    statusText.setTextColor(getColor(android.R.color.holo_red_dark));
                    Toast.makeText(ChatActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void sendMessage(String message) {
        if (webSocket == null) {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String timestamp = getCurrentTimestamp();
            JSONObject json = new JSONObject();
            json.put("type", "private_message");
            json.put("from", currentUsername);
            json.put("to", chatPartner);
            json.put("content", message);
            json.put("timestamp", timestamp);

            webSocket.send(json.toString());

            // Save to database
            messageDatabase.saveMessage(currentUsername, chatPartner, currentUsername, message, timestamp);

            messageInput.setText("");
            displayMessage("Me", message, "now");

            // Update the main chat list
            if (MainActivity.instance != null) {
                MainActivity.instance.updateChatListOnNewMessage(chatPartner, message, timestamp);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void displayMessage(String sender, String message, String timestamp) {
        TextView messageView = new TextView(this);
        String displayText = message;
        if (!timestamp.isEmpty() && !timestamp.equals("now")) {
            displayText = displayText + "\n" + timestamp;
        }
        messageView.setText(displayText);
        messageView.setPadding(16, 12, 16, 12);
        messageView.setTextSize(16);
        messageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        boolean isMe = sender.equals(currentUsername) || sender.equals("Me");
        if (isMe) {
            messageView.setBackgroundColor(getColor(android.R.color.holo_blue_light));
            messageView.setTextColor(getColor(android.R.color.white));
            messageView.setGravity(android.view.Gravity.END);
        } else {
            messageView.setBackgroundColor(getColor(android.R.color.darker_gray));
            messageView.setTextColor(getColor(android.R.color.black));
            messageView.setGravity(android.view.Gravity.START);
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) messageView.getLayoutParams();
        params.setMargins(0, 4, 0, 4);
        messageView.setLayoutParams(params);

        messagesContainer.addView(messageView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void addSystemMessage(String message) {
        TextView systemView = new TextView(this);
        systemView.setText("⚡ " + message);
        systemView.setGravity(android.view.Gravity.CENTER);
        systemView.setTextSize(12);
        systemView.setTextColor(getColor(android.R.color.darker_gray));
        systemView.setPadding(8, 4, 8, 4);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        systemView.setLayoutParams(params);

        messagesContainer.addView(systemView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity closing");
        }
    }
}
