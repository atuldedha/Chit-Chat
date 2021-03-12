package com.example.chitchat.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chitchat.GetTimeAgo;
import com.example.chitchat.R;
import com.example.chitchat.adapter.MessagesAdapter;
import com.example.chitchat.model.messages_model.Messege;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private FirebaseUser currentUser;
    private DatabaseReference rootReference;
    private DatabaseReference userReference;
    String currentUserID;

    ///user Info
    private String chatUserId;
    private String userName;
    //user Info

    private TextView chatName;
    private TextView lastSeen;
    private CircleImageView chatImage;

    private ImageButton chatSendImageButton, addImageButton;
    private EditText sendMessageEditText;

    private RecyclerView chatRecyclerView;
    MessagesAdapter messagesAdapter;
    private List<Messege> messegeList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;

    private SwipeRefreshLayout swipeRefreshLayout;

    public static final int TOTAL_ITEM_TO_LOAD = 10;
    private int currentPage = 1;

    private int itemPos = 0;
    private String lastKey = "";
    private String prevToLastKey = "";

    private int GALLERY_PICK = 1;

    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.toolbar);

        chatUserId = getIntent().getStringExtra("userID");
        userName = getIntent().getStringExtra("userName");
        rootReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserID = currentUser.getUid();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        chatName = findViewById(R.id.chatNameTextView);
        lastSeen = findViewById(R.id.lastSeenTextView);
        chatImage = findViewById(R.id.chatImage);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        chatRecyclerView.setLayoutManager(linearLayoutManager);

        messagesAdapter = new MessagesAdapter(messegeList);

        loadMessages();
        messagesAdapter.notifyDataSetChanged();

        chatRecyclerView.setAdapter(messagesAdapter);

        sendMessageEditText = findViewById(R.id.sendMessageEditText);
        addImageButton = findViewById(R.id.addImageButton);
        chatSendImageButton = findViewById(R.id.sendImageButton);

        chatName.setText(userName);

        rootReference.child("USERS").child(chatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String image = snapshot.child("image").getValue().toString();
                String online = snapshot.child("online").getValue().toString();

                Glide.with(getApplicationContext()).load(image).
                        apply(new RequestOptions().placeholder(R.drawable.ic_baseline_account_circle_24))
                        .into(chatImage);

                if(online.equals("true")){

                    lastSeen.setText("online");

                }else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    String lastSeenTime = getTimeAgo.getTimeAgo(Long.parseLong(online), getApplicationContext());

                    lastSeen.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rootReference.child("chat").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.hasChild(chatUserId)){

                    Map<String, Object> chatMap = new HashMap<>();
                    chatMap.put("seen", false);
                    chatMap.put("time_stamp", ServerValue.TIMESTAMP);

                    Map<String, Object> userChatMap = new HashMap<>();
                    userChatMap.put("chat/" + currentUserID + "/" + chatUserId, chatMap);
                    userChatMap.put("chat/" + chatUserId + "/" + currentUserID, chatMap);

                    rootReference.updateChildren(userChatMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            if (error != null){

                                Log.d("Chat log","Success");

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        chatSendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PICK);

            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                currentPage++;

                itemPos = 0;

                loadMoreMessages();

            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            final String currentUserRef = "messages/" + currentUserID + "/" + chatUserId;
            final String chatUserRef = "messages/" + chatUserId + "/" +currentUserID;

            DatabaseReference user_message_push = rootReference.child("message").child(currentUserID)
                    .child(chatUserId).push();

            final String pushId = user_message_push.getKey();

            StorageReference filepath = storageReference.child("message_images").child(pushId + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        if (task.getResult().getMetadata().getReference() != null) {
                            Task<Uri> downloadUri = task.getResult().getStorage().getDownloadUrl();

                            downloadUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String imageUri = uri.toString();

                                    Map<String, Object> messageMap = new HashMap<>();
                                    messageMap.put("message", imageUri);
                                    messageMap.put("seen", false);
                                    messageMap.put("type", "image");
                                    messageMap.put("time", ServerValue.TIMESTAMP);
                                    messageMap.put("from", currentUserID);

                                    sendMessageEditText.setText("");

                                    Map<String, Object> messageUserMap = new HashMap<>();
                                    messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
                                    messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

                                    rootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                                            if(error != null){

                                                Log.d("Message", "Sent to database");

                                            }

                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            });

        }

    }

    private void sendMessage(){

        String message = sendMessageEditText.getText().toString();

        if(!(TextUtils.isEmpty(message))){

            String currentUserRef = "messages/" + currentUserID + "/" + chatUserId;
            String chatUserRef = "messages/" + chatUserId + "/" +currentUserID;

            DatabaseReference messagePush = rootReference.child("message").child(currentUserID)
                    .child(chatUserId).push();

            String pudhID = messagePush.getKey();

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", currentUserID);

            sendMessageEditText.setText("");

            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(currentUserRef + "/" + pudhID, messageMap);
            messageUserMap.put(chatUserRef + "/" + pudhID, messageMap);

            rootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                    if(error != null){

                        Log.d("Message", "Sent to database");

                    }

                }
            });

        }

    }

    private void loadMoreMessages(){

        DatabaseReference messageRef = rootReference.child("messages").child(currentUserID).child(chatUserId);

        Query messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Messege messege = snapshot.getValue(Messege.class);
                String messageKey = snapshot.getKey();
                messegeList.add(itemPos++ ,messege);

                if(!prevToLastKey.equals(messageKey)){

                    messegeList.add(itemPos++, messege);

                }else{

                    prevToLastKey = lastKey;

                }

                if(itemPos == 1){

                    lastKey = messageKey;

                }

                Log.d("TOTAL KEYS", "last key :" + lastKey + " prevtolast :" + prevToLastKey + " Message key : " + messageKey);

                messagesAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);

                linearLayoutManager.scrollToPositionWithOffset(10,0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        

    }

    private void loadMessages(){

        DatabaseReference messageRef = rootReference.child("messages").child(currentUserID).child(chatUserId);

        Query messageQuery = messageRef.limitToLast(currentPage * TOTAL_ITEM_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Messege messege = snapshot.getValue(Messege.class);

                itemPos++;

                if(itemPos == 1){

                    lastKey = snapshot.getKey();
                    prevToLastKey = lastKey;

                }

                messegeList.add(messege);
                messagesAdapter.notifyDataSetChanged();

                chatRecyclerView.scrollToPosition(messegeList.size() - 1);

                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}