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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    private EditText mEmail,mPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeFields();
    }

    private void initializeFields() {
        mEmail = findViewById(R.id.email_address);
        mPassword = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        progressDialog = new ProgressDialog(this);
    }

    public void loginAccount(View view){
        if (TextUtils.isEmpty(mEmail.getText()) && TextUtils.isEmpty(mPassword.getText())){
            mEmail.setError("Field Required");
            mPassword.setError("Field Required");
        }else if (TextUtils.isEmpty(mEmail.getText())){
            mEmail.setError("Field Required");
        }else if (TextUtils.isEmpty(mPassword.getText())){
            mPassword.setError("Field Required");
        }else if (Patterns.EMAIL_ADDRESS.matcher(mEmail.getText()).matches()){
            progressDialog.setTitle("Signing In");
            progressDialog.setMessage("Please wait");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(mEmail.getText().toString(),mPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        mUserRef.child(mAuth.getUid()).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    progressDialog.dismiss();
                                    sendToMainActivity();
                                }
                            }
                        });
                    }else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else {
            mEmail.setError("Enter a valid email");
        }
    }

    public void sendToPhoneLoginActivity(View view) {
        final Intent intent = new Intent(LoginActivity.this,PhoneLoginActivity.class);
        startActivity(intent);
    }

    private void sendToMainActivity() {
        final Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void goToRegisterActivity(View view){
        final Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }
}