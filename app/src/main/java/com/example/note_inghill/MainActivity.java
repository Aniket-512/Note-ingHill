package com.example.note_inghill;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());
            Log.i("MyAmplifyApp", "Initialized Amplify Auth");
        } catch (AmplifyException error) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify Auth", error);
        }

        // Check whether user is signed in or not
        Amplify.Auth.fetchAuthSession(
                result -> Log.i("AmplifyQuickstart", result.toString()),
                error -> Log.e("AmplifyQuickstart", error.toString())
        );

        /*
        // Test account attributes
        ArrayList<AuthUserAttribute> attributes = new ArrayList<>();
        attributes.add(new AuthUserAttribute(AuthUserAttributeKey.email(), "aniket.ga8@gmail.com"));
        attributes.add(new AuthUserAttribute(AuthUserAttributeKey.phoneNumber(), "+917022995558"));

        // Test account registration
        Amplify.Auth.signUp(
                "aniket.ga8@gmail.com",
                "Pass1234!",
                AuthSignUpOptions.builder().userAttributes(attributes).build(),
                result -> Log.i("AuthQuickstart", result.toString()),
                error -> Log.e("AuthQuickstart", error.toString())
        );*/

        // Attempt to sign in
        Amplify.Auth.signIn(
                "aniket.ga8@gmail.com",
                "Pass1234!",
                result -> Log.i("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                error -> Log.e("AuthQuickstart", error.toString())
        );
    }
}