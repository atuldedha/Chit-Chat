package com.example.chitchat.settings.all_users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chitchat.R;
import com.example.chitchat.model.users_model.Users;
import com.example.chitchat.user_profile.UsersProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView allUserRecyclerView;

    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        query = FirebaseDatabase.getInstance().getReference().child("USERS");

        allUserRecyclerView = findViewById(R.id.allUserRecyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        allUserRecyclerView.setLayoutManager(linearLayoutManager);


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(query, Users.class).build();

        FirebaseRecyclerAdapter<Users, MyViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Users, MyViewHolder>(options) {
                    @NonNull
                    @Override
                    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_item_layout, parent, false);

                        return new MyViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Users model) {

                        String image = model.getThumb_image();
                        String name = model.getName();
                        String status = model.getStatus();

                        holder.setData(image, name, status);

                        final String userID = getRef(position).getKey();

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent usersProfileIntent = new Intent(getApplicationContext(), UsersProfileActivity.class);
                                usersProfileIntent.putExtra("userID", userID);

                                startActivity(usersProfileIntent);

                            }
                        });

                    }
                };

        firebaseRecyclerAdapter.startListening();
        firebaseRecyclerAdapter.notifyDataSetChanged();

        allUserRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView usersProfileImage;
        TextView usersName, usersStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            usersProfileImage = itemView.findViewById(R.id.usersProfileImage);
            usersName = itemView.findViewById(R.id.usersNameTextView);
            usersStatus = itemView.findViewById(R.id.usersStatusTextView);

        }

        private void setData(String image, String name, String status) {

            Glide.with(itemView.getContext()).load(image).apply(new RequestOptions().placeholder(R.drawable.ic_baseline_account_circle_24)).into(usersProfileImage);
            usersName.setText(name);
            usersStatus.setText(status);

        }

    }

}