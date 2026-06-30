package com.directlink.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements ChatAdapter.OnItemClickListener {

    private RecyclerView chatsRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatItem> chatList = new ArrayList<>();
    private TextView statusText, userNameDisplay;
    private TextView chatBadge;
    private Button logoutButton;
    private FloatingActionButton fabAddUser;
    private int totalUnreadCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupBottomNavigation();

        if (!ConnectionManager.getInstance().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        statusText = findViewById(R.id.statusText);
        userNameDisplay = findViewById(R.id.userNameDisplay);
        logoutButton = findViewById(R.id.logoutButton);
        fabAddUser = findViewById(R.id.fabAddUser);
        chatBadge = findViewById(R.id.chatBadge);

        String username = ConnectionManager.getInstance().getUsername();
        userNameDisplay.setText("👤 " + username);

        chatAdapter = new ChatAdapter(chatList, this);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatsRecyclerView.setAdapter(chatAdapter);

        logoutButton.setOnClickListener(v -> {
            ConnectionManager.getInstance().logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        fabAddUser.setOnClickListener(v -> showAddUserDialog());

        loadData();
    }

    private void loadData() {
        statusText.setText("⏳ Loading...");

        new Thread(() -> {
            try {
                List<Contact> contacts = ConnectionManager.getInstance().getContacts();
                JSONArray requests = ConnectionManager.getInstance().getFriendRequests();
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    updateChatList(contacts, requests);
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    statusText.setText("❌ Error: " + e.getMessage());
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateChatList(List<Contact> contacts, JSONArray requests) {
        chatList.clear();
        totalUnreadCount = 0;

        // Friend requests
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

        // Contacts with unread badges
        for (Contact contact : contacts) {
            int unread = (int) (Math.random() * 3);
            if (unread > 0) {
                totalUnreadCount += unread;
            }
            chatList.add(new ChatItem(
                contact.getUsername(),
                contact.getPhoneNumber(),
                "Tap to chat",
                "Now",
                unread,
                contact.isOnline()
            ));
        }

        chatAdapter.notifyDataSetChanged();
        updateBottomBadge();
        statusText.setText("📋 " + chatList.size() + " items");
    }

    private void updateBottomBadge() {
        if (totalUnreadCount > 0) {
            chatBadge.setVisibility(View.VISIBLE);
            chatBadge.setText(String.valueOf(totalUnreadCount));
        } else {
            chatBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onChatClick(String name, String phone) {
        Toast.makeText(this, "💬 Opening chat with " + name, Toast.LENGTH_SHORT).show();
        // Open ChatActivity
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("username", name);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    @Override
    public void onFriendRequestAccept(String requestId, String name, String phone) {
        new Thread(() -> {
            try {
                ConnectionManager.getInstance().acceptFriendRequest(requestId);
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
    public void onFriendRequestReject(String requestId) {
        new Thread(() -> {
            try {
                ConnectionManager.getInstance().rejectFriendRequest(requestId);
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

    private void showAddUserDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("➕ Add New User");

        final android.widget.EditText phoneInput = new android.widget.EditText(this);
        phoneInput.setHint("📞 Enter phone number");
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        phoneInput.setPadding(40, 20, 40, 20);

        builder.setView(phoneInput);
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
                JSONObject result = ConnectionManager.getInstance().checkUser(phone);
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        if (result.has("on_directlink") && result.getBoolean("on_directlink")) {
                            String username = result.getString("username");
                            sendFriendRequest(username);
                        } else {
                            Toast.makeText(this, "❌ User not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void sendFriendRequest(String username) {
        new Thread(() -> {
            try {
                ConnectionManager.getInstance().sendFriendRequest(username);
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
}
