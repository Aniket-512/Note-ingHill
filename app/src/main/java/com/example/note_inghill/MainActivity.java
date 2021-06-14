package com.example.note_inghill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.amplifyframework.core.Amplify;

public class MainActivity extends AppCompatActivity {

    private Button signoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Sign out intent - move back to AuthActivity
        Intent signoutIntent = new Intent(this, LoginActivity.class);

        // Sign out using Amplify Auth when button clicked
        signoutButton = findViewById(R.id.signout_button);
        signoutButton.setOnClickListener(v -> {
            Amplify.Auth.signOut(
                    () -> Log.i("AmplifyAuth", "Signed out successfully"),
                    error -> Log.e("AmplifyAuth", error.toString())
            );
            // start AuthActivity (login)
            startActivity(signoutIntent);
        });
    }
}