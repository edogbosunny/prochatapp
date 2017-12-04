package com.example.crystaldave.chatapp.activities;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crystaldave.chatapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private ImageView mProfileImage;
    private TextView mProfileUsername;
    private TextView mProfilestatus;
    private Button mProfileSendReqBtn;
    private Button mProfileDeclineReq;

    private DatabaseReference mUserDb;
    private DatabaseReference mCrrUserDb;
    private DatabaseReference mFriendReqDb;
    private DatabaseReference mFriendsDb;
    private DatabaseReference mNotifDb;
    private String mCrrUserId;

    private ProgressDialog mProgress;
    private String mCrrState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileUsername = (TextView) findViewById(R.id.profile_username);
        mProfilestatus = (TextView) findViewById(R.id.profile_status);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_sendReq);
        mProfileDeclineReq = (Button) findViewById(R.id.profile_declineReq);

        mCrrState = "not_friends";

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading profile");
        mProgress.setMessage("Please wait..");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        // Firebase
        final String userId = getIntent().getStringExtra("userId");

        mUserDb = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        mCrrUserDb = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mFriendReqDb = FirebaseDatabase.getInstance().getReference().child("friends_req");
        mFriendsDb = FirebaseDatabase.getInstance().getReference().child("friends");
        mNotifDb = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCrrUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        mUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue().toString();
                String status = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileUsername.setText(username);
                mProfilestatus.setText(status);

                Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.user2).into(mProfileImage);

                // Update friends Reqs

                mFriendReqDb.child(mCrrUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("received").hasChild(userId)){
                            mCrrState = "req_received";
                            mProfileSendReqBtn.setText("Accept Friend Request");
                            mProfileDeclineReq.setVisibility(View.VISIBLE);
                            mProfileDeclineReq.setEnabled(true);
                        }else if(dataSnapshot.child("sent").hasChild(userId)){
                            mCrrState = "req_sent";
                            mProfileSendReqBtn.setText("Cancel Friend Request");
                            mProfileDeclineReq.setVisibility(View.INVISIBLE);
                            mProfileDeclineReq.setEnabled(false);
                        }else{
                            mFriendsDb.child(mCrrUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(userId)){
                                        mCrrState = "friends";
                                        mProfileSendReqBtn.setText("Unfriend");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        mProgress.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProfileSendReqBtn.setEnabled(false);

                // Not Friends Part

                if(mCrrState.equals("not_friends")){

                    DatabaseReference ref = mFriendReqDb.child(mCrrUserId).child("sent").child(userId);

                    Map reqMap = new HashMap();
                    reqMap.put("date", ServerValue.TIMESTAMP);

                    ref.updateChildren(reqMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            Map reqMap = new HashMap();
                            reqMap.put("date", ServerValue.TIMESTAMP);

                            mFriendReqDb.child(userId).child("received").child(mCrrUserId)
                                    .updateChildren(reqMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if(databaseError==null)
                                        Toast.makeText(ProfileActivity.this, "Request sent successfully.;)", Toast.LENGTH_SHORT).show();
                                    else{
                                        Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                                        mProfileSendReqBtn.setEnabled(true);
                                        mCrrState = "req_sent";
                                    }
                                }

                            });
                        }
                    });

                }

                // Cancel Request State

                if(mCrrState.equals("req_sent")){
                    mFriendReqDb.child(mCrrUserId).child("sent").child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDb.child(userId).child("received").child(mCrrUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCrrState = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                }
                            });
                        }
                    });
                }

                // Req received state

                if(mCrrState.equals(("req_received"))){
                    mFriendsDb.child(mCrrUserId).child(userId).child("date").setValue(ServerValue.TIMESTAMP)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendsDb.child(userId).child(mCrrUserId).child("date").setValue(ServerValue.TIMESTAMP);

                            mFriendReqDb.child(mCrrUserId).child("sent").child(userId).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendReqDb.child(userId).child("received").child(mCrrUserId).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mProfileSendReqBtn.setEnabled(true);
                                            mCrrState = "friends";
                                            mProfileSendReqBtn.setText("unFriend this person");

                                            mProfileDeclineReq.setVisibility(View.INVISIBLE);
                                            mProfileDeclineReq.setEnabled(false);
                                        }
                                    });
                                }
                            });

                        }
                    });
                }
            }
        });

        mProfileDeclineReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCrrState.equals(("req_sent"))){

                    mProfileDeclineReq.setEnabled(false);

                    mFriendReqDb.child(mCrrUserId).child("sent").child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mCrrState = "not_friends";
                            mProfileSendReqBtn.setEnabled(true);
                            mProfileDeclineReq.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        mCrrUserDb.child("online").setValue(true);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mCrrUserDb.child("online").setValue(ServerValue.TIMESTAMP);
    }
}
