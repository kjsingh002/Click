package com.jrtech.click;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private View mContactsFragmentView;
    private DatabaseReference mContactsDatabase,mUserDatabase;
    private FirebaseAuth mAuth;

    public ContactsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContactsFragmentView = inflater.inflate(R.layout.fragment_contacts, container, false);
        initializeFields();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return mContactsFragmentView;
    }

    private void initializeFields() {
        mRecyclerView = mContactsFragmentView.findViewById(R.id.contacts_fragment_recycler);
        mAuth = FirebaseAuth.getInstance();
        mContactsDatabase = FirebaseDatabase.getInstance().getReference().child("Contacts").child(mAuth.getUid());
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(mContactsDatabase,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull final Contacts model) {
                String userIds = getRef(position).getKey();
                mUserDatabase.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("image")){
                            holder.mUserName.setText(dataSnapshot.child("name").getValue().toString());
                            holder.mUserStatus.setText(dataSnapshot.child("status").getValue().toString());
                            Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(holder.mProfileImage);
                        }else {
                            holder.mUserName.setText(dataSnapshot.child("name").getValue().toString());
                            holder.mUserStatus.setText(dataSnapshot.child("status").getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends_users_layout,parent,false);
                return new ContactsViewHolder(view);
            }
        };
        mRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView mProfileImage;
        private TextView mUserName,mUserStatus;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            mProfileImage = itemView.findViewById(R.id.users_profile_image);
            mUserName = itemView.findViewById(R.id.users_name);
            mUserStatus = itemView.findViewById(R.id.users_status);
        }
    }
}
