package com.example.note_inghill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;

public class AuthActivity extends AppCompatActivity {

    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity);

        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());
            Log.i("AmplifyAuth", "Initialized Amplify Auth");
        } catch (AmplifyException error) {
            Log.e("AmplifyAuth", "Could not initialize Amplify Auth", error);
        }

        // Check whether user is signed in or not
        Amplify.Auth.fetchAuthSession(
                result -> Log.i("AmplifyAuthSession", result.toString()),
                error -> Log.e("AmplifyAuthSession", error.toString())
        );

        Intent loginIntent = new Intent(this, MainActivity.class);

        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {
            // Attempt to sign in
            Amplify.Auth.signIn(
                    "aniket.ga8@gmail.com",
                    "Pass1234!",
                    result -> Log.i("AmplifyAuth", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                    error -> Log.e("AmplifyAuth", error.toString())
            );

            startActivity(loginIntent);
        });


    }
}