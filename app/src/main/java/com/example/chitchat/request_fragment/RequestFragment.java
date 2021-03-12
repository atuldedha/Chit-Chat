package com.example.chitchat.request_fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chitchat.R;
import com.example.chitchat.model.messages_model.Messege;
import com.example.chitchat.user_profile.UsersProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment {

    private RecyclerView recyclerView;

    private DatabaseReference friendsRef, requestRef, rootRef;

    private Query requestQuery;

    private FirebaseUser currentUser;

    private String currentState = "req_received";

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_request, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(linearLayoutManager);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        friendsRef = FirebaseDatabase.getInstance().getReference().child("friends");
        requestRef = FirebaseDatabase.getInstance().getReference().child("friend_request");
        rootRef = FirebaseDatabase.getInstance().getReference();

        requestQuery = FirebaseDatabase.getInstance().getReference().child("friend_request").child(currentUser.getUid());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Messege> options = new FirebaseRecyclerOptions.Builder<Messege>()
                .setQuery(requestQuery, Messege.class).build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Messege, ViewHolder>(options) {

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_layout, parent, false);

                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, final int position, @NonNull final Messege model) {

                final String checkReceivingId = getRef(position).getKey();

                DatabaseReference requestTypeRef = getRef(position).child("request_type").getRef();
                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){

                            String requestType = snapshot.getValue().toString();

                            if(requestType.equals("received")){

                                rootRef.child("USERS").child(checkReceivingId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if(snapshot.exists()){

                                            String image = snapshot.child("image").getValue().toString();
                                            String name = snapshot.child("name").getValue().toString();

                                            holder.nameTextView.setText(name);

                                            Glide.with(getContext()).load(image)
                                                    .apply(new RequestOptions().placeholder(R.drawable.ic_baseline_account_circle_24))
                                                    .into(holder.profileImage);

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(getContext(), UsersProfileActivity.class);
                        profileIntent.putExtra("userID", getRef(position).getKey());

                        startActivity(profileIntent);

                    }
                });

                holder.followButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        /////Req. Received
                        //{

                        holder.followButton.setEnabled(false);

                        if (currentState.equals("req_received")) {

                            final String date = DateFormat.getDateTimeInstance().format(new Date());

                            Map<String, Object> friendsMap = new HashMap<>();

                            friendsMap.put("friends/" + currentUser.getUid() + "/" + getRef(position).getKey() + "/date", date);
                            friendsMap.put("friends/" + getRef(position).getKey() + "/" + currentUser.getUid() + "/date", date);

                            friendsMap.put("friend_request/" + currentUser.getUid() + "/" + getRef(position).getKey() + "/date", null);
                            friendsMap.put("friend_request/" + getRef(position).getKey() + "/" + currentUser.getUid() + "/date", null);

                            rootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                                    if (error == null) {

                                        currentState = "friends";

                                        holder.followButton.setText("Unfriend");

                                    } else {

                                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();

                                    }

                                    holder.followButton.setEnabled(true);

                                }

                            });


                        }

                    }
                });

            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        Button followButton;
        CircleImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.singleProfileImage);
            nameTextView = itemView.findViewById(R.id.singleNameTextView);
            followButton = itemView.findViewById(R.id.followButton);

            followButton.setText("Accept Request");

        }
    }
}