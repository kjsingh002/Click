package com.jrtech.click;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {
    private EditText profileName,profileStatus;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private StorageReference mStorage;
    private ImageView mProfileImage;
    final static int IMAGE_PROFILE_PICK = 1;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initializeFields();
        mToolbar.setTitle("Account Settings");
        setSupportActionBar(mToolbar);
        retrieveUserInfo();
    }

    private void initializeFields() {
        profileName = findViewById(R.id.settings_user_name);
        profileStatus = findViewById(R.id.settings_user_status);
        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        mProfileImage = findViewById(R.id.profile_image);
        mToolbar = findViewById(R.id.settings_appbar);
    }

    public void updateProfile(View view){
        if (TextUtils.isEmpty(profileName.getText()) && TextUtils.isEmpty(profileStatus.getText())){
            profileName.setError("Field Required");
            profileStatus.setError("Field Required");
        }else if (TextUtils.isEmpty(profileName.getText())){
            profileName.setError("Field Required");
        }else if (TextUtils.isEmpty(profileStatus.getText())){
            profileName.setError("Field Required");
        }else {
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",mAuth.getUid());
            profileMap.put("name",profileName.getText().toString());
            profileMap.put("status",profileStatus.getText().toString());
            mRootRef.child("Users").child(mAuth.getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        sendToMainActivity();
                    }else {
                        Toast.makeText(SettingsActivity.this, "Profile Updation Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void sendToMainActivity(){
        final Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void retrieveUserInfo(){
        mRootRef.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("status") && dataSnapshot.hasChild("name")){
                    profileName.setText(dataSnapshot.child("name").getValue().toString());
                    profileStatus.setText(dataSnapshot.child("status").getValue().toString());
                    if (dataSnapshot.hasChild("image")){
                        final String imageURL = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(imageURL).into(mProfileImage);
                    }
                }else {
                    profileStatus.setText("Hey I am using Click");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void updateProfileImage(View view){
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PROFILE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PROFILE_PICK && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                mStorage.child("ProfileImages").child(mAuth.getUid()+".jpg").putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                       taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                           @Override
                           public void onComplete(@NonNull Task<Uri> task) {
                               if (task.isSuccessful()){
                                   final String imageURL = task.getResult().toString();
                                   Picasso.get().load(imageURL).into(mProfileImage);
                                   mRootRef.child("Users").child(mAuth.getUid()).child("image").setValue(imageURL);
                               }
                           }
                       });
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}