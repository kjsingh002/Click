package com.jrtech.click;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabase;
    final static String USERID = "userid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        initializeFields();
        mToolbar.setTitle("Click");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initializeFields() {
        mToolbar = findViewById(R.id.find_friends_activity_appbar);
        mRecyclerView = findViewById(R.id.find_friends_recycler);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(mDatabase,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull Contacts model) {
                holder.mUsersName.setText(model.getName());
                holder.mUsersStatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_person_24dp).into(holder.mUsersProfileImage);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent intent = new Intent(FindFriendsActivity.this,ProfileActivity.class);
                        intent.putExtra(USERID,getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends_users_layout,parent,false);
                return new FindFriendsViewHolder(view);
            }
        };
        mRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        private TextView mUsersName,mUsersStatus;
        private CircleImageView mUsersProfileImage,mUsersOnlineStatus;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mUsersName = itemView.findViewById(R.id.users_name);
            mUsersStatus = itemView.findViewById(R.id.users_status);
            mUsersProfileImage = itemView.findViewById(R.id.users_profile_image);
            mUsersOnlineStatus = itemView.findViewById(R.id.users_online_offline);
        }
    }
}