package com.capstone.note_inghill;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {
    // Variables for corresponding UI elements
    private TextView nameField;
    private TextView emailField;
    private TextView mobileField;
    private TextView passwordField;
    private TextView confirmPassField;

    private Button registerButton;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Intent to move back to Login Activity in case user is already registered
        Intent loginIntent = new Intent(this, LoginActivity.class);

        // Connect UI text fields to Java code
        nameField = findViewById(R.id.name_register);
        emailField = findViewById(R.id.email_register);
        mobileField = findViewById(R.id.mobile_register);
        passwordField = findViewById(R.id.password_register);
        confirmPassField = findViewById(R.id.confirm_pass_reg);

        // If user clicks Register button -> Try to register user
        registerButton = findViewById(R.id.registerUser_button);
        registerButton.setOnClickListener(v -> {
            try {
                registerUser();
            } catch (Exception e){
                Log.e("RegisterUser", "Failed: ", e);
            }
        });

        // If user clicks Login button -> move back to Login Activity via intent
        loginButton = findViewById(R.id.loginActivity_button);
        loginButton.setOnClickListener(v -> startActivity(loginIntent));
    }

    // Try registering user
    private void registerUser(){
        // Get user inputs
        String email = emailField.getText().toString();
        String name = nameField.getText().toString();
        String mobile = mobileField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPass = confirmPassField.getText().toString();

        // Verify inputs are non-empty
        if(email.isEmpty()) Toast.makeText(this,"Please enter your Email ID",Toast.LENGTH_LONG).show();
        else if(name.isEmpty()) Toast.makeText(this,"Please enter your Name",Toast.LENGTH_LONG).show();
        else if(mobile.isEmpty()) Toast.makeText(this,"Please enter your Mobile Number",Toast.LENGTH_LONG).show();
        else if(password.isEmpty()) Toast.makeText(this, "Please enter a password",Toast.LENGTH_LONG).show();
        else if(confirmPass.isEmpty()) Toast.makeText(this, "Please confirm the password",Toast.LENGTH_LONG).show();

        // Verify passwords match and non-empty user inputs
        if(password.equals(confirmPass) && !(email.isEmpty() || mobile.isEmpty() || name.isEmpty() || password.isEmpty())){
            // Store required user attributes for registering account - email, mobile, name
            ArrayList<AuthUserAttribute> attributes = new ArrayList<>();
            attributes.add(new AuthUserAttribute(AuthUserAttributeKey.email(), email));
            attributes.add(new AuthUserAttribute(AuthUserAttributeKey.phoneNumber(), mobile));
            attributes.add(new AuthUserAttribute(AuthUserAttributeKey.name(), name));

            // Attempt to register user (send request to AWS Cognito)
            Amplify.Auth.signUp(
                    email, password,
                    AuthSignUpOptions.builder().userAttributes(attributes).build(),
                    result -> {Log.i("AmplifyAuthSignUp", result.toString());
                        if(result.isSignUpComplete())
                            confirmRegistration();},
                    error -> Log.e("AmplifyAuthSignUp", error.toString())
            );
        }
        // If password fields do not match (confirmPass!=password)
        else if(!password.equals(confirmPass))
            Toast.makeText(this, "Ensure that the passwords match", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Try again", Toast.LENGTH_LONG).show();
    }

    private void confirmRegistration(){
        Intent confirmReg = new Intent(this,ConfirmRegActivity.class);
        confirmReg.putExtra("email",emailField.getText().toString());
        startActivity(confirmReg);
    }
}