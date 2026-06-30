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

        contactsAdapter = new ChatAdapter(contactsList, new ChatAdapter.OnItemClickListener() {
            @Override
            public void onChatClick(String name, String phone) {
                Intent intent = new Intent(ContactsActivity.this, UserProfileActivity.class);
                intent.putExtra("username", name);
                intent.putExtra("phone", phone);
                startActivity(intent);
            }

            @Override
            public void onFriendRequestAccept(String requestId, String name, String phone) {}

            @Override
            public void onFriendRequestReject(String requestId) {}
        });

        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(contactsAdapter);

        loadContacts();
    }

    private void loadContacts() {
        new Thread(() -> {
            try {
                List<Contact> contacts = ConnectionManager.getInstance().getContacts();
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    contactsList.clear();
                    for (Contact contact : contacts) {
                        contactsList.add(new ChatItem(
                            contact.getUsername(),
                            contact.getPhoneNumber(),
                            "Contact",
                            "Now",
                            0,
                            contact.isOnline()
                        ));
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
                    Toast.makeText(this, "Error loading contacts: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
