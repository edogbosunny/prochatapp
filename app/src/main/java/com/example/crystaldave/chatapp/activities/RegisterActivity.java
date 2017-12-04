package com.example.crystaldave.chatapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.crystaldave.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Button mCreateBtn;
    private TextInputLayout mUsername;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ProgressDialog mRegProgress;
    private DatabaseReference mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Toolbar Set
        mToolbar = (Toolbar) findViewById(R.id.reg_activity_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Progress bar
        mRegProgress = new ProgressDialog(this);

        mCreateBtn = (Button) findViewById(R.id.reg_create_acc);
        mUsername = (TextInputLayout) findViewById(R.id.reg_display_name);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = mUsername.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(username) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait..");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    registerUser(username, email, password);
                }
            }
        });
    }
    private void registerUser(final String username, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){


                    FirebaseUser crrUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = crrUser.getUid();

                    mDb = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("image", "default");
                    userMap.put("username", username);
                    userMap.put("online", "Hi there, I'm using Propius Chat App...;)");
                    userMap.put("thumb_img", "default");

                    mDb.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRegProgress.dismiss();

                            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });
                }else{

                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this, "Cannot sign in.. :(", Toast.LENGTH_LONG).show();

                }
            }

        });

    }
}