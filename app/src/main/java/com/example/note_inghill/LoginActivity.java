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

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;
    private TextView userMobile; // Mobile number variable
    private TextView userPassword; // Password variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Amplify Auth plugin setup on device
        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());
            Log.i("AmplifyAuth", "Initialized Amplify Auth");
        } catch (AmplifyException error) {
            Log.e("AmplifyAuth", "Could not initialize Amplify Auth", error);
        }

        // Intent to shift to Main Activity upon logging in successfully
        Intent mainIntent = new Intent(this, MainActivity.class);
        // Intent to shift to Register Activity in case of new user
        Intent registerIntent = new Intent(this, RegisterActivity.class);

        // Check whether user is signed in or not
        // If signed in -> show Main Activity
        Amplify.Auth.fetchAuthSession(
                result -> {Log.i("AmplifyAuthSession", result.toString());
                if(result.isSignedIn()) startActivity(mainIntent);},
                error -> Log.e("AmplifyAuthSession", error.toString())
        );

        // Listen for click on Register button, if clicked move to -> Register Activity
        registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> {
            startActivity(registerIntent);
        });

        // Connect variables to corresponding UI elements
        userMobile = findViewById(R.id.phone_field_login);
        userPassword = findViewById(R.id.password_field_login);

        // Login button click -> sign in attempt -> show Main activity
        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {
            // Check if fields are not empty
            if(TextUtils.isEmpty(userMobile.getText()))
                Toast.makeText(this,"Please enter your Mobile Number",Toast.LENGTH_LONG).show();
            else if(TextUtils.isEmpty(userPassword.getText()))
                Toast.makeText(this, "Please enter your Password", Toast.LENGTH_LONG).show();

            else if(!(userPassword.getText().toString().isEmpty() && userMobile.getText().toString().isEmpty())){
                // Attempt to sign in
                Amplify.Auth.signIn(
                        userMobile.getText().toString(), userPassword.getText().toString(),
                        result -> {Log.i("AmplifyAuth", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete");
                        if(result.isSignInComplete()) startActivity(mainIntent);},
                        error -> Log.e("AmplifyAuth", error.toString())
                );
            }
        });
    }
}