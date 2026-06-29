package com.directlink.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class MessageDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "directlink_messages.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CHAT_PARTNER = "chat_partner";
    private static final String COLUMN_SENDER = "sender";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_IS_READ = "is_read";

    public MessageDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CHAT_PARTNER + " TEXT,"
                + COLUMN_SENDER + " TEXT,"
                + COLUMN_MESSAGE + " TEXT,"
                + COLUMN_TIMESTAMP + " TEXT,"
                + COLUMN_IS_READ + " INTEGER DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    public void saveMessage(String chatPartner, String sender, String message, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_PARTNER, chatPartner);
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_MESSAGE, message);
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_IS_READ, 1);
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public List<MessageItem> getMessages(String chatPartner) {
        List<MessageItem> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MESSAGES
                + " WHERE " + COLUMN_CHAT_PARTNER + " = ?"
                + " ORDER BY " + COLUMN_ID + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{chatPartner});

        if (cursor.moveToFirst()) {
            do {
                MessageItem message = new MessageItem();
                message.id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                message.chatPartner = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHAT_PARTNER));
                message.sender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER));
                message.message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE));
                message.timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                message.isRead = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_READ)) == 1;
                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messages;
    }

    public void clearMessages(String chatPartner) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, COLUMN_CHAT_PARTNER + " = ?", new String[]{chatPartner});
        db.close();
    }

    public static class MessageItem {
        public int id;
        public String chatPartner;
        public String sender;
        public String message;
        public String timestamp;
        public boolean isRead;
    }
}
