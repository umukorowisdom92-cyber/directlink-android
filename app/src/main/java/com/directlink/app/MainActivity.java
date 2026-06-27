package com.directlink.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText searchInput;
    private EditText serverUrlInput;
    private Button connectButton;
    private TextView statusText;
    private RecyclerView chatsRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatItem> chatList = new ArrayList<>();
    private FloatingActionButton fabAddUser;

    // Bottom navigation
    private LinearLayout navChats, navContacts, navCalls, navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        searchInput = findViewById(R.id.searchInput);
        serverUrlInput = findViewById(R.id.serverUrlInput);
        connectButton = findViewById(R.id.connectButton);
        statusText = findViewById(R.id.statusText);
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        fabAddUser = findViewById(R.id.fabAddUser);

        // Bottom navigation
        navChats = findViewById(R.id.navChats);
        navContacts = findViewById(R.id.navContacts);
        navCalls = findViewById(R.id.navCalls);
        navSettings = findViewById(R.id.navSettings);

        // Setup RecyclerView
        chatAdapter = new ChatAdapter(chatList);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatsRecyclerView.setAdapter(chatAdapter);

        // Load saved preferences
        SharedPreferences prefs = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE);
        String savedUrl = prefs.getString("server_url", "http://10.55.192.27:3030");
        serverUrlInput.setText(savedUrl);

        // Load sample chats
        loadSampleChats();

        // Bottom navigation click listeners
        navChats.setOnClickListener(v -> {
            Toast.makeText(this, "💬 Chats", Toast.LENGTH_SHORT).show();
            highlightNav(navChats);
        });

        navContacts.setOnClickListener(v -> {
            Toast.makeText(this, "👥 Contacts", Toast.LENGTH_SHORT).show();
            highlightNav(navContacts);
        });

        navCalls.setOnClickListener(v -> {
            Toast.makeText(this, "📞 Calls", Toast.LENGTH_SHORT).show();
            highlightNav(navCalls);
        });

        navSettings.setOnClickListener(v -> {
            // Open settings - show logout option
            Toast.makeText(this, "⚙️ Settings - Logout option here", Toast.LENGTH_SHORT).show();
            // You can add a dialog or new activity for settings
            showSettingsDialog();
        });

        // FAB: Add new user
        fabAddUser.setOnClickListener(v -> {
            showAddUserDialog();
        });

        // Connect button
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
                    String result = DirectLinkClient.getContacts();

                    new Handler(Looper.getMainLooper()).post(() -> {
                        statusText.setText("✅ Connected to: " + serverUrl);
                        Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
                        connectButton.setEnabled(true);
                        updateChatsFromContacts(result);
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

        // Search functionality
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Auto-connect
        if (!savedUrl.isEmpty()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!savedUrl.isEmpty()) {
                    connectButton.performClick();
                }
            }, 500);
        }

        // Highlight default nav
        highlightNav(navChats);
    }

    private void highlightNav(LinearLayout selected) {
        // Reset all
        resetNav(navChats);
        resetNav(navContacts);
        resetNav(navCalls);
        resetNav(navSettings);

        // Highlight selected
        TextView label = (TextView) selected.getChildAt(1);
        if (label != null) {
            label.setTextColor(0xFF3F51B5);
            label.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void resetNav(LinearLayout nav) {
        TextView label = (TextView) nav.getChildAt(1);
        if (label != null) {
            label.setTextColor(0xFF666666);
            label.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private void showSettingsDialog() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("⚙️ Settings")
            .setMessage("Logout from your account?")
            .setPositiveButton("Logout", (dialog, which) -> {
                SharedPreferences.Editor editor = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE).edit();
                editor.remove("auth_token");
                editor.remove("username");
                editor.apply();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showAddUserDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("➕ Add New User");

        // Create input fields
        final EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");
        final EditText phoneInput = new EditText(this);
        phoneInput.setHint("Phone Number (e.g., +1234567890)");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(usernameInput);
        layout.addView(phoneInput);
        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String username = usernameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            if (!username.isEmpty() && !phone.isEmpty()) {
                // Send friend request or add contact
                Toast.makeText(this, "Friend request sent to " + username, Toast.LENGTH_SHORT).show();
                chatList.add(new ChatItem(username, "Friend request sent", "Now", 0, false));
                chatAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadSampleChats() {
        chatList.clear();
        chatList.add(new ChatItem("Ken Kingston", "I understand if you can't assist", "Wed", 0, true));
        chatList.add(new ChatItem("Jeff Sirois", "okay", "Mon", 0, true));
        chatList.add(new ChatItem("Raul Loa", "Voice message", "Mon", 0, false));
        chatList.add(new ChatItem("Jeff", "good morning to you too sweetie", "Sun", 1, true));
        chatList.add(new ChatItem("Dazza Lee", "You said when i have it", "12 Jun", 0, false));
        chatList.add(new ChatItem("BUTCH Proper", "hi", "29 May", 0, false));
        chatList.add(new ChatItem("Bruce", "hi", "22 May", 0, false));
        chatList.add(new ChatItem("Conny Albert", "Image message", "01 May", 0, false));
        chatList.add(new ChatItem("David", "you can say what ever you want", "12 Apr", 0, false));
        chatAdapter.notifyDataSetChanged();
    }

    private void updateChatsFromContacts(String contactsJson) {
        try {
            JSONArray array = new JSONArray(contactsJson);
            chatList.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String username = obj.getString("username");
                boolean online = obj.optBoolean("online", false);
                chatList.add(new ChatItem(username, "Online" + (online ? " 🟢" : " ⚪"), "Now", 0, online));
            }
            if (chatList.isEmpty()) {
                loadSampleChats();
            } else {
                chatAdapter.notifyDataSetChanged();
                statusText.setText("✅ Loaded " + chatList.size() + " contacts");
            }
        } catch (Exception e) {
            loadSampleChats();
        }
    }

    private void filterChats(String query) {
        // Simple filter - you can implement proper search later
        if (query.isEmpty()) {
            loadSampleChats();
        } else {
            List<ChatItem> filtered = new ArrayList<>();
            for (ChatItem item : chatList) {
                if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(item);
                }
            }
            chatAdapter = new ChatAdapter(filtered);
            chatsRecyclerView.setAdapter(chatAdapter);
        }
    }
}
