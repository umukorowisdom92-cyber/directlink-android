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

public class MainActivity extends BaseActivity {

    private EditText searchInput;
    private EditText serverUrlInput;
    private Button connectButton;
    private TextView statusText;
    private RecyclerView chatsRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatItem> chatList = new ArrayList<>();
    private FloatingActionButton fabAddUser;

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

        chatAdapter = new ChatAdapter(chatList);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatsRecyclerView.setAdapter(chatAdapter);

        SharedPreferences prefs = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE);
        String savedUrl = prefs.getString("server_url", "http://10.55.192.27:3030");
        serverUrlInput.setText(savedUrl);

        loadSampleChats();

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

    private void showAddUserDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("➕ Add New User");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        android.widget.Button qrButton = new android.widget.Button(this);
        qrButton.setText("📷 Scan QR Code");
        qrButton.setBackgroundColor(0xFF3F51B5);
        qrButton.setTextColor(0xFFFFFFFF);
        qrButton.setPadding(20, 20, 20, 20);
        qrButton.setOnClickListener(v -> {
            Toast.makeText(this, "📷 QR Code Scanner (Coming soon)", Toast.LENGTH_SHORT).show();
        });
        layout.addView(qrButton);

        android.widget.TextView divider = new android.widget.TextView(this);
        divider.setText("────────── OR ──────────");
        divider.setGravity(android.view.Gravity.CENTER);
        divider.setPadding(0, 20, 0, 20);
        layout.addView(divider);

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
            checkUserAndShowProfile(phone);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void checkUserAndShowProfile(String phone) {
        Toast.makeText(this, "🔍 Checking user...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE);
                String serverUrl = prefs.getString("server_url", "http://10.55.192.27:3030");
                DirectLinkClient.init(serverUrl);
                String result = DirectLinkClient.checkUser(phone);
                JSONObject json = new JSONObject(result);

                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        if (json.has("on_directlink") && json.getBoolean("on_directlink")) {
                            String username = json.getString("username");
                            String phoneNumber = json.getString("phone_number");
                            boolean online = json.optBoolean("online", false);

                            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("phone", phoneNumber);
                            intent.putExtra("online", online);
                            startActivity(intent);
                        } else {
                            new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle("❌ User Not Found")
                                .setMessage("No user found with phone number: " + phone + "\n\nWould you like to invite them to join DirectLink?")
                                .setPositiveButton("Invite", (dialog, which) -> {
                                    Toast.makeText(MainActivity.this, "📤 Invitation sent!", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Cancel", null)
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
