package com.directlink.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private TextView usernameText, serverUrlText;
    private Button logoutButton, clearDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        usernameText = findViewById(R.id.usernameText);
        serverUrlText = findViewById(R.id.serverUrlText);
        logoutButton = findViewById(R.id.logoutButton);
        clearDataButton = findViewById(R.id.clearDataButton);

        ConnectionManager.getInstance().init(this);

        String username = ConnectionManager.getInstance().getUsername();
        usernameText.setText("👤 " + (username != null ? username : "Guest"));
        serverUrlText.setText("🌐 Server: " + ConnectionManager.SERVER_URL);

        logoutButton.setOnClickListener(v -> {
            ConnectionManager.getInstance().logout();
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            finish();
        });

        clearDataButton.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                .setTitle("⚠️ Clear Data")
                .setMessage("This will clear all saved data. Are you sure?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    ConnectionManager.getInstance().logout();
                    Toast.makeText(this, "Data cleared!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }
}
