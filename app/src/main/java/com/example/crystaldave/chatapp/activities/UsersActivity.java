package com.example.crystaldave.chatapp.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.crystaldave.chatapp.R;
import com.example.crystaldave.chatapp.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDb;

    private DatabaseReference mCrrUserDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = (Toolbar) findViewById(R.id.users_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDb = FirebaseDatabase.getInstance().getReference().child("users");

        mCrrUserDb = mUsersDb.child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        mCrrUserDb.child("online").setValue(true);

        FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<User, UsersViewHolder>(
                        User.class,
                        R.layout.users_single_layout,
                        UsersViewHolder.class,
                        mUsersDb
                ) {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, User users, int position) {
                usersViewHolder.setInfo(users.getUsername(), users.getStatus());
                usersViewHolder.setUserImage(users.getThumb_img(), getApplicationContext());

                final String userId = getRef(position).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("userId", userId);
                        startActivity(profileIntent);
                    }
                });
            }


        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setInfo(String username, String status) {
            TextView usernameView = (TextView) mView.findViewById(R.id.user_single_username);
            usernameView.setText(username);
        }

        public void setUserImage(String thumb_image, Context context){
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.user2).into(userImageView);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCrrUserDb.child("online").setValue(ServerValue.TIMESTAMP);
    }
}