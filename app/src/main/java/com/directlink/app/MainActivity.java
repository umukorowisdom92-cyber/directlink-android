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

        // Auto-connect
        if (!savedUrl.isEmpty()) {
            connectToServer(savedUrl);
        }

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
    }

    private void connectToServer(String serverUrl) {
        try {
            statusText.setText("⏳ Connecting to: " + serverUrl);
            
            // Try to initialize
            DirectLinkClient.init(serverUrl);
            statusText.setText("✅ Connected to: " + serverUrl);
            Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();

            // Try to get contacts (with try-catch to prevent crash)
            try {
                String result = DirectLinkClient.getContacts();
                contactsText.setText("📋 Contacts: " + result);
            } catch (Exception e) {
                contactsText.setText("❌ Contacts error: " + e.getMessage());
            }
            
        } catch (Exception e) {
            statusText.setText("❌ Error: " + e.getMessage());
            Toast.makeText(this, "Connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
