package com.example.chitchat.settings.status_change;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.chitchat.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeStatusActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextInputLayout textInputLayout;
    private Button changeStatusButton;

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_status);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = currentUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("USERS").child(currentUid);

        String currentStatus = getIntent().getStringExtra("currentStatus");

        textInputLayout = findViewById(R.id.textInputLayout);
        changeStatusButton = findViewById(R.id.changeStatusButton);



        textInputLayout.getEditText().setText(currentStatus);

        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String changedStatus = textInputLayout.getEditText().getText().toString();

                databaseReference.child("status").setValue(changedStatus);

                Toast.makeText(ChangeStatusActivity.this, "Status changed", Toast.LENGTH_SHORT).show();

            }
        });

    }
}