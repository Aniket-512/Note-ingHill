package com.example.note_inghill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {
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

        Intent loginIntent = new Intent(this, LoginActivity.class);

        // Connect UI text fields to Java code
        nameField = findViewById(R.id.name_register);
        emailField = findViewById(R.id.email_register);
        mobileField = findViewById(R.id.mobile_register);
        passwordField = findViewById(R.id.password_register);
        confirmPassField = findViewById(R.id.confirm_pass_reg);

        registerButton = findViewById(R.id.registerUser_button);
        registerButton.setOnClickListener(v -> {
            try {
                registerUser();
            } catch (Exception e){
                Log.e("RegisterUser", "Failed: ", e);
            }
        });

        loginButton = findViewById(R.id.loginActivity_button);
        loginButton.setOnClickListener(v ->{
            startActivity(loginIntent);
        });
    }

    // Try registering user
    private void registerUser(){
        // Verify non-empty user inputs
        String email = emailField.getText().toString();
        if(email.isEmpty()) Toast.makeText(this,"Please enter your Email ID",Toast.LENGTH_LONG).show();
        String name = nameField.getText().toString();
        if(name.isEmpty()) Toast.makeText(this,"Please enter your Name",Toast.LENGTH_LONG).show();
        String mobile = mobileField.getText().toString();
        if(mobile.isEmpty()) Toast.makeText(this,"Please enter your Mobile Number",Toast.LENGTH_LONG).show();
        String password = passwordField.getText().toString();
        if(password.isEmpty()) Toast.makeText(this, "Please enter a password",Toast.LENGTH_LONG).show();
        String confirmPass = confirmPassField.getText().toString();
        if(confirmPass.isEmpty()) Toast.makeText(this, "Please confirm the password",Toast.LENGTH_LONG).show();

        // Verify passwords match and non-empty user inputs
        if(password.equals(confirmPass) && !(email.isEmpty() && mobile.isEmpty() && name.isEmpty() && password.isEmpty())){
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