package com.directlink.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private TextView chatPartnerName;
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();
    private String chatPartner;
    private String chatPartnerPhone;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatPartner = getIntent().getStringExtra("username");
        chatPartnerPhone = getIntent().getStringExtra("phone");
        currentUsername = ConnectionManager.getInstance().getUsername();

        chatPartnerName = findViewById(R.id.chatPartnerName);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        if (chatPartnerName != null) {
            chatPartnerName.setText(chatPartner);
        }

        messageAdapter = new MessageAdapter(messages);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });

        loadMessages();
    }

    private void loadMessages() {
        messages.clear();
        messages.add(new Message(chatPartner, "Hey there! How are you?", System.currentTimeMillis(), false));
        messages.add(new Message(currentUsername, "I'm good, thanks! You?", System.currentTimeMillis(), true));
        messages.add(new Message(chatPartner, "Great! Ready to chat?", System.currentTimeMillis(), false));
        messageAdapter.notifyDataSetChanged();
    }

    private void sendMessage(String message) {
        messages.add(new Message(currentUsername, message, System.currentTimeMillis(), true));
        messageAdapter.notifyDataSetChanged();
        messageInput.setText("");
        messagesRecyclerView.scrollToPosition(messages.size() - 1);
        Toast.makeText(this, "Message sent to " + chatPartner, Toast.LENGTH_SHORT).show();
    }

    static class Message {
        String sender;
        String content;
        long timestamp;
        boolean isSent;

        Message(String sender, String content, long timestamp, boolean isSent) {
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
            this.isSent = isSent;
        }
    }

    static class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
        private List<Message> messages;

        MessageAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Message msg = messages.get(position);
            holder.textView.setText(msg.sender + ": " + msg.content);
        }

        @Override
        public int getItemCount() { return messages.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
