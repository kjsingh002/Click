package com.jrtech.click;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment {
    private View requestFragmentView;
    private RecyclerView mRecyclerView;
    private DatabaseReference requestsDatabase,mUserDatabase,mDatabase;
    private FirebaseAuth mAuth;

    public RequestFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);
        initializeFields();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return requestFragmentView;
    }

    private void initializeFields() {
        mRecyclerView = requestFragmentView.findViewById(R.id.request_fragment_list);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        requestsDatabase = FirebaseDatabase.getInstance().getReference().child("ChatRequests").child(mAuth.getUid());
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(requestsDatabase,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, final int position, @NonNull Contacts model) {
                getRef(position).child("RequestType").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            try {
                                if (dataSnapshot.getValue().toString().equals("RequestReceived")) {
                                        String userIds = getRef(position).getKey();
                                        mUserDatabase.child(userIds).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {
                                                    holder.mUserName.setText(dataSnapshot.child("name").getValue().toString());
                                                    holder.mUserStatus.setText(dataSnapshot.child("status").getValue().toString());
                                                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(holder.mProfileImage);
                                                } else {
                                                    holder.mUserName.setText(dataSnapshot.child("name").getValue().toString());
                                                    holder.mUserStatus.setText(dataSnapshot.child("status").getValue().toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                } else {
                                    holder.mProfileImage.setVisibility(View.INVISIBLE);
                                    holder.mUserName.setVisibility(View.INVISIBLE);
                                    holder.mUserStatus.setVisibility(View.INVISIBLE);
                                    holder.mOnline.setVisibility(View.INVISIBLE);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }finally {
                                holder.mButtonAccept.setVisibility(View.VISIBLE);
                                holder.mButtonCancel.setVisibility(View.VISIBLE);
                                holder.mButtonAccept.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mDatabase.child("Contacts").child(mAuth.getUid()).child(getRef(position).getKey()).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mDatabase.child("Contacts").child(getRef(position).getKey()).child(mAuth.getUid()).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                mDatabase.child("ChatRequests").child(getRef(position).getKey()).child(mAuth.getUid()).child("RequestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            mDatabase.child("ChatRequests").child(mAuth.getUid()).child(getRef(position).getKey()).child("RequestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Toast.makeText(getContext(), "Friend Added in your Contacts", Toast.LENGTH_SHORT).show();
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
                                            }
                                        });
                                    }
                                });
                                holder.mButtonCancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                            mDatabase.child("ChatRequests").child(getRef(position).getKey()).child(mAuth.getUid()).child("RequestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        mDatabase.child("ChatRequests").child(mAuth.getUid()).child(getRef(position).getKey()).child("RequestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(getContext(), "Request Cancelled", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                    }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends_users_layout,parent,false);
                    return new RequestViewHolder(view);
                }
        };
        mRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView mProfileImage,mOnline;
        private TextView mUserName,mUserStatus;
        private Button mButtonAccept,mButtonCancel;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            mOnline = itemView.findViewById(R.id.users_online_offline);
            mProfileImage = itemView.findViewById(R.id.users_profile_image);
            mUserName = itemView.findViewById(R.id.users_name);
            mUserStatus = itemView.findViewById(R.id.users_status);
            mButtonAccept = itemView.findViewById(R.id.button_accept);
            mButtonCancel = itemView.findViewById(R.id.button_cancel);
        }
    }
}