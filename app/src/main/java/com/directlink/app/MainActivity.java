package com.directlink.app;

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

        serverUrlInput.setText("http://10.0.0.2:3030");

        connectButton.setOnClickListener(v -> {
            String serverUrl = serverUrlInput.getText().toString();
            if (serverUrl.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter server URL", Toast.LENGTH_SHORT).show();
                return;
            }
            
            DirectLinkClient.init(serverUrl);
            statusText.setText("Connected to: " + serverUrl);
            Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
            
            String result = DirectLinkClient.getContacts();
            contactsText.setText("Contacts: " + result);
        });
    }
}
