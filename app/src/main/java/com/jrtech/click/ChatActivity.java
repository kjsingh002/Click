package com.jrtech.click;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText mMessage;
    private String userID, userName, userImage;
    private CircleImageView mImage;
    private TextView userTitle;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private List<Messages> messagesList = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private RecyclerView mRecycler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        final Intent intent = getIntent();
        userName = intent.getStringExtra(ChatsFragment.USER_NAME);
        userID = intent.getStringExtra(ChatsFragment.USER_ID);
        userImage = intent.getStringExtra(ChatsFragment.USER_IMAGE_LINK);
        initializeFields();
    }

    private void initializeFields() {
        mMessage = findViewById(R.id.chat_activity_message);
        mToolbar = findViewById(R.id.chat_activity_appbar);
        setSupportActionBar(mToolbar);
        userTitle = findViewById(R.id.appbar_user_name);
        userTitle.setText(userName);
        mImage = findViewById(R.id.chat_activity_image);
        Picasso.get().load(userImage).placeholder(R.drawable.ic_person_24dp).into(mImage);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Messages");
        mRecycler = findViewById(R.id.chat_activity_recycler);
        messageAdapter = new MessageAdapter(messagesList);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(messageAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.child(mAuth.getUid()).child(userID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                mRecycler.smoothScrollToPosition(mRecycler.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void sendPrivateMessages(View view){
        if (TextUtils.isEmpty(mMessage.getText().toString().trim())){}
        else {
            final String messageKey = mDatabase.push().getKey();
            final String message = mMessage.getText().toString().trim();
            final HashMap<String,String> messageDetails = new HashMap<>();
            messageDetails.put("message",message);
            messageDetails.put("from",mAuth.getUid());
            messageDetails.put("type","text");
            mDatabase.child(mAuth.getUid()).child(userID).child(messageKey).setValue(messageDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        mDatabase.child(userID).child(mAuth.getUid()).child(messageKey).setValue(messageDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
        mMessage.setText("");
    }
}