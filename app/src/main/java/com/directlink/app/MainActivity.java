package com.directlink.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText serverUrlInput;
    private Button connectButton;
    private TextView statusText;
    private TextView contactsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverUrlInput = findViewById(R.id.serverUrlInput);
        connectButton = findViewById(R.id.connectButton);
        statusText = findViewById(R.id.statusText);
        contactsText = findViewById(R.id.contactsText);

        // Load saved server URL
        SharedPreferences prefs = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE);
        String savedUrl = prefs.getString("server_url", "http://10.0.0.2:3030");
        serverUrlInput.setText(savedUrl);
        statusText.setText("Status: Ready");

        connectButton.setOnClickListener(v -> {
            String serverUrl = serverUrlInput.getText().toString();
            if (serverUrl.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter server URL", Toast.LENGTH_SHORT).show();
                return;
            }
            
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("server_url", serverUrl);
            editor.apply();

            connectToServer(serverUrl);
        });

        // Auto-connect if we have a saved URL
        if (!savedUrl.isEmpty()) {
            connectToServer(savedUrl);
        }
    }

    private void connectToServer(String serverUrl) {
        try {
            statusText.setText("⏳ Connecting to: " + serverUrl);
            
            DirectLinkClient.init(serverUrl);
            
            statusText.setText("✅ Connected to: " + serverUrl);
            Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();

            String result = DirectLinkClient.getContacts();
            contactsText.setText("📋 Contacts: " + result);
            
        } catch (Exception e) {
            statusText.setText("❌ Error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
