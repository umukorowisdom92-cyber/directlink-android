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

public class LoginActivity extends AppCompatActivity {

    private EditText phoneInput, passwordInput;
    private Button loginButton;
    private TextView statusText;
    private TextView goToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneInput = findViewById(R.id.loginPhone);
        passwordInput = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        statusText = findViewById(R.id.loginStatus);
        goToRegister = findViewById(R.id.goToRegister);

        SharedPreferences prefs = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE);
        String serverUrl = prefs.getString("server_url", "https://plains-fun-alice-educational.trycloudflare.com");

        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        loginButton.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            statusText.setText("⏳ Logging in...");
            loginButton.setEnabled(false);

            new Thread(() -> {
                try {
                    DirectLinkClient.init(serverUrl);
                    String result = DirectLinkClient.login(phone, password);
                    JSONObject json = new JSONObject(result);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            if (json.has("token")) {
                                String token = json.getString("token");
                                String username = json.getString("username");

                                statusText.setText("✅ Login successful!");

                                SharedPreferences.Editor editor = getSharedPreferences("DirectLinkPrefs", MODE_PRIVATE).edit();
                                editor.putString("auth_token", token);
                                editor.putString("username", username);
                                editor.apply();

                                // Set token in client
                                DirectLinkClient.setAuthToken(token);

                                Toast.makeText(LoginActivity.this, "Welcome " + username + "!", Toast.LENGTH_LONG).show();

                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else if (json.has("error")) {
                                String error = json.getString("error");
                                statusText.setText("❌ " + error);
                                Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                            } else {
                                statusText.setText("❌ Login failed");
                                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            statusText.setText("❌ Error: " + e.getMessage());
                            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        loginButton.setEnabled(true);
                    });
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        statusText.setText("❌ Error: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        loginButton.setEnabled(true);
                    });
                }
            }).start();
        });
    }
}
