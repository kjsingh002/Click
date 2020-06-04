package com.jrtech.click;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {
    private View chatFragmentView;
    private RecyclerView mRecycler;
    private DatabaseReference contactRef,mUserDatabase;
    private FirebaseAuth mAuth;
    private List<String> name = new ArrayList<>(),image = new ArrayList<>();
    final static String USER_ID = "ContactUserID",USER_NAME = "ContactUserName",USER_IMAGE_LINK = "ContactImageLink";

    public ChatsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        chatFragmentView = inflater.inflate(R.layout.fragment_chats, container, false);
        initializeFields();
        return chatFragmentView;
    }

    private void initializeFields() {
        mRecycler = chatFragmentView.findViewById(R.id.chat_fragment_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(mAuth.getUid());
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactRef,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,ChatViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder holder, final int position, @NonNull Contacts model) {
                final String userIds = getRef(position).getKey();
                mUserDatabase.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        name.add(dataSnapshot.child("name").getValue().toString());
                        if (dataSnapshot.hasChild("image")){
                            image.add(dataSnapshot.child("image").getValue().toString());
                            holder.mUserName.setText(dataSnapshot.child("name").getValue().toString());
                            holder.mUserStatus.setText(dataSnapshot.child("status").getValue().toString());
                            Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(holder.mProfileImage);
                        }else {
                            image.add("");
                            holder.mUserName.setText(dataSnapshot.child("name").getValue().toString());
                            holder.mUserStatus.setText(dataSnapshot.child("status").getValue().toString());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent intent = new Intent(getContext(),ChatActivity.class);
                        intent.putExtra(USER_ID,userIds);
                        intent.putExtra(USER_NAME,name.get(position));
                        intent.putExtra(USER_IMAGE_LINK,image.get(position));
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends_users_layout,parent,false);
                return new ChatViewHolder(view);
            }
        };
        mRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView mProfileImage;
        private TextView mUserName,mUserStatus;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            mProfileImage = itemView.findViewById(R.id.users_profile_image);
            mUserName = itemView.findViewById(R.id.users_name);
            mUserStatus = itemView.findViewById(R.id.users_status);
        }
    }
}
