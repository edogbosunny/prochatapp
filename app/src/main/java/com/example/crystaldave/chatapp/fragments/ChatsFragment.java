package com.example.crystaldave.chatapp.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.crystaldave.chatapp.R;
import com.example.crystaldave.chatapp.activities.ChatActivity;
import com.example.crystaldave.chatapp.models.Chat;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    RecyclerView mChatsRecyclerV;
    private View mMainView;
    private FirebaseAuth mAuth;
    private DatabaseReference mChatsDb;
    private DatabaseReference mUsersDb;
    private DatabaseReference mMessagesDb;
    private String mCrrUserId;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mChatsRecyclerV = (RecyclerView) mMainView.findViewById(R.id.chat_recyclerView) ;

        mAuth = FirebaseAuth.getInstance();
        mCrrUserId = mAuth.getCurrentUser().getUid();

        mChatsDb = FirebaseDatabase.getInstance().getReference().child("chats").child(mCrrUserId);
        mChatsDb.keepSynced(true);
        mUsersDb = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDb.keepSynced(true);
        mMessagesDb = FirebaseDatabase.getInstance().getReference().child("messages").child(mCrrUserId);
        mMessagesDb.keepSynced(true);

        mChatsRecyclerV.setHasFixedSize(true);
        mChatsRecyclerV.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Chat, ChatViewHolder> chatsRecyclerVAdapter =
                new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(
                        Chat.class,
                        R.layout.chat_single_layout,
                        ChatViewHolder.class,
                        mChatsDb

                ) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, Chat model, int i) {
                final String crrUserInList = getRef(i).getKey();

                mChatsDb.child(crrUserInList).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        mUsersDb.child(crrUserInList).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String username = dataSnapshot.child("username").getValue().toString();
                                String thumbImg = dataSnapshot.child("thumb_img").getValue().toString();
                                String online = dataSnapshot.child("online").getValue().toString();

                                viewHolder.setName(username);
                                viewHolder.setUserOnline(online);
                                viewHolder.setUserImage(thumbImg, getContext());

                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                        chatIntent.putExtra("userId", crrUserInList);
                                        chatIntent.putExtra("username", username);
                                        startActivity(chatIntent);
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        Query messageQuery = mMessagesDb.child(crrUserInList).limitToLast(1);
                        messageQuery.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                String lastMessage = dataSnapshot.child("message").getValue().toString();
                                if(lastMessage.length() > 15)
                                    lastMessage = lastMessage.substring(0, 15) + "...";
                                viewHolder.setLastMessage(lastMessage);
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

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };
        mChatsRecyclerV.setAdapter(chatsRecyclerVAdapter);

    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }


        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.chat_single_username);
            userNameView.setText(name);
        }

        public void setLastMessage(String msg){
            TextView lastMessage = (TextView) mView.findViewById(R.id.chat_single_last_msg);
            lastMessage.setText(msg);
        }

        public void setUserImage(String thumb_image, Context context){
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.chat_single_image);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.user2).into(userImageView);
        }

        public void setUserOnline(String online_status) {
            ImageView onlineBtn = (ImageView) mView.findViewById(R.id.chat_single_online);
            if(online_status.equals("true"))
                onlineBtn.setVisibility(View.VISIBLE);
            else
                onlineBtn.setVisibility(View.INVISIBLE);
        }
    }
}
