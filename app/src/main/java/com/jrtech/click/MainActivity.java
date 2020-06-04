package com.jrtech.click;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private TabAccessAdapter mTabAccessAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText mGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeFields();
        mGroupName.setHint("eg. Desi Boyz");
        mToolbar.setTitle("Click");
        setSupportActionBar(mToolbar);
        mViewPager.setAdapter(mTabAccessAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initializeFields() {
        mToolbar = findViewById(R.id.main_page_toolbar);
        mTabLayout = findViewById(R.id.my_tab_layout);
        mViewPager = findViewById(R.id.my_view_pager);
        mTabAccessAdapter = new TabAccessAdapter(getSupportFragmentManager(),4);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mGroupName = new EditText(MainActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null){
            sendToLoginActivity();
        }else {
            verifyUserExistence();
        }
    }

    private void verifyUserExistence() {
        mDatabase.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").exists()){
                    //Do Nothing
                }else{
                    sendToSettingsActivityPermanent();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendToLoginActivity() {
        final Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void sendToSettingsActivity() {
        final Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(intent);
    }
    private void sendToSettingsActivityPermanent() {
        final Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_options_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.find_friends:
                sendToFindFriendsActivity();
                return true;
            case R.id.settings:
                sendToSettingsActivity();
                return true;
            case R.id.sign_out:
                mAuth.signOut();
                sendToLoginActivity();
                return true;
            case R.id.create_group:
                createUserGroup();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendToFindFriendsActivity() {
        final Intent intent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(intent);
    }

    private void createUserGroup() {
        new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog)
                .setTitle("Enter Group Name :")
                .setView(mGroupName)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.child("Groups").child(mGroupName.getText().toString()).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(MainActivity.this, "Group Created Successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}