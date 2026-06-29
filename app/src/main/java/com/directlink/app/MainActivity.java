package com.directlink.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements ChatAdapter.OnFriendRequestListener {

    private EditText searchInput;
    private EditText serverUrlInput;
    private Button connectButton;
    private TextView statusText;
    private RecyclerView chatsRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatItem> chatList = new ArrayList<>();
    private FloatingActionButton fabAddUser;
    private String authToken;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupBottomNavigation();

        searchInput = findViewById(R.id.searchInput);
        serverUrlInput = findViewById(R.id.serverUrlInput);
        connectButton = findViewById(R.id.connectButton);
        statusText = findViewById(R.id.statusText);
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        fabAddUser = findViewById(R.id.fabAddUser);

        SharedPreferences prefs = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE);
        authToken = prefs.getString("auth_token", "");
        currentUsername = prefs.getString("username", "");
        String savedUrl = prefs.getString("server_url", "https://construct-blend-instant-alfred.trycloudflare.com");
        serverUrlInput.setText(savedUrl);

        DirectLinkClient.setAuthToken(authToken);
        DirectLinkClient.setUsername(currentUsername);

        chatAdapter = new ChatAdapter(chatList, this);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatsRecyclerView.setAdapter(chatAdapter);

        fabAddUser.setOnClickListener(v -> showAddUserDialog());

        connectButton.setOnClickListener(v -> {
            String serverUrl = serverUrlInput.getText().toString().trim();
            if (serverUrl.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter server URL", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("server_url", serverUrl);
            editor.apply();

            statusText.setText("⏳ Connecting...");
            connectButton.setEnabled(false);

            new Thread(() -> {
                try {
                    DirectLinkClient.init(serverUrl);
                    DirectLinkClient.setAuthToken(authToken);
                    DirectLinkClient.setUsername(currentUsername);
                    loadData();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        statusText.setText("✅ Connected to: " + serverUrl);
                        Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
                        connectButton.setEnabled(true);
                    });
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        statusText.setText("❌ Error: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        connectButton.setEnabled(true);
                    });
                }
            }).start();
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        if (!savedUrl.isEmpty()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!savedUrl.isEmpty()) {
                    connectButton.performClick();
                }
            }, 500);
        }
    }

    private void loadData() {
        new Thread(() -> {
            try {
                String contactsResult = DirectLinkClient.getContacts();
                JSONArray contacts = new JSONArray(contactsResult);

                String requestsResult = DirectLinkClient.getFriendRequests();
                JSONArray requests = new JSONArray(requestsResult);

                new Handler(Looper.getMainLooper()).post(() -> {
                    updateChatList(contacts, requests);
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    statusText.setText("❌ Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void updateChatList(JSONArray contacts, JSONArray requests) {
        chatList.clear();

        try {
            for (int i = 0; i < requests.length(); i++) {
                JSONObject req = requests.getJSONObject(i);
                String from = req.getString("from_username");
                String phone = req.getString("from_phone");
                String requestId = req.getString("id");
                chatList.add(new ChatItem(from, phone, requestId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < contacts.length(); i++) {
                JSONObject contact = contacts.getJSONObject(i);
                String username = contact.getString("username");
                String phone = contact.getString("phone_number");
                boolean online = contact.optBoolean("online", false);
                chatList.add(new ChatItem(username, phone, "Tap to chat", "Now", 0, online));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        chatAdapter.notifyDataSetChanged();
        statusText.setText("📋 " + chatList.size() + " items");
    }

    @Override
    public void onAccept(String requestId, String name, String phone) {
        DirectLinkClient.setUsername(currentUsername);
        Toast.makeText(this, "Accepting friend request...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                DirectLinkClient.setAuthToken(authToken);
                String result = DirectLinkClient.acceptFriendRequest(requestId);
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Friend request accepted!", Toast.LENGTH_SHORT).show();
                    loadData();
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    public void onReject(String requestId, String name, String phone) {
        DirectLinkClient.setUsername(currentUsername);
        Toast.makeText(this, "Rejecting friend request...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                DirectLinkClient.setAuthToken(authToken);
                String result = DirectLinkClient.rejectFriendRequest(requestId);
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Friend request rejected", Toast.LENGTH_SHORT).show();
                    loadData();
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    public void onChatClick(String name, String phone) {
        // Clear the badge count for this user
        for (ChatItem item : chatList) {
            if (item.getName().equals(name)) {
                item.setBadgeCount(0);
                chatAdapter.notifyDataSetChanged();
                break;
            }
        }

        // Open chat activity
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra("username", name);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    private void showAddUserDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("➕ Add New User");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText phoneInput = new EditText(this);
        phoneInput.setHint("📞 Enter phone number");
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        phoneInput.setPadding(20, 20, 20, 20);
        layout.addView(phoneInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String phone = phoneInput.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
                return;
            }
            checkUserAndAdd(phone);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void checkUserAndAdd(String phone) {
        Toast.makeText(this, "🔍 Checking user...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE);
                String serverUrl = prefs.getString("server_url", "https://construct-blend-instant-alfred.trycloudflare.com");
                DirectLinkClient.init(serverUrl);
                DirectLinkClient.setAuthToken(authToken);
                DirectLinkClient.setUsername(currentUsername);

                String result = DirectLinkClient.checkUser(phone);
                JSONObject json = new JSONObject(result);

                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        if (json.has("on_directlink") && json.getBoolean("on_directlink")) {
                            String username = json.getString("username");
                            String phoneNumber = json.getString("phone_number");
                            sendFriendRequest(username, phoneNumber);
                        } else {
                            new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle("❌ User Not Found")
                                .setMessage("No user found with phone number: " + phone)
                                .setPositiveButton("OK", null)
                                .show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void sendFriendRequest(String username, String phone) {
        new Thread(() -> {
            try {
                DirectLinkClient.setAuthToken(authToken);
                DirectLinkClient.setUsername(currentUsername);
                String result = DirectLinkClient.sendFriendRequest(username);
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Friend request sent to " + username + "!", Toast.LENGTH_SHORT).show();
                    loadData();
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void filterChats(String query) {
        // TODO: Implement proper search
    }
}
