package com.example.chitchat.friends_frament;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chitchat.R;
import com.example.chitchat.chat.ChatActivity;
import com.example.chitchat.model.friends_model.Friends;
import com.example.chitchat.user_profile.UsersProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {

    private View view;

    private RecyclerView friendRecyclerView;

    private DatabaseReference databaseReference;

    private FirebaseUser currentUSer;
    private Query query;

    String currentUserID;

    ///user info
    String name;
    String thumbImage;
    String userOnline;
    ////user info

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_friends, container, false);

        friendRecyclerView = view.findViewById(R.id.friendRecyclerView);

        currentUSer = FirebaseAuth.getInstance().getCurrentUser();
        currentUserID = currentUSer.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("USERS");
        databaseReference.keepSynced(true);

        query = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        friendRecyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder <Friends>()
                .setQuery(query, Friends.class).build();

        FirebaseRecyclerAdapter<Friends, MyViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Friends, MyViewHolder>(options) {
                    @NonNull
                    @Override
                    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_item_layout, parent, false);

                        return new MyViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final MyViewHolder holder, int position, @NonNull Friends model) {

                        final String date = model.getDate();

                        final String userID = getRef(position).getKey();


                        databaseReference.child(userID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                name = snapshot.child("name").getValue().toString();
                                thumbImage = snapshot.child("thumb_image").getValue().toString();
                                userOnline = snapshot.child("online").getValue().toString();

                                holder.setData(thumbImage, name, date, userOnline);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[] = new CharSequence[] {"Open Profile" , "Send Message"};


                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(which == 0){

                                            Intent usersProfileIntent = new Intent(getContext(), UsersProfileActivity.class);
                                            usersProfileIntent.putExtra("userID", userID);

                                            startActivity(usersProfileIntent);

                                        }if(which == 1){

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("userID", userID);
                                            chatIntent.putExtra("userName", name);
                                            startActivity(chatIntent);

                                        }

                                    }
                                });

                                builder.show();

                            }
                        });

                    }
                };

        firebaseRecyclerAdapter.startListening();
        firebaseRecyclerAdapter.notifyDataSetChanged();

        friendRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView usersProfileImage ;
        ImageView onlineImageView;
        TextView usersName, usersStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            usersProfileImage = itemView.findViewById(R.id.usersProfileImage);
            usersName = itemView.findViewById(R.id.usersNameTextView);
            usersStatus = itemView.findViewById(R.id.usersStatusTextView);
            onlineImageView = itemView.findViewById(R.id.userOnlineImageView);

        }

        private void setData(String image, String name ,String date, String userOnline) {

            if(image != null) {
                Glide.with(getContext()).load(image).apply(new RequestOptions().placeholder(R.drawable.ic_baseline_account_circle_24))
                        .into(usersProfileImage);
            }
            usersName.setText(name);
            usersStatus.setText(date);

            if(userOnline.equals("true")){

                onlineImageView.setVisibility(View.VISIBLE);

            }else{

                onlineImageView.setVisibility(View.INVISIBLE);

            }

        }

    }

}