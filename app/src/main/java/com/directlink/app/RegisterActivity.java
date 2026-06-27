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
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput, phoneInput, passwordInput;
    private Button registerButton;
    private TextView statusText;
    private TextView goToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.registerUsername);
        phoneInput = findViewById(R.id.registerPhone);
        passwordInput = findViewById(R.id.registerPassword);
        registerButton = findViewById(R.id.registerButton);
        statusText = findViewById(R.id.registerStatus);
        goToLogin = findViewById(R.id.goToLogin);

        SharedPreferences prefs = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE);
        String serverUrl = prefs.getString("server_url", "http://10.0.0.2:3030");

        goToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            statusText.setText("⏳ Registering...");
            registerButton.setEnabled(false);

            new Thread(() -> {
                try {
                    DirectLinkClient.init(serverUrl);
                    String result = DirectLinkClient.register(username, phone, password);
                    JSONObject json = new JSONObject(result);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            if (json.has("token")) {
                                statusText.setText("✅ Registration successful!");
                                Toast.makeText(RegisterActivity.this, "Account created! Please login.", Toast.LENGTH_LONG).show();

                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            } else if (json.has("error")) {
                                String error = json.getString("error");
                                statusText.setText("❌ " + error);
                                Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                            } else {
                                statusText.setText("❌ Registration failed");
                                Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            statusText.setText("❌ Error: " + e.getMessage());
                            Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        registerButton.setEnabled(true);
                    });
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        statusText.setText("❌ Error: " + e.getMessage());
                        Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        registerButton.setEnabled(true);
                    });
                }
            }).start();
        });
    }
}
