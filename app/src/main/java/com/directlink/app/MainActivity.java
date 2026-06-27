package com.directlink.app;

import android.content.Intent;
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
    private Button connectButton, logoutButton;
    private TextView statusText, contactsText, userNameDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverUrlInput = findViewById(R.id.serverUrlInput);
        connectButton = findViewById(R.id.connectButton);
        logoutButton = findViewById(R.id.logoutButton);
        statusText = findViewById(R.id.statusText);
        contactsText = findViewById(R.id.contactsText);
        userNameDisplay = findViewById(R.id.userNameDisplay);

        SharedPreferences prefs = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE);
        String savedUrl = prefs.getString("server_url", "http://10.55.192.27:3030");
        String username = prefs.getString("username", "User");

        serverUrlInput.setText(savedUrl);
        userNameDisplay.setText("👤 " + username);
        statusText.setText("⚪ Status: Ready");

        // Save the server URL immediately so other activities use it
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("server_url", savedUrl);
        editor.apply();

        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("auth_token");
            edit.remove("username");
            edit.apply();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        connectButton.setOnClickListener(v -> {
            String serverUrl = serverUrlInput.getText().toString().trim();
            if (serverUrl.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter server URL", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("server_url", serverUrl);
            edit.apply();

            statusText.setText("⏳ Connecting...");
            connectButton.setEnabled(false);

            new Thread(() -> {
                try {
                    DirectLinkClient.init(serverUrl);
                    String result = DirectLinkClient.getContacts();

                    new Handler(Looper.getMainLooper()).post(() -> {
                        statusText.setText("✅ Connected to: " + serverUrl);
                        contactsText.setText("📋 Contacts: " + result);
                        Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
                        connectButton.setEnabled(true);
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

        if (!savedUrl.isEmpty()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!savedUrl.isEmpty()) {
                    connectButton.performClick();
                }
            }, 500);
        }
    }
}
