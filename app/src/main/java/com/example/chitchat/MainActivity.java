package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.chitchat.chat.ChatFragment;
import com.example.chitchat.friends_frament.FriendsFragment;
import com.example.chitchat.register.RegisterActivity;
import com.example.chitchat.request_fragment.RequestFragment;
import com.example.chitchat.settings.SettingActivity;
import com.example.chitchat.settings.all_users.AllUsersActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.model.ServerTimestamps;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViewPAger();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("USERS").child(firebaseAuth.getCurrentUser().getUid());


    }

    private void initViewPAger(){

       viewPager = findViewById(R.id.viewPager);
       tabLayout = findViewById(R.id.tabLayout);

        ViewPAgerAdapter viewPAgerAdapter = new ViewPAgerAdapter(getSupportFragmentManager());
        viewPAgerAdapter.addFragment(new RequestFragment(), "Requeets");
        viewPAgerAdapter.addFragment(new ChatFragment(), "Chats");
        viewPAgerAdapter.addFragment(new FriendsFragment(), "Friends");

        viewPager.setAdapter(viewPAgerAdapter);

        tabLayout.setupWithViewPager(viewPager);


    }

    public static class ViewPAgerAdapter extends FragmentPagerAdapter{

        private List<Fragment> fragments;
        private List<String> fragmentTitle;

        public ViewPAgerAdapter(@NonNull FragmentManager fm) {
            super(fm);

            fragments = new ArrayList<>();
            fragmentTitle = new ArrayList<>();

        }

        private void addFragment(Fragment fragment, String title){

            fragments.add(fragment);
            fragmentTitle.add(title);

        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitle.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseReference.child("online").setValue("true");

    }

    @Override
    protected void onStop() {
        super.onStop();

        databaseReference.child("online").setValue(ServerValue.TIMESTAMP);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id= item.getItemId();
        switch (id){

            case R.id.setting :

                Intent settingsIntent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(settingsIntent);
                break;

            case R.id.allUsers :
                Intent userIntent = new Intent(getApplicationContext(), AllUsersActivity.class);
                startActivity(userIntent);
                break;

            case R.id.logOut :
                logOutUser();
                break;

            default :
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();

    }

    private void logOutUser(){

        FirebaseAuth.getInstance().signOut();
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);

    }


}