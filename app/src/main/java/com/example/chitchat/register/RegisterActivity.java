package com.example.chitchat.register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.chitchat.MainActivity;
import com.example.chitchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private FrameLayout mainFrameLayout;

    public boolean setSignup=false;

    private FirebaseAuth firebaseAuth;

    private FirebaseUser currentUser;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mainFrameLayout=findViewById(R.id.mainFrameLayout);

        firebaseAuth= FirebaseAuth.getInstance();

        currentUser= firebaseAuth.getCurrentUser();

        if(currentUser != null) {

            databaseReference = FirebaseDatabase.getInstance().getReference().child("USERS").child(firebaseAuth.getCurrentUser().getUid());

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot != null) {

                        databaseReference.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser == null){

            setFragment(new SigninFragment());

        }else{

            databaseReference.child("online").setValue("true");

            Intent start = new Intent(this, MainActivity.class);
            startActivity(start);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(currentUser != null) {
            databaseReference.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void setFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(mainFrameLayout.getId(),fragment);
        fragmentTransaction.commit();

    }

}