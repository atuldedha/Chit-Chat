package com.example.chitchat.user_profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chitchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UsersProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView profileName, profileStatus, totalFriends;
    private Button sendRequestButton, declineRequestButton;

    private DatabaseReference databaseReference;
    private DatabaseReference friendRequestRef;
    private DatabaseReference friendDatabaseRef;
    private DatabaseReference notificationRef;
    private DatabaseReference rootRef;

    private FirebaseUser currentUser;

    private String currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);

        initWidgets();

        final String userID = getIntent().getStringExtra("userID");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("USERS").child(userID);
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("friend_request");
        friendDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friends");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("notifications");
        rootRef = FirebaseDatabase.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = snapshot.child("name").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String image = snapshot.child("image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);

                Glide.with(getApplicationContext()).load(image).
                        apply(new RequestOptions().placeholder(R.drawable.ic_baseline_account_circle_24))
                        .into(profileImage);

                //////Accept request
                //{

                friendRequestRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.hasChild(userID)) {

                            String reqType = snapshot.child(userID).child("request_type").getValue().toString();

                            if (reqType.equals("received")) {

                                currentState = "req_received";
                                sendRequestButton.setText("Accept Friend Request");

                                declineRequestButton.setVisibility(View.VISIBLE);
                                declineRequestButton.setEnabled(false);

                            } else if (reqType.equals("sent")) {

                                currentState = "req_sent";
                                sendRequestButton.setText("Cancel request");

                                declineRequestButton.setVisibility(View.INVISIBLE);
                                declineRequestButton.setEnabled(false);

                            }

                        } else {

                            friendDatabaseRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if (snapshot.hasChild(userID)) {

                                        currentState = "friends";
                                        sendRequestButton.setText("Unfriend");

                                        declineRequestButton.setVisibility(View.INVISIBLE);
                                        declineRequestButton.setEnabled(false);

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /////sneding request{


        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendRequestButton.setEnabled(false);

                if (currentState.equals("not_friends")) {

                    friendRequestRef.child(currentUser.getUid()).child(userID).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                friendRequestRef.child(userID).child(currentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, Object> notificationMap = new HashMap<>();
                                        notificationMap.put("from", currentUser.getUid());
                                        notificationMap.put("type", "request");

                                        notificationRef.child(userID).push().setValue(notificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {


                                                    currentState = "req_sent";

                                                    sendRequestButton.setText("Cancel Request");
                                                    sendRequestButton.setEnabled(true);

                                                    declineRequestButton.setVisibility(View.INVISIBLE);
                                                    declineRequestButton.setEnabled(false);


                                                } else {

                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(UsersProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                                                }

                                            }
                                        });

                                    }
                                });

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(UsersProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }
                ///// };

                //////Cancel Request Sent
                // {

                if (currentState.equals("req_sent")) {

                    friendRequestRef.child(currentUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                friendRequestRef.child(userID).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            currentState = "not_friends";

                                            sendRequestButton.setText("Send Friend Request");

                                            declineRequestButton.setVisibility(View.INVISIBLE);
                                            declineRequestButton.setEnabled(false);

                                        } else {

                                            String error = task.getException().getMessage();
                                            Toast.makeText(UsersProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                                        }

                                        sendRequestButton.setEnabled(true);
                                    }
                                });

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(UsersProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                            sendRequestButton.setEnabled(true);

                        }
                    });

                }

                // }

                /////Req. Received
                //{

                if (currentState.equals("req_received")) {

                    final String date = DateFormat.getDateTimeInstance().format(new Date());

                    Map<String, Object> friendsMap = new HashMap<>();

                    friendsMap.put("friends/" + currentUser.getUid() + "/" + userID + "/date", date);
                    friendsMap.put("friends/" + userID + "/" + currentUser.getUid() + "/date", date);

                    friendsMap.put("friend_request/" + currentUser.getUid() + "/" + userID + "/date", null);
                    friendsMap.put("friend_request/" + userID + "/" + currentUser.getUid() + "/date", null);

                    rootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            if (error == null) {

                                currentState = "friends";

                                sendRequestButton.setText("Unfriend");

                                declineRequestButton.setVisibility(View.INVISIBLE);
                                declineRequestButton.setEnabled(false);

                            } else {

                                Toast.makeText(UsersProfileActivity.this, error.toString(), Toast.LENGTH_SHORT).show();

                            }

                            sendRequestButton.setEnabled(true);

                        }

                    });


                }

                ///}

                ////Unfriend a friend
                //{

                if (currentState.equals("friends")) {

                    friendDatabaseRef.child(currentUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                friendDatabaseRef.child(userID).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            currentState = "not_friends";

                                            sendRequestButton.setText("Send Friend Request");

                                            declineRequestButton.setVisibility(View.INVISIBLE);
                                            declineRequestButton.setEnabled(false);

                                        } else {

                                            String error = task.getException().getMessage();
                                            Toast.makeText(UsersProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                                        }

                                        sendRequestButton.setEnabled(true);

                                    }
                                });

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(UsersProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                            sendRequestButton.setEnabled(true);

                        }
                    });

                }

                //}

            }
        });

    }

    private void initWidgets() {

        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileNameTextView);
        profileStatus = findViewById(R.id.profileStatusTextView);
        totalFriends = findViewById(R.id.totalFriensTextView);
        sendRequestButton = findViewById(R.id.sendRequestButton);
        declineRequestButton = findViewById(R.id.declineRequestButton);

        currentState = "not_friends";

    }

}