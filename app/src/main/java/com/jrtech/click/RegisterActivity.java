package com.jrtech.click;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEmail,mPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initializeFields();
    }

    public void goToMainActivity(View view){
        final Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void initializeFields() {
        mEmail = findViewById(R.id.register_email_address);
        mPassword = findViewById(R.id.register_password);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        mDatabase = FirebaseDatabase.getInstance();
    }

    public void createAccount(View view){
        if (TextUtils.isEmpty(mEmail.getText()) && TextUtils.isEmpty(mPassword.getText())){
            mEmail.setError("Field Required");
            mPassword.setError("Field Required");
        }else if (TextUtils.isEmpty(mEmail.getText())){
            mEmail.setError("Field Required");
        }else if (TextUtils.isEmpty(mPassword.getText())){
            mPassword.setError("Field Required");
        }else if (Patterns.EMAIL_ADDRESS.matcher(mEmail.getText()).matches()){
            progressDialog.setTitle("Creating New Account");
            progressDialog.setMessage("Please wait while your account is being Created");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(),mPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        mDatabase.getReference().child("Users").child(mAuth.getUid()).setValue("");
                        mDatabase.getReference().child("Users").child(mAuth.getUid()).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    progressDialog.dismiss();
                                    goToMainActivity(getCurrentFocus());
                                }
                            }
                        });
                    }else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account Creation Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else {
            mEmail.setError("Enter a valid email");
        }
    }
}
