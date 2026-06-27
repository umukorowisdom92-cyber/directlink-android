package com.directlink.app;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText serverUrlInput;
    private Button connectButton;
    private TextView statusText;
    private RecyclerView contactsRecyclerView;
    private ContactsAdapter adapter;
    private List<Contact> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        serverUrlInput = findViewById(R.id.serverUrlInput);
        connectButton = findViewById(R.id.connectButton);
        statusText = findViewById(R.id.statusText);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);

        // Setup RecyclerView
        adapter = new ContactsAdapter(contacts);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(adapter);

        // Set default server URL
        serverUrlInput.setText("http://10.0.0.2:3030");

        // Connect button click
        connectButton.setOnClickListener(v -> {
            String serverUrl = serverUrlInput.getText().toString();
            if (serverUrl.isEmpty()) {
                Toast.makeText(this, "Please enter server URL", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Initialize the Rust client
            DirectLinkClient.init(serverUrl);
            statusText.setText("Connected to: " + serverUrl);
            Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();
            
            // Load contacts
            loadContacts();
        });
    }

    private void loadContacts() {
        String result = DirectLinkClient.getContacts();
        try {
            org.json.JSONArray array = new org.json.JSONArray(result);
            contacts.clear();
            for (int i = 0; i < array.length(); i++) {
                org.json.JSONObject obj = array.getJSONObject(i);
                contacts.add(new Contact(
                    obj.getString("username"),
                    obj.getString("phone_number"),
                    obj.optBoolean("online", false)
                ));
            }
            adapter.notifyDataSetChanged();
            statusText.setText("Contacts loaded: " + contacts.size() + " found");
        } catch (Exception e) {
            statusText.setText("Error loading contacts: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
