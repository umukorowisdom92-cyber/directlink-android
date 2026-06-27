package com.directlink.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
        statusText.setText("⚪ Status: Ready");

        connectButton.setOnClickListener(v -> {
            String serverUrl = serverUrlInput.getText().toString().trim();
            if (serverUrl.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter server URL", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Save URL
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("server_url", serverUrl);
            editor.apply();

            // Show connecting status immediately
            statusText.setText("⏳ Connecting to: " + serverUrl);
            contactsText.setText("⏳ Checking connection...");
            connectButton.setEnabled(false);

            // Use a thread to avoid blocking UI
            new Thread(() -> {
                try {
                    DirectLinkClient.init(serverUrl);
                    
                    // Try to get contacts to verify connection
                    String result = DirectLinkClient.getContacts();
                    
                    // Update UI on main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        statusText.setText("✅ Connected to: " + serverUrl);
                        contactsText.setText("📋 Contacts: " + result);
                        Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
                        connectButton.setEnabled(true);
                    });
                    
                } catch (Exception e) {
                    // Error - update UI on main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        String errorMsg = e.getMessage();
                        statusText.setText("❌ Connection failed: " + errorMsg);
                        contactsText.setText("❌ Error: " + errorMsg);
                        Toast.makeText(MainActivity.this, "Failed to connect: " + errorMsg, Toast.LENGTH_LONG).show();
                        connectButton.setEnabled(true);
                    });
                }
            }).start();
        });

        // Auto-connect if we have a saved URL
        if (!savedUrl.isEmpty()) {
            // Auto-connect after a short delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!savedUrl.isEmpty()) {
                    connectButton.performClick();
                }
            }, 500);
        }
    }
}
