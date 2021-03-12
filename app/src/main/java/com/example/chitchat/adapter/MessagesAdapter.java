package com.example.chitchat.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chitchat.R;
import com.example.chitchat.model.messages_model.Messege;
import com.google.android.material.canvas.CanvasCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    List<Messege> messegeList;
    private FirebaseUser currentUser;

    public MessagesAdapter(List<Messege> messegeList) {
        this.messegeList = messegeList;
    }

    @NonNull
    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_layout, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.ViewHolder holder, int position) {

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String currentUserID = currentUser.getUid();

        String text = messegeList.get(position).getMessage();

        String fromUser = messegeList.get(position).getFrom();
        String type = messegeList.get(position).getType();

        if(fromUser.equals(currentUserID)){

            holder.messageSend.setBackgroundColor(Color.parseColor("#996FD6"));
            holder.messageSend.setTextColor(Color.BLACK);

        }else{

            holder.messageSend.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.messageSend.setTextColor(Color.BLACK);

        }

        if(type.equals("text")){

            holder.messageSend.setVisibility(View.VISIBLE);
            holder.imageMessage.setVisibility(View.GONE);

            holder.messageSend.setText(text);

        }else{

            holder.messageSend.setVisibility(View.GONE);
            holder.imageMessage.setVisibility(View.VISIBLE);

            Glide.with(holder.itemView.getContext()).load(messegeList.get(position).getMessage())
                    .apply(new RequestOptions().placeholder(R.drawable.ic_baseline_account_circle_24))
                    .into(holder.imageMessage);

        }

    }

    @Override
    public int getItemCount() {
        return messegeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView messageSend;
        private CircleImageView senderPicture;
        private ImageView imageMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            messageSend = itemView.findViewById(R.id.senderMessage);
            senderPicture = itemView.findViewById(R.id.senderImage);
            imageMessage = itemView.findViewById(R.id.imageMessage);

        }
    }
}
