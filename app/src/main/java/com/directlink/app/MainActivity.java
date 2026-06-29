// ... existing code ...

@Override
public void onChatClick(String name, String phone) {
    // Open chat activity
    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
    intent.putExtra("username", name);
    intent.putExtra("phone", phone);
    startActivity(intent);
}

// ... rest of the code ...
