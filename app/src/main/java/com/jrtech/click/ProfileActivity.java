package com.jrtech.click;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private CircleImageView mProfileImage;
    private TextView mUserName, mUserStatus;
    private DatabaseReference mDatabase,mNotification;
    private String uid, mRequestType;
    private Button mSendMessage, mCancelRequest;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final Intent intent = getIntent();
        uid = intent.getStringExtra(FindFriendsActivity.USERID);
        initializeFields();
        manageUserInfo();
    }

    private void manageUserInfo() {
        if (uid.equals(mAuth.getUid())) {
            mSendMessage.setVisibility(View.INVISIBLE);
        } else {
            mSendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSendMessage.getText().equals("Send Message")) {
                        sendMessageRequest();
                    } else if (mSendMessage.getText().equals("Cancel Request")) {
                        cancelRequest();
                    } else if (mSendMessage.getText().equals("Remove Contact")) {
                        removeContact();
                    } else {
                        acceptRequest();
                    }
                }
            });
            mCancelRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelRequest();
                }
            });
        }
    }

    private void removeContact() {
        mDatabase.child("Contacts").child(mAuth.getUid()).child(uid).child("Contacts").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mDatabase.child("Contacts").child(uid).child(mAuth.getUid()).child("Contacts").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mSendMessage.setText("Send Message");
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendMessageRequest() {
        mDatabase.child("ChatRequests").child(mAuth.getUid()).child(uid).child("RequestType").setValue("RequestSent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mDatabase.child("ChatRequests").child(uid).child(mAuth.getUid()).child("RequestType").setValue("RequestReceived").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mSendMessage.setText("Cancel Request");
                                HashMap<String,String> notification = new HashMap<>();
                                notification.put("from",mAuth.getUid());
                                notification.put("type","request");
                                mNotification.child(uid).push().setValue(notification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(ProfileActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void cancelRequest() {
        mDatabase.child("ChatRequests").child(uid).child(mAuth.getUid()).child("RequestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mDatabase.child("ChatRequests").child(mAuth.getUid()).child(uid).child("RequestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mSendMessage.setText("Send Message");
                                mCancelRequest.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void deleteRequest() {
        mDatabase.child("ChatRequests").child(uid).child(mAuth.getUid()).child("RequestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mDatabase.child("ChatRequests").child(mAuth.getUid()).child(uid).child("RequestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mSendMessage.setText("Remove Contact");
                                mCancelRequest.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptRequest() {
        mDatabase.child("Contacts").child(mAuth.getUid()).child(uid).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
           if (task.isSuccessful()){
               mDatabase.child("Contacts").child(uid).child(mAuth.getUid()).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful()){
                           deleteRequest();
                       }
                   }
               });
           }
            }
        });
    }

    private void initializeFields() {
        mProfileImage = findViewById(R.id.profile_activity_profile_image);
        mUserName = findViewById(R.id.profile_activity_profile_name);
        mUserStatus = findViewById(R.id.profile_activity_profile_status);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mSendMessage = findViewById(R.id.profile_activity_send_message);
        mCancelRequest = findViewById(R.id.profile_activity_cancel_request);
        mNotification = FirebaseDatabase.getInstance().getReference().child("Notifications");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("status") && dataSnapshot.hasChild("name")) {
                    mUserName.setText(dataSnapshot.child("name").getValue().toString());
                    mUserStatus.setText(dataSnapshot.child("status").getValue().toString());
                    if (dataSnapshot.hasChild("image")) {
                        final String imageURL = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(imageURL).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabase.child("ChatRequests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mAuth.getUid())) {
                    if (dataSnapshot.child(mAuth.getUid()).hasChild(uid)) {
                        mRequestType = dataSnapshot.child(mAuth.getUid()).child(uid).child("RequestType").getValue().toString();
                        if (mRequestType.equals("RequestSent")) {
                            mSendMessage.setText("Cancel Request");
                        } else if (mRequestType.equals("RequestReceived")) {
                            mSendMessage.setText("Accept Request");
                            mCancelRequest.setVisibility(View.VISIBLE);
                        }
                    }
                }else {
                    mSendMessage.setText("Send Message");
                    mCancelRequest.setVisibility(View.INVISIBLE);
                    mDatabase.child("Contacts").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                if (dataSnapshot.hasChild(mAuth.getUid())){
                                    if (dataSnapshot.child(mAuth.getUid()).hasChild(uid)){
                                        mDatabase.child("Contacts").child(mAuth.getUid()).child(uid).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()){
                                                    String friends = dataSnapshot.child("Contacts").getValue().toString();
                                                    mSendMessage.setText("Remove Contact");
                                                }else {
                                                    mSendMessage.setText("Send Message");
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
