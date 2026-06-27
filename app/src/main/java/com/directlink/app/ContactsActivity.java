package com.directlink.app;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends BaseActivity {

    private RecyclerView contactsRecyclerView;
    private ChatAdapter contactsAdapter;
    private List<ChatItem> contactsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        setupBottomNavigation();

        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);

        contactsList.add(new ChatItem("Alice", "Online", "Now", 0, true));
        contactsList.add(new ChatItem("Bob", "Last seen 10min ago", "10min", 0, false));
        contactsList.add(new ChatItem("Charlie", "Online", "Now", 0, true));
        contactsList.add(new ChatItem("Diana", "Last seen 1 hour ago", "1h", 0, false));

        contactsAdapter = new ChatAdapter(contactsList);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(contactsAdapter);
    }
}
