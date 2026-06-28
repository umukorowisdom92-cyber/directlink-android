package com.directlink.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatItem> chatList;
    private OnFriendRequestListener requestListener;

    public interface OnFriendRequestListener {
        void onAccept(String requestId, String name, String phone);
        void onReject(String requestId, String name, String phone);
        void onChatClick(String name, String phone);
    }

    public ChatAdapter(List<ChatItem> chatList, OnFriendRequestListener listener) {
        this.chatList = chatList;
        this.requestListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return chatList.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ChatItem.TYPE_FRIEND_REQUEST) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.friend_request_item, parent, false);
            return new FriendRequestViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item, parent, false);
            return new ChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatItem chat = chatList.get(position);

        if (chat.getType() == ChatItem.TYPE_FRIEND_REQUEST) {
            FriendRequestViewHolder frHolder = (FriendRequestViewHolder) holder;
            frHolder.name.setText(chat.getName());
            frHolder.phone.setText(chat.getPhone());
            frHolder.avatar.setText(chat.getAvatarText());
            frHolder.avatar.setBackgroundColor(chat.getAvatarColor());

            frHolder.acceptButton.setOnClickListener(v -> {
                if (requestListener != null) {
                    requestListener.onAccept(chat.getRequestId(), chat.getName(), chat.getPhone());
                }
            });

            frHolder.rejectButton.setOnClickListener(v -> {
                if (requestListener != null) {
                    requestListener.onReject(chat.getRequestId(), chat.getName(), chat.getPhone());
                }
            });
        } else {
            ChatViewHolder chatHolder = (ChatViewHolder) holder;
            chatHolder.name.setText(chat.getName());
            chatHolder.lastMessage.setText(chat.getLastMessage());
            chatHolder.time.setText(chat.getTime());
            chatHolder.avatar.setText(chat.getAvatarText());
            chatHolder.avatar.setBackgroundColor(chat.getAvatarColor());

            if (chat.isOnline()) {
                chatHolder.onlineDot.setBackgroundResource(R.drawable.online_dot);
            } else {
                chatHolder.onlineDot.setBackgroundResource(R.drawable.offline_dot);
            }

            if (chat.getBadgeCount() > 0) {
                chatHolder.badge.setVisibility(View.VISIBLE);
                chatHolder.badge.setText(String.valueOf(chat.getBadgeCount()));
            } else {
                chatHolder.badge.setVisibility(View.GONE);
            }

            chatHolder.itemView.setOnClickListener(v -> {
                if (requestListener != null) {
                    requestListener.onChatClick(chat.getName(), chat.getPhone());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView avatar, name, lastMessage, time, badge;
        View onlineDot;

        ChatViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.chatAvatar);
            name = itemView.findViewById(R.id.chatName);
            lastMessage = itemView.findViewById(R.id.chatLastMessage);
            time = itemView.findViewById(R.id.chatTime);
            badge = itemView.findViewById(R.id.chatBadge);
            onlineDot = itemView.findViewById(R.id.onlineDot);
        }
    }

    static class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        TextView avatar, name, phone;
        Button acceptButton, rejectButton;

        FriendRequestViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.frAvatar);
            name = itemView.findViewById(R.id.frName);
            phone = itemView.findViewById(R.id.frPhone);
            acceptButton = itemView.findViewById(R.id.frAccept);
            rejectButton = itemView.findViewById(R.id.frReject);
        }
    }
}
