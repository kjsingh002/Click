package com.jrtech.click;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends Fragment {
    private View mGroupFragmentView;
    private ListView mGroupList;
    private DatabaseReference mDatabase;
    private ArrayList<String> mGroups;
    private ArrayAdapter arrayAdapter;

    public GroupsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mGroupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        initializeFields();
        retrieveGroups();
        moveToGroupChatActivity();
        return mGroupFragmentView;
    }

    private void moveToGroupChatActivity() {
        mGroupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent intent = new Intent(getContext(),GroupChatActivity.class);
                intent.putExtra("group",parent.getItemAtPosition(position).toString());
                startActivity(intent);
            }
        });
    }

    private void retrieveGroups() {
        mDatabase.child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                mGroups.clear();
                mGroups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void initializeFields() {
        mGroupList = mGroupFragmentView.findViewById(R.id.groups_list);
        mGroups = new ArrayList<>();
        arrayAdapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,mGroups);
        mGroupList.setAdapter(arrayAdapter);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }
}
