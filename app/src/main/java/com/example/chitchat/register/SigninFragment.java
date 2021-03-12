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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 */
public class SigninFragment extends Fragment {

    private FrameLayout mainFrameLayout;


    private EditText signinEmailEditText;
    private EditText signinPasswordEditText;
    private Button signinButton;

    private ProgressBar signinProgressBar;

    private TextView dontHaveAccount;
    private TextView forgotPasswordTextView;

    private FirebaseAuth firebaseAuth;

    public SigninFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view= inflater.inflate(R.layout.fragment_signin, container, false);

        setupFragmentWidgets(view);

        signinEmailEditText.addTextChangedListener(new TextWatcher() {
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

        signinPasswordEditText.addTextChangedListener(new TextWatcher() {
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

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isEmailValid(signinEmailEditText.getText().toString())){

                    if(signinPasswordEditText.length()>=8){

                        signinProgressBar.setVisibility(View.VISIBLE);

                        signinButton.setEnabled(false);
                        signinButton.setTextColor(Color.argb(70,0,0,0));

                        firebaseAuth.signInWithEmailAndPassword(signinEmailEditText.getText().toString(),
                                signinPasswordEditText.getText().toString()).
                                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if(task.isSuccessful()){

                                            Intent intent=new Intent(view.getContext(), MainActivity.class);
                                            startActivity(intent);
                                            getActivity().finish();

                                        }else{

                                            String error=task.getException().getMessage();
                                            Toast.makeText(view.getContext(), error, Toast.LENGTH_SHORT).show();
                                            signinProgressBar.setVisibility(View.INVISIBLE);
                                            signinButton.setEnabled(true);
                                            signinButton.setTextColor(Color.rgb(0,0,0));


                                        }

                                    }
                                });

                    }else{

                        Toast.makeText(view.getContext(), "E-mail or password is incorrect", Toast.LENGTH_SHORT).show();

                    }
                }else{

                    Toast.makeText(view.getContext(), "E-mail or pass word is incorrect", Toast.LENGTH_SHORT).show();

                }
            }
        });

        dontHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(new SignupFragment());
            }
        });

        return view;
    }

    private void setupFragmentWidgets(View view){


        mainFrameLayout=getActivity().findViewById(R.id.mainFrameLayout);

        signinEmailEditText = view.findViewById(R.id.signinEmailEditText);
        signinPasswordEditText = view.findViewById(R.id.signinPasswordEditText);

        signinButton = view.findViewById(R.id.signinButton);
        firebaseAuth = FirebaseAuth.getInstance();

        dontHaveAccount = view.findViewById(R.id.dontHaveAccountTextView);
        forgotPasswordTextView = view.findViewById(R.id.forgotPasswordTextView);

        signinProgressBar = view.findViewById(R.id.signInProgressBar);

    }

    private void checkInput(){

        if(!TextUtils.isEmpty(signinEmailEditText.getText())){

            if(!TextUtils.isEmpty(signinPasswordEditText.getText())){

                signinButton.setEnabled(true);
                signinButton.setTextColor(Color.rgb(0,0,0));


            }else{

                signinButton.setEnabled(false);
                signinButton.setTextColor(Color.argb(70,0,0,0));

            }

        }else{

            signinButton.setEnabled(false);
            signinButton.setTextColor(Color.argb(70,0,0,0));

        }

    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void setFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction=getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(mainFrameLayout.getId(),fragment);
        fragmentTransaction.commit();

    }
}
