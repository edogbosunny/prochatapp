package com.example.crystaldave.chatapp.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.crystaldave.chatapp.adapters.CustomPagerAdapter;
import com.example.crystaldave.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private CustomPagerAdapter mCustomPagerAdapter;
    private TabLayout mTabLayout;

    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()==null){
            sendToStart();
            return;
        }

        mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        mToolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("PRO CHAT");

        // tabs
        mViewPager = (ViewPager) findViewById(R.id.main_viewPager);
        mCustomPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mCustomPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabPager);
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_bubble_black_24dp);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_group_add_black_24dp);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_people_black_24dp);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.main_setting_btn:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.main_allUsers_btn:
                Intent allUsersIntent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(allUsersIntent);
                break;
            case R.id.main_logout_btn:
                FirebaseAuth.getInstance().signOut();
                sendToStart();
                break;
            default:
                return true;
        }
        return true;
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null)
            sendToStart();
        else
            mUserRef.child("online").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
    }
}
