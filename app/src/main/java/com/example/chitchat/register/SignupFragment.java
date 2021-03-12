package com.example.chitchat.register;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.chitchat.MainActivity;
import com.example.chitchat.R;
import com.example.chitchat.register.RegisterActivity;
import com.example.chitchat.register.SigninFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignupFragment extends Fragment {

    private FrameLayout mainFrameLayout;

    private EditText signupEmailEditText;
    private EditText signupPasswordEditText;
    private EditText signupNameEditText;
    private EditText usernameEditText;

    private ProgressBar signupProgressBar;

    private Button signupButton;

    private TextView haveAnAccountTextView;


    public SignupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        setupFragmentWidgets(view);

        signupEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                checkInput();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        signupNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                checkInput();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        signupPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                checkInput();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isEmailValid(signupEmailEditText.getText().toString())){

                    if(signupPasswordEditText.length()>=8){

                        signupProgressBar.setVisibility(View.VISIBLE);

                        signupButton.setEnabled(false);
                        signupButton.setTextColor(Color.argb(70,0,0,0));

                        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                         firebaseAuth.createUserWithEmailAndPassword(signupEmailEditText.getText().toString(),
                                signupPasswordEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){

                                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                    String userId = firebaseUser.getUid();

                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("USERS").child(userId);

                                    HashMap<String, Object> user = new HashMap<>();
                                    user.put("name", signupNameEditText.getText().toString());
                                    user.put("image" ,"default");
                                    user.put("thumb_image", "default-thumb");
                                    user.put("status", "Hi.. I am using chit chat");
                                    databaseReference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                Intent homeIntent = new Intent(getContext(), MainActivity.class);
                                                startActivity(homeIntent);
                                                getActivity().finish();

                                            }else{

                                                signupProgressBar.setVisibility(View.GONE);

                                                signupButton.setEnabled(true);
                                                signupButton.setTextColor(Color.rgb(0,0,0));

                                                String error = task.getException().getMessage();
                                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    });

                                }else{

                                    signupProgressBar.setVisibility(View.GONE);

                                    signupButton.setEnabled(true);
                                    signupButton.setTextColor(Color.rgb(0,0,0));

                                    String error = task.getException().getMessage();
                                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();

                                }

                            }
                        });
                    }else{

                        Toast.makeText(getContext(), "Password is too short", Toast.LENGTH_SHORT).show();

                    }

                }else{

                    Toast.makeText(getContext(), "E-mail is not valid", Toast.LENGTH_SHORT).show();

                }

            }
        });

        haveAnAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(new SigninFragment());
            }
        });

        return view;
    }

    private void setupFragmentWidgets(View view){

        mainFrameLayout = getActivity().findViewById(R.id.mainFrameLayout);

        signupEmailEditText = view.findViewById(R.id.signupEmailEditText);
        signupNameEditText = view.findViewById(R.id.signupNameEditText);
        signupPasswordEditText = view.findViewById(R.id.signupPasswordEditText);
        usernameEditText = view.findViewById(R.id.usernameEditText);

        signupProgressBar = view.findViewById(R.id.signupProgressBar);

        signupButton = view.findViewById(R.id.signupButton);

        haveAnAccountTextView = view.findViewById(R.id.haveAnAccountTextView);



    }

    private void checkInput(){

        if(!TextUtils.isEmpty(signupEmailEditText.getText())){

            if(!TextUtils.isEmpty(signupNameEditText.getText())){

                if(!TextUtils.isEmpty(signupPasswordEditText.getText())){

                    signupButton.setEnabled(true);
                    signupButton.setTextColor(Color.rgb(0,0,0));

                }else{

                    signupButton.setEnabled(false);
                    signupButton.setTextColor(Color.argb(70,0,0,0));

                }

            }else{

                signupButton.setEnabled(false);
                signupButton.setTextColor(Color.argb(70,0,0,0));

            }

        }else{

            signupButton.setEnabled(false);
            signupButton.setTextColor(Color.argb(70,0,0,0));

        }

    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void setFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(mainFrameLayout.getId(),fragment);
        fragmentTransaction.commit();

    }

}
