package com.example.chitchat.adapter.chats_fragment_adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chitchat.R;
import com.example.chitchat.chat.ChatActivity;
import com.example.chitchat.model.users_model.Users;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Users> usersList;
    private List<String> chatId;
    private Context context;

    public ChatAdapter(List<Users> usersList, List<String> chatId ,Context context) {
        this.usersList = usersList;
        this.chatId = chatId;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_user_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, final int position) {

        String name = usersList.get(position).getName();
        String image = usersList.get(position).getImage();

        holder.nameTextView.setText(name);
        Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.ic_baseline_account_circle_24))
                .into(holder.profileImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent chatIntent = new Intent(context, ChatActivity.class);
                chatIntent.putExtra("userID", chatId.get(position));
                context.startActivity(chatIntent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        Button followButton;
        CircleImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.singleProfileImage);
            nameTextView = itemView.findViewById(R.id.singleNameTextView);
            followButton = itemView.findViewById(R.id.followButton);

            followButton.setVisibility(View.GONE);

        }
    }
}
