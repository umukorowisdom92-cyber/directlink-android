package com.directlink.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {

    private TextView profileName, profilePhone, profileStatus;
    private Button chatButton, callButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        String username = getIntent().getStringExtra("username");
        String phone = getIntent().getStringExtra("phone");
        boolean online = getIntent().getBooleanExtra("online", false);

        profileName = findViewById(R.id.profileName);
        profilePhone = findViewById(R.id.profilePhone);
        profileStatus = findViewById(R.id.profileStatus);
        chatButton = findViewById(R.id.chatButton);
        callButton = findViewById(R.id.callButton);
        backButton = findViewById(R.id.backButton);

        profileName.setText(username);
        profilePhone.setText(phone);
        profileStatus.setText(online ? "🟢 Online" : "⚪ Offline");

        backButton.setOnClickListener(v -> finish());

        chatButton.setOnClickListener(v -> {
            Toast.makeText(this, "💬 Chat with " + username, Toast.LENGTH_SHORT).show();
        });

        callButton.setOnClickListener(v -> {
            Toast.makeText(this, "📞 Call " + username, Toast.LENGTH_SHORT).show();
        });
    }
}
