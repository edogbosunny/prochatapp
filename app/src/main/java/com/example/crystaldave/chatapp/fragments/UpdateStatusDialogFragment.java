package com.example.crystaldave.chatapp.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.crystaldave.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by root on 9/22/17.
 */

public class UpdateStatusDialogFragment extends DialogFragment {
    private View v;
    private ProgressDialog mProgress;
    private DatabaseReference mStatusDb;


    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        v = inflater.inflate(R.layout.update_status_dialog, null);

        final Bundle b = getArguments();
        String crrStatus = b.getString("crrStatus");

        final EditText statusET = (EditText) v.findViewById(R.id.update_status_status);
        statusET.setText(crrStatus);

        builder.setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String uid = b.getString("uid");
                        mStatusDb = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

                        // progress Dlg
                        mProgress = new ProgressDialog(getActivity());
                        mProgress.setTitle("Saving changes");
                        mProgress.setMessage("Please wait while saving the changes..");
                        mProgress.show();

                        String status = statusET.getText().toString();
                        mStatusDb.child("online").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    mProgress.dismiss();
                                }else{
                                    Toast.makeText(getActivity(), "There was an error while saving changes :(", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }


}
