package com.directlink.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

public class CallsActivity extends BaseActivity {

    private ListView callsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calls);

        // Setup bottom navigation (from BaseActivity)
        setupBottomNavigation();

        callsListView = findViewById(R.id.callsListView);

        List<String> calls = new ArrayList<>();
        calls.add("📞 Ken Kingston - 2 min ago");
        calls.add("📞 Jeff Sirois - 1 hour ago");
        calls.add("📞 Raul Loa - Yesterday");
        calls.add("📞 Jeff - 3 days ago");
        calls.add("📞 Dazza Lee - 1 week ago");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, calls);
        callsListView.setAdapter(adapter);
    }
}
