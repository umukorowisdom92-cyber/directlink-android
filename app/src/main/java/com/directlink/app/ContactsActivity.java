package com.directlink.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends BaseActivity {

    private RecyclerView contactsRecyclerView;
    private ChatAdapter contactsAdapter;
    private List<ChatItem> contactsList = new ArrayList<>();
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        setupBottomNavigation();

        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        emptyText = findViewById(R.id.emptyText);

        // Custom adapter for contacts with click listener
        contactsAdapter = new ChatAdapter(contactsList, new ChatAdapter.OnFriendRequestListener() {
            @Override
            public void onAccept(String requestId, String name, String phone) {}
            @Override
            public void onReject(String requestId) {}
            @Override
            public void onChatClick(String name, String phone) {
                // In contacts, click opens profile
                Intent intent = new Intent(ContactsActivity.this, UserProfileActivity.class);
                intent.putExtra("username", name);
                intent.putExtra("phone", phone);
                intent.putExtra("online", true);
                startActivity(intent);
            }
        });

        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(contactsAdapter);

        loadContacts();
    }

    private void loadContacts() {
        new Thread(() -> {
            try {
                String result = DirectLinkClient.getContacts();
                JSONArray contacts = new JSONArray(result);

                new Handler(Looper.getMainLooper()).post(() -> {
                    contactsList.clear();
                    try {
                        for (int i = 0; i < contacts.length(); i++) {
                            JSONObject obj = contacts.getJSONObject(i);
                            String username = obj.getString("username");
                            String phone = obj.getString("phone_number");
                            boolean online = obj.optBoolean("online", false);
                            contactsList.add(new ChatItem(username, phone, "Contact", "Now", 0, online));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (contactsList.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        contactsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                        contactsRecyclerView.setVisibility(View.VISIBLE);
                        contactsAdapter.notifyDataSetChanged();
                    }
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Error loading contacts", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
