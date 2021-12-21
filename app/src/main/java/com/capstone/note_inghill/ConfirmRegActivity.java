package com.capstone.note_inghill;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.amplifyframework.core.Amplify;

// Confirm User Registration with OTP
public class ConfirmRegActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_reg);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Get user's Email ID from RegisterActivity
        String email = getIntent().getStringExtra("email");

        // Connect UI elements to Java code - OTP text field, Confirm button
        TextView otpField = findViewById(R.id.otp_field);
        Button confirmOtpButton = findViewById(R.id.confirm_otp_button);

        Intent mainIntent = new Intent(this, MainActivity.class);

        confirmOtpButton.setOnClickListener(v -> {
            // Ensure OTP field is not empty
            assert !otpField.getText().toString().isEmpty();
            // No error handling yet
            Amplify.Auth.confirmSignUp(email, otpField.getText().toString(),
                result -> {Log.i("AmplifyAuthConfirm", result.isSignUpComplete() ? "Confirm signUp succeeded" : "Confirm sign up not complete");
                    if(result.isSignUpComplete())
                        startActivity(mainIntent);},
                error ->Log.e("AmplifyAuthConfirm", error.toString())
            );
        });
    }
}