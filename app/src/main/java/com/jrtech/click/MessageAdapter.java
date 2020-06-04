package com.jrtech.click;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        Messages messages = userMessagesList.get(position);
        mDatabase.child("Users").child(messages.getFrom()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String imageLink = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(imageLink).placeholder(R.drawable.ic_person_24dp).into(holder.mReceiverImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (messages.getType().equals("text")){
            holder.mReceiverMessage.setVisibility(View.INVISIBLE);
            holder.mReceiverImage.setVisibility(View.INVISIBLE);
            holder.mSenderMessage.setVisibility(View.INVISIBLE );

            if (messages.getFrom().equals(mAuth.getUid())){
                holder.mSenderMessage.setVisibility(View.VISIBLE);
                holder.mSenderMessage.setText(messages.getMessage());

            }else {
                holder.mReceiverMessage.setVisibility(View.VISIBLE);
                holder.mReceiverImage.setVisibility(View.VISIBLE);
                holder.mReceiverMessage.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder{
        private TextView mSenderMessage,mReceiverMessage;
        private CircleImageView mReceiverImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mSenderMessage = itemView.findViewById(R.id.sender_message);
            mReceiverImage = itemView.findViewById(R.id.receiver_profile_image);
            mReceiverMessage = itemView.findViewById(R.id.receiver_message);
        }
    }
}
