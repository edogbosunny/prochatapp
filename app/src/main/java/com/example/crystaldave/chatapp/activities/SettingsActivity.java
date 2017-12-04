package com.example.crystaldave.chatapp.activities;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crystaldave.chatapp.R;
import com.example.crystaldave.chatapp.fragments.UpdateStatusDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseReference mUserDb;
    private FirebaseUser mCrrUser;
    private ScrollView mScrollView;
    private RelativeLayout mLoadingPanel;

    private ImageView mImage;
    private TextView mUsername;
    private TextView mStatus;
    private ImageButton mStatusEditBtn;
//    private Button mChangeStatusBtn;
    private FloatingActionButton mChangeImageBtn;
    private ProgressDialog mProgress;

    private static final int GALLERY_PICK = 2;

    // Storage Firebase
    private StorageReference mImagesStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mImage = (ImageView) findViewById(R.id.settings_image);
        mUsername = (TextView) findViewById(R.id.settings_username);
        mStatusEditBtn = (ImageButton) findViewById(R.id.settings_edit);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mLoadingPanel = (RelativeLayout) findViewById(R.id.settings_loadingPanel);

//        mChangeStatusBtn = (Button) findViewById(R.id.settings_changeStatus);
        mChangeImageBtn = (FloatingActionButton) findViewById(R.id.settings_changeImg);
        mScrollView = (ScrollView) findViewById(R.id.settings_scrollview);

        // Firebase
        mCrrUser = FirebaseAuth.getInstance().getCurrentUser();
        mImagesStorage = FirebaseStorage.getInstance().getReference();

        final String crrUid = mCrrUser.getUid();
        mUserDb = FirebaseDatabase.getInstance().getReference().child("users").child(crrUid);

        mUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                String username = dataSnapshot.child("username").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
//                String thumb_img = dataSnapshot.child("thumb_img").getValue().toString();

                mUsername.setText(username);
                mStatus.setText(status);

                // Setting the loading panel
                if( !image.equals("default") )
                    Picasso.with(SettingsActivity.this).load(image).into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            mLoadingPanel.setVisibility(View.GONE);
                            mScrollView.setVisibility(View.VISIBLE);
                            mScrollView.scrollTo(0, mImage.getHeight()/2);
                        }

                        @Override
                        public void onError() {
                            mLoadingPanel.setVisibility(View.GONE);
                            mScrollView.setVisibility(View.VISIBLE);
                        }
                    });
                else{
                    mLoadingPanel.setVisibility(View.GONE);
                    mScrollView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChangeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Intent galleryIntent = new Intent();
//                galleryIntent.setType("image/*");
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1)
                        .setMinCropResultSize(500, 500)
                        .start(SettingsActivity.this);

            }
        });


        // Update the current online by opening a Dialog fragment
        mStatusEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String statusString = mStatus.getText().toString();
                Bundle bundle = new Bundle();
                bundle.putString("crrStatus", statusString);
                bundle.putString("uid", crrUid);
                DialogFragment statusDialog = new UpdateStatusDialogFragment();
                statusDialog.setArguments(bundle);
                statusDialog.show(getFragmentManager(), "online");

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && requestCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setAspectRatio(1, 1).start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgress = new ProgressDialog(this);
                mProgress.setTitle("Uploading image");
                mProgress.setMessage("Please wait for the uploading process..");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                Uri resultUri = result.getUri();

                File thumbFilePath = new File(resultUri.getPath());

                Bitmap thumbBitmap = null;
                try {
                    thumbBitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumbFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumbBytes = baos.toByteArray();

                //firebase storage

                StorageReference imagePath = mImagesStorage.child("profile_images").child(mCrrUser.getUid()+"jpg");
                final StorageReference thumbPath = mImagesStorage.child("profile_images").child("thumbs").child(mCrrUser.getUid()+"jpg");

                imagePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            final String down_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumbPath.putBytes(thumbBytes);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {

                                    String thumbDownUrl = thumbTask.getResult().getDownloadUrl().toString();

                                    if(thumbTask.isSuccessful()){
                                        Map data= new HashMap();
                                        data.put("image", down_url);
                                        data.put("thumb_img", thumbDownUrl);

                                        mUserDb.updateChildren(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    mProgress.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Image uploaded successfully ;)", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    mProgress.hide();
                                                    Toast.makeText(SettingsActivity.this, "error during uploading..:(", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }else{
                            mProgress.hide();
                            Toast.makeText(SettingsActivity.this, "error during uploading..:(", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserDb.child("online").setValue(true);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mUserDb.child("online").setValue(ServerValue.TIMESTAMP);
    }
}
