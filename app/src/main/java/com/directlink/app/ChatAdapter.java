package com.directlink.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<ChatItem> chatList;

    public ChatAdapter(List<ChatItem> chatList) {
        this.chatList = chatList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatItem chat = chatList.get(position);

        holder.name.setText(chat.getName());
        holder.lastMessage.setText(chat.getLastMessage());
        holder.time.setText(chat.getTime());

        // Avatar
        holder.avatar.setText(chat.getAvatarText());
        holder.avatar.setBackgroundColor(chat.getAvatarColor());

        // Online/Offline dot
        if (chat.isOnline()) {
            holder.onlineDot.setBackgroundResource(R.drawable.online_dot);
        } else {
            holder.onlineDot.setBackgroundResource(R.drawable.offline_dot);
        }

        // Badge
        if (chat.getBadgeCount() > 0) {
            holder.badge.setVisibility(View.VISIBLE);
            holder.badge.setText(String.valueOf(chat.getBadgeCount()));
        } else {
            holder.badge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView avatar, name, lastMessage, time, badge;
        View onlineDot;

        ViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.chatAvatar);
            name = itemView.findViewById(R.id.chatName);
            lastMessage = itemView.findViewById(R.id.chatLastMessage);
            time = itemView.findViewById(R.id.chatTime);
            badge = itemView.findViewById(R.id.chatBadge);
            onlineDot = itemView.findViewById(R.id.onlineDot);
        }
    }
}
