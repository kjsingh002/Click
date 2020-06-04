package com.jrtech.click;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText mTextMessage;
    private TextView mTextMessageDisplay;
    private ScrollView mScrollView;
    private String currentDate,currentTime,userName,groupName;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        initializeFields();
        final Intent intent = getIntent();
        groupName = intent.getStringExtra("group");
        mToolbar.setTitle(groupName);
        setSupportActionBar(mToolbar);
        getUserInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.child("Groups").child(groupName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    retrieveGroupMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    retrieveGroupMessages(dataSnapshot);
                }
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

    private void getUserInfo() {
        mDatabase.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    userName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
        mToolbar = findViewById(R.id.group_chat_app_bar);
        mTextMessage = findViewById(R.id.group_chat_text_message);
        mTextMessageDisplay = findViewById(R.id.group_chat_text_display);
        mScrollView = findViewById(R.id.group_chat_scroll_view);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void sendMessage(View view){
        if (TextUtils.isEmpty(mTextMessage.getText())){}
        else {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = simpleDateFormat.format(calendar.getTime());
            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = simpleTimeFormat.format(calendar.getTime());
            HashMap<String,String> messageDetails = new HashMap<>();
            messageDetails.put("name",userName);
            messageDetails.put("message",mTextMessage.getText().toString());
            messageDetails.put("date",currentDate);
            messageDetails.put("time",currentTime);
            mDatabase.child("Groups").child(groupName).push().setValue(messageDetails);
        }
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        mTextMessage.setText("");
    }
    private void retrieveGroupMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String date = ((DataSnapshot) (iterator.next())).getValue().toString();
            String message = ((DataSnapshot)(iterator.next())).getValue().toString();
            String user = ((DataSnapshot) (iterator.next())).getValue().toString();
            String time = ((DataSnapshot)(iterator.next())).getValue().toString();
            mTextMessageDisplay.append(user + "\n"+ message + "\n" + date + " " + time + "\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
