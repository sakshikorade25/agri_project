package com.sakshikorade25.agri;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class signup extends AppCompatActivity {

    Button signin;
    Button getVerificationCode;
    EditText phoneNumber;
    EditText verficationCode;
    FirebaseAuth mAuth;
    String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signin = findViewById(R.id.button4);
        getVerificationCode = findViewById(R.id.button3);
        phoneNumber = findViewById(R.id.editText5);
        verficationCode = findViewById(R.id.editText6);
        mAuth = FirebaseAuth.getInstance();

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySignInCode();
            }
        });

        getVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerficationCode();
            }
        });
    }

    private void sendVerficationCode()
    {
        String phone = phoneNumber.getText().toString();
        if(phone.isEmpty())
        {
            phoneNumber.setError("Phone number is required");
            phoneNumber.requestFocus();
            return;
        }
        if(phone.length()<10)
        {
            phoneNumber.setError("Enter a valid phone number");
            phoneNumber.requestFocus();
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
        phone,        // Phone number to verify
        60,                 // Timeout duration
        TimeUnit.SECONDS,   // Unit of timeout
        this,               // Activity (for callback binding)
        mCallbacks);        // OnVerificationStateChangedCallbacksPhoneAuthActivity.java
    }

       PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
           @Override
           public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

           }

           @Override
           public void onVerificationFailed(FirebaseException e) {

           }

           @Override
           public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
               super.onCodeSent(s, forceResendingToken);

               codeSent = s;
           }
       };

    private void verifySignInCode()
    {
        String code = verficationCode.getText().toString();
        if(code.isEmpty())
        {
            verficationCode.setError("Verification code is required");
            verficationCode.requestFocus();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("s", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // ...
                            Toast.makeText(getApplicationContext(),
                                    "Login Successfull", Toast.LENGTH_LONG).show();

                            Intent intent= new Intent(signup.this, MainActivity.class);
                            startActivity(intent);


                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("f", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect Verification Code ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

}
