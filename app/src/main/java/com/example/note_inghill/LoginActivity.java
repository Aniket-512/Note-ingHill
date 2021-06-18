package com.example.note_inghill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;

import java.util.concurrent.atomic.AtomicInteger;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;
    private TextView userEmail; // Mobile number UI field variable
    private TextView userPassword; // Password UI field variable

    private int counter = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Intent to shift to Main Activity upon logging in successfully
        Intent mainIntent = new Intent(this, MainActivity.class);

        // Amplify Auth plugin setup on device
        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());
            Log.i("Amplify", "Initialized Amplify plugins");
        } catch (AmplifyException error) {
            Log.e("Amplify", "Could not initialize Amplify plugins", error);
        }

        // Intent to shift to Register Activity in case of new user
        Intent registerIntent = new Intent(this, RegisterActivity.class);

        // Check whether user is signed in or not
        // If signed in -> show Main Activity
        Amplify.Auth.fetchAuthSession(
                result -> {Log.i("AmplifyAuthSession", result.toString());
                if(result.isSignedIn())
                    startActivity(mainIntent);},
                error -> Log.e("AmplifyAuthSession", error.toString())
        );

        // Listen for click on Register button, if clicked move to -> Register Activity
        registerButton = findViewById(R.id.registerActivity_button);
        registerButton.setOnClickListener(v -> startActivity(registerIntent));

        // Connect Java code variables to corresponding UI elements
        userEmail = findViewById(R.id.email_login);
        userPassword = findViewById(R.id.password_login);   // Test password -

        // Login button click -> sign in attempt -> show Main activity
        loginButton = findViewById(R.id.loginActivity_button);
        loginButton.setOnClickListener(v -> {
            attemptLogin();
        });
    }

    // Function to try logging in user
    private void attemptLogin(){
        // Check if fields are empty, toast accordingly
        if(TextUtils.isEmpty(userEmail.getText()))
            Toast.makeText(this,"Please enter your Email ID",Toast.LENGTH_LONG).show();
        else if(TextUtils.isEmpty(userPassword.getText()))
            Toast.makeText(this, "Please enter your Password", Toast.LENGTH_LONG).show();

        // Test account credentials -
        // Email - test@example.com
        // Password - Test123@#

        // If input fields are not empty, and max login attempts have not been made -> try to login user
        else if(!(userPassword.getText().toString().isEmpty() && userEmail.getText().toString().isEmpty()) && counter!=0){
            // Intent to shift to Main Activity upon logging in successfully
            Intent mainIntent = new Intent(this, MainActivity.class);
            // Attempt to sign in
            Amplify.Auth.signIn(
                    userEmail.getText().toString(), userPassword.getText().toString(),
                    // Log result of sign in attempt, if successful move to MainActivity
                    result -> {Log.i("AmplifyAuth", result.isSignInComplete() ? "Sign in  successful":"Sign in FAILED");
                        if(result.isSignInComplete())
                            startActivity(mainIntent);},
                    error -> Log.e("AmplifyAuth", error.toString())
            );
            counter-=1;
        }
    }
}