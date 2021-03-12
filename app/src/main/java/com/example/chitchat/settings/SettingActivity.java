package com.example.chitchat.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chitchat.MainActivity;
import com.example.chitchat.R;
import com.example.chitchat.settings.status_change.ChangeStatusActivity;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    String currentUid;
    FirebaseUser currentUser;

    private CircleImageView profileCircleImageView;
    private TextView displayNameTextView, statusTextView;
    private Button changeImageButton, changeStatusButton;

    private StorageReference storageReference;

    Bitmap thumbBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        storageReference = FirebaseStorage.getInstance().getReference();

        profileCircleImageView = findViewById(R.id.profileCircleImageView);
        displayNameTextView = findViewById(R.id.displayNameTextView);
        statusTextView = findViewById(R.id.statusTextView);
        changeImageButton = findViewById(R.id.changeImageButton);
        changeStatusButton = findViewById(R.id.changeStatusButton);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = currentUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("USERS").child(currentUid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = snapshot.child("name").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String image = snapshot.child("image").getValue().toString();
                String thumbImgae = snapshot.child("thumb_image").getValue().toString();

                displayNameTextView.setText(name);
                statusTextView.setText(status);

                Glide.with(getApplicationContext()).load(image).apply(new RequestOptions().placeholder(R.drawable.ic_baseline_account_circle_24)).into(profileCircleImageView);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent changeStatusIntent = new Intent(SettingActivity.this, ChangeStatusActivity.class);
                changeStatusIntent.putExtra("currentStatus", statusTextView.getText().toString());
                startActivity(changeStatusIntent);

            }
        });

        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), 1);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                final File thumbFile = new File(resultUri.getPath());

                try {

                    thumbBitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .compressToBitmap(thumbFile);

                } catch (IOException e) {

                    e.printStackTrace();

                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

                final byte[] thumbByte = byteArrayOutputStream.toByteArray();

                final StorageReference profileImageRef = storageReference.child("profile_images").child(currentUid + ".jpg");

                final StorageReference profileThumbImageRef = storageReference.child("profile_images").child("thumbs").child(currentUid + ".jpg");

                profileImageRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            if (task.getResult().getMetadata().getReference() != null) {
                                Task<Uri> downloadUri = task.getResult().getStorage().getDownloadUrl();

                                downloadUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String imageUri = uri.toString();

                                        UploadTask uploadTask = profileThumbImageRef.putBytes(thumbByte);
                                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {

                                                if(thumbTask.isSuccessful()) {

                                                    Task<Uri> downloadThumbUri = thumbTask.getResult().getStorage().getDownloadUrl();
                                                    downloadThumbUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {

                                                            String thumbUri = uri.toString();

                                                            Map<String, Object> updateUser = new HashMap<>();
                                                            updateUser.put("image", imageUri);
                                                            updateUser.put("thumb_image",thumbUri);

                                                            databaseReference.updateChildren(updateUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {

                                                                        Toast.makeText(SettingActivity.this, "User Updated", Toast.LENGTH_SHORT).show();

                                                                    } else {

                                                                        String error = task.getException().getMessage();
                                                                        Toast.makeText(SettingActivity.this, error, Toast.LENGTH_SHORT).show();

                                                                    }
                                                                }
                                                            });

                                                        }
                                                    });
                                                }else{

                                                    String error = thumbTask.getException().getMessage();
                                                    Toast.makeText(SettingActivity.this, error, Toast.LENGTH_SHORT).show();

                                                }

                                            }
                                        });

                                    }
                                });
                            }

                        } else {

                            String error = task.getException().getMessage();
                            Toast.makeText(SettingActivity.this, error, Toast.LENGTH_SHORT).show();

                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }

        }

    }

}