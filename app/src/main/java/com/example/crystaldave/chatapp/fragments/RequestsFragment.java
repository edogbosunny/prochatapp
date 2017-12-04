package com.example.crystaldave.chatapp.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crystaldave.chatapp.R;
import com.example.crystaldave.chatapp.models.Request;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View mMainView;
    private RecyclerView mRequestsRecyclerV;
    private FirebaseAuth mAuth;
    private String mCrrUserId;
    private DatabaseReference mRequestsDb;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsDb;
    private DatabaseReference mRootDb;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestsRecyclerV = (RecyclerView) mMainView.findViewById(R.id.request_recyclerView);
        mAuth = FirebaseAuth.getInstance();

        mCrrUserId = mAuth.getCurrentUser().getUid();

        mRequestsDb = FirebaseDatabase.getInstance().getReference().child("friends_req").child(mCrrUserId).child("received");

        mFriendsDb = FirebaseDatabase.getInstance().getReference().child("friends");
//        mRequestsDb.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
//        mUsersDatabase.keepSynced(true);

        mRootDb = FirebaseDatabase.getInstance().getReference();

        mRequestsRecyclerV.setHasFixedSize(true);
        mRequestsRecyclerV.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<Request, RequestViewHolder> RequestsRecyclerViewAdapter =
                new FirebaseRecyclerAdapter<Request, RequestViewHolder>(
                        Request.class,
                        R.layout.request_single_layout,
                        RequestViewHolder.class,
                        mRequestsDb

                ) {
                    @Override
                    protected void populateViewHolder(final RequestViewHolder requestViewHolder, Request req, int i) {
                        final RequestViewHolder ViewHolder = requestViewHolder;

                        final String mCrrUserInList = getRef(i).getKey();

                        mUsersDatabase.child(mCrrUserInList).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String username = dataSnapshot.child("username").getValue().toString();
                                String thumbImg = dataSnapshot.child("thumb_img").getValue().toString();
                                requestViewHolder.setUsername(username);
                                requestViewHolder.setImage(thumbImg, getContext());

                                // accept btn
                                // create the friendship
                                // delete the reqs
                                requestViewHolder.mAcceptBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Map friendsMap = new HashMap();

                                        friendsMap.put(mCrrUserId + "/" + mCrrUserInList + "/date", ServerValue.TIMESTAMP);
                                        friendsMap.put(mCrrUserInList + "/" + mCrrUserId + "/date", ServerValue.TIMESTAMP);


                                        mFriendsDb.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if(databaseError != null)
                                                    Log.d("CHAT", databaseError.getMessage().toString());

                                                mRootDb.child("friends_req").child(mCrrUserId).child("received")
                                                        .child(mCrrUserInList).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mRootDb.child("friends_req").child(mCrrUserInList).child("sent")
                                                                .child(mCrrUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(getActivity(), "You are now friends", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });

                                // decline btn
                                // remove reqs from both sides
                                requestViewHolder.mDeclineBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mRequestsDb.child(mCrrUserInList).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mRootDb.child("friends_req").child(mCrrUserInList).child("sent")
                                                        .child(mCrrUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });
                                            }
                                        });

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                };
        mRequestsRecyclerV.setAdapter(RequestsRecyclerViewAdapter);


    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircleImageView mImage;
        TextView mUsername;
        ImageButton mAcceptBtn;
        ImageButton mDeclineBtn;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mImage = (CircleImageView) mView.findViewById(R.id.request_single_image);
            mUsername = (TextView) mView.findViewById(R.id.request_single_text);
            mAcceptBtn = (ImageButton) mView.findViewById(R.id.request_single_done);
            mDeclineBtn = (ImageButton) mView.findViewById(R.id.request_single_clear);


        }


        public void setUsername(String name) {
            mUsername.setText(name);
        }

        public void setImage(String url, Context cnx){
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.request_single_image);
            Picasso.with(cnx).load(url).placeholder(R.drawable.user2).into(userImageView);
        }
    }
}
