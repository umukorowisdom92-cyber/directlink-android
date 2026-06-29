package com.directlink.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    protected LinearLayout navChats, navContacts, navCalls, navSettings;
    protected TextView badgeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNavigation() {
        navChats = findViewById(R.id.navChats);
        navContacts = findViewById(R.id.navContacts);
        navCalls = findViewById(R.id.navCalls);
        navSettings = findViewById(R.id.navSettings);

        // Find badge in navChats
        View badgeView = navChats.findViewById(R.id.badgeView);
        if (badgeView != null) {
            badgeText = badgeView.findViewById(R.id.badgeText);
        }

        navChats.setOnClickListener(v -> {
            if (!(this instanceof MainActivity)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        navContacts.setOnClickListener(v -> {
            if (!(this instanceof ContactsActivity)) {
                startActivity(new Intent(this, ContactsActivity.class));
                finish();
            }
        });

        navCalls.setOnClickListener(v -> {
            if (!(this instanceof CallsActivity)) {
                startActivity(new Intent(this, CallsActivity.class));
                finish();
            }
        });

        navSettings.setOnClickListener(v -> {
            if (!(this instanceof SettingsActivity)) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
            }
        });

        highlightCurrentNav();
        updateBadge();
    }

    protected void highlightCurrentNav() {
        resetNav(navChats);
        resetNav(navContacts);
        resetNav(navCalls);
        resetNav(navSettings);

        if (this instanceof MainActivity) {
            highlightNav(navChats);
        } else if (this instanceof ContactsActivity) {
            highlightNav(navContacts);
        } else if (this instanceof CallsActivity) {
            highlightNav(navCalls);
        }
        // Settings - no highlight
    }

    protected void highlightNav(LinearLayout nav) {
        TextView label = (TextView) nav.getChildAt(1);
        if (label != null) {
            label.setTextColor(0xFF3F51B5);
            label.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    protected void resetNav(LinearLayout nav) {
        TextView label = (TextView) nav.getChildAt(1);
        if (label != null) {
            label.setTextColor(0xFF666666);
            label.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    protected void updateBadge() {
        int totalUnread = com.directlink.app.NotificationManager.getInstance().getTotalUnreadCount();
        if (badgeText != null) {
            if (totalUnread > 0) {
                badgeText.setVisibility(View.VISIBLE);
                badgeText.setText(String.valueOf(totalUnread));
            } else {
                badgeText.setVisibility(View.GONE);
            }
        }
    }
}
