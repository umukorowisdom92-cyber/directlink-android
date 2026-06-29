package com.directlink.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private TextView serverUrlText, versionText, usernameText;
    private Button logoutButton, clearDataButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        serverUrlText = findViewById(R.id.serverUrlText);
        versionText = findViewById(R.id.versionText);
        usernameText = findViewById(R.id.usernameText);
        logoutButton = findViewById(R.id.logoutButton);
        clearDataButton = findViewById(R.id.clearDataButton);
        backButton = findViewById(R.id.backButton);

        SharedPreferences prefs = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        String serverUrl = prefs.getString("server_url", "https://construct-blend-instant-alfred.trycloudflare.com");

        usernameText.setText("👤 " + username);
        serverUrlText.setText("🌐 Server: " + serverUrl);
        versionText.setText("📱 Version: 1.0.1");

        backButton.setOnClickListener(v -> finish());

        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("auth_token");
            editor.remove("username");
            editor.apply();
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            finish();
        });

        clearDataButton.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                .setTitle("⚠️ Clear Data")
                .setMessage("This will clear all saved data. Are you sure?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.apply();
                    Toast.makeText(this, "Data cleared!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }
}
