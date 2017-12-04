package com.example.crystaldave.chatapp.activities;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.crystaldave.chatapp.adapters.MessageAdapter;
import com.example.crystaldave.chatapp.R;
import com.example.crystaldave.chatapp.helpers.TimeAgo;
import com.example.crystaldave.chatapp.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    private String mCrrUserId;
    private String mOtherUserId;
    private Toolbar mToolbar;
    private DatabaseReference mRootRef;

    private FirebaseAuth mAuth;
    private TextView mUsername;
    private TextView mLastSeen;
    private CircleImageView mProfileImage;

    private ImageView mChatAddBtn;
    private ImageView mChatSendBtn;
    private EditText mChatMessage;

    private RecyclerView mMessagesRecyclerV;

    private List<Message> mMessagesList;
    private MessageAdapter mAdapter;

    private final int MSGS_TO_LOAD = 10;
    private int crrPage = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mOtherUserId = getIntent().getStringExtra("userId");
        String username = getIntent().getStringExtra("username");

        mToolbar = (Toolbar) findViewById(R.id.single_chat_appbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);



        // Setting custom action bar
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_appbar, null);

        actionBar.setCustomView(action_bar_view);

        //


        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCrrUserId = mAuth.getCurrentUser().getUid();

        mUsername = (TextView) findViewById(R.id.chat_appbar_username);
        mLastSeen = (TextView) findViewById(R.id.chat_appbar_lastseen);
        mProfileImage = (CircleImageView) findViewById(R.id.single_chat_appbar_image);

        mChatAddBtn = (ImageButton) findViewById(R.id.single_chat_add);
        mChatSendBtn = (ImageButton) findViewById(R.id.single_chat_send);
        mChatMessage = (EditText) findViewById(R.id.single_chat_message);


        mMessagesList = new ArrayList<>();

        mAdapter = new MessageAdapter(mMessagesList);

        mMessagesRecyclerV = (RecyclerView) findViewById(R.id.single_chat_messages_list);
        mMessagesRecyclerV.setLayoutManager(new LinearLayoutManager(this));
        mMessagesRecyclerV.setHasFixedSize(true);
        mMessagesRecyclerV.setAdapter(mAdapter);

        mUsername.setText(username);
        loadMessages();

        mRootRef.child("users").child(mOtherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String lastSeen = dataSnapshot.child("online").getValue().toString();
                String imageUrl = dataSnapshot.child("image").getValue().toString();
                setUserImage(imageUrl);

                if(lastSeen.equals("true")){
                    mLastSeen.setText("Online");
                }else{
                    long time = Long.parseLong(lastSeen);
                    mLastSeen.setText(TimeAgo.getTimeAgo(time, getApplicationContext()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


//        mRootRef.child("chats").child(mCrrUserId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(!dataSnapshot.hasChild(mCrrUserId)){
//
//                    Map chatMap = new HashMap();
//                    chatMap.put("seen", false);
//                    chatMap.put("timestamp", ServerValue.TIMESTAMP);
//
//                    Map userMap = new HashMap();
//                    userMap.put("chats/" + mCrrUserId + "/" + mOtherUserId, chatMap);
//                    userMap.put("chats/" + mOtherUserId + "/" + mCrrUserId, chatMap);
//
//                    mRootRef.updateChildren(userMap, new DatabaseReference.CompletionListener(){
//                        @Override
//                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                            if(databaseError != null)
//                                Log.d("CHAT", databaseError.getMessage().toString());
//
//                        }
//                    });
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


        // Send Btn

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });


    }

    private void sendMessage() {
        String msg = mChatMessage.getText().toString();
        if(!TextUtils.isEmpty(msg)){
            mChatMessage.setText("");
            String crrUserRef = "messages/" + mCrrUserId + "/" + mOtherUserId;
            String otherUserRef = "messages/" + mOtherUserId + "/" + mCrrUserId;

            DatabaseReference userMessagePush = mRootRef
                    .child("messages").child(mCrrUserId).child(mOtherUserId).push();

            String pushId = userMessagePush.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", msg);
            messageMap.put("from", mCrrUserId);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);

            Map messageUserMap = new HashMap();
            messageUserMap.put(crrUserRef + "/" + pushId, messageMap);
            messageUserMap.put(otherUserRef + "/" + pushId, messageMap);

            // Updating chat part
            Map userMap = new HashMap();

            Map chatMap1 = new HashMap();
            chatMap1.put("seen", true);
            chatMap1.put("timestamp", ServerValue.TIMESTAMP);

            Map chatMap2 = new HashMap();
            chatMap2.put("seen", false);
            chatMap2.put("timestamp", ServerValue.TIMESTAMP);

            userMap.put("chats/" + mCrrUserId + "/" + mOtherUserId, chatMap1);
            userMap.put("chats/" + mOtherUserId + "/" + mCrrUserId, chatMap2);

            mRootRef.updateChildren(userMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null)
                        Log.d("CHAT", databaseError.getMessage().toString());
                }
            });

            /////////////////

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null)
                        Log.d("CHAT", databaseError.getMessage().toString());
                }
            });
            mAdapter.notifyDataSetChanged();
        }
    }

    private void loadMessages(){

        DatabaseReference messageRef = mRootRef.child("messages").child(mCrrUserId).child(mOtherUserId);

//        Query messageQuery = messageRef.limitToLast(MSGS_TO_LOAD * crrPage);

//        messageQuery.addChildEventListener(new ChildEventListener() {
            messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message m = dataSnapshot.getValue(Message.class);

                mMessagesList.add(m);
                mAdapter.notifyDataSetChanged();
                
                mMessagesRecyclerV.scrollToPosition(mAdapter.getItemCount()-1);


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setUserImage(String imageUrl){
        Picasso.with(ChatActivity.this).load(imageUrl).into(mProfileImage);
    }
}