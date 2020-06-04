package com.jrtech.click;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private EditText mPhoneNumber,mVerificationCode;
    private Button mSendVerificationCode,mVerifyCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        initializeFields();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Phone Verification Failed", Toast.LENGTH_SHORT).show();
                mPhoneNumber.setVisibility(View.VISIBLE);
                mVerificationCode.setVisibility(View.INVISIBLE);
                mSendVerificationCode.setVisibility(View.VISIBLE);
                mVerifyCode.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                progressDialog.dismiss();
                mVerificationId = s;
                mResendToken = forceResendingToken;
                mPhoneNumber.setVisibility(View.INVISIBLE);
                mVerificationCode.setVisibility(View.VISIBLE);
                mSendVerificationCode.setVisibility(View.INVISIBLE);
                mVerifyCode.setVisibility(View.VISIBLE);
            }
        };
    }

    private void initializeFields() {
        mPhoneNumber = findViewById(R.id.phone_number);
        mVerificationCode = findViewById(R.id.verification_code);
        mAuth = FirebaseAuth.getInstance();
        mSendVerificationCode = findViewById(R.id.send_verification_code);
        mVerifyCode = findViewById(R.id.verify_code);
        progressDialog = new ProgressDialog(this);
    }
    public void sendVerificationCode(View view){
        if (TextUtils.isEmpty(mPhoneNumber.getText())){
            mPhoneNumber.setError("Phone number Required");
        }else {
            progressDialog.setTitle("Phone Verification");
            progressDialog.setMessage("Please wait while your phone number is being verified");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            String phoneNumber = mPhoneNumber.getText().toString();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks);        // OnVerificationStateChangedCallbacks
        }
    }
    public void verifyCode(View view){
        progressDialog.setTitle("Verifying Code");
        progressDialog.setMessage("Please wait while your code is being verified");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,mVerificationCode.getText().toString());
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            sendToMainActivity();
                        } else {
                            Toast.makeText(PhoneLoginActivity.this, "Phone Verification Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                });
    }

    private void sendToMainActivity() {
        final Intent intent = new Intent(PhoneLoginActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
