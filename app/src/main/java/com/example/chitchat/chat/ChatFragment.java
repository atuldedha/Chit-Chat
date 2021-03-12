package com.example.chitchat.chat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chitchat.R;
import com.example.chitchat.adapter.chats_fragment_adapter.ChatAdapter;
import com.example.chitchat.model.users_model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;

    private DatabaseReference rootRef;
    private Query query;

    private String currentUserId;

    private List<String> chatID = new ArrayList<>();
    private List<Users> usersList = new ArrayList<>();

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(linearLayoutManager);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();

        getChatIds();

        getChattedUsers();

        chatAdapter = new ChatAdapter(usersList, chatID, getContext());
        recyclerView.setAdapter(chatAdapter);
        chatAdapter.notifyDataSetChanged();

        return view;

    }

    private void getChatIds(){

        rootRef.child("chat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                        chatID.add(dataSnapshot.getKey());

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getChattedUsers(){

        for(int i=0; i<chatID.size(); i++){

            rootRef.child(chatID.get(i)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists()) {

                        Users user = snapshot.getValue(Users.class);
                        usersList.add(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

}