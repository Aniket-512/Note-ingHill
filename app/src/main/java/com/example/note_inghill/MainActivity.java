package com.example.note_inghill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amplifyframework.auth.AuthChannelEventName;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.InitializationStatus;
import com.amplifyframework.hub.HubChannel;

// Main Activity only has a sign out button for now
// Will show Lecture recording and Image-to-Text feature on this screen in future
public class MainActivity extends AppCompatActivity {
//Password : Ronaldoidol#7

    private Button signoutButton;
    private Button imageToText; // Button press loads new activity
    private Button speechtoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Unused, implementation remaining - possible way of
        userStatus();
        // Sign out intent - move back to AuthActivity
        Intent signoutIntent = new Intent(this, LoginActivity.class);

        //Image to Text Button
        imageToText = findViewById(R.id.imageToTextId);
        imageToText.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImageToTextActivity.class);
            startActivity(intent);
        });

        //Speech-to-Text Button
        speechtoText = findViewById(R.id.speechtext_button);
        speechtoText.setOnClickListener(v -> {
            Intent speechIntent = new Intent(this, SpeechActivity.class);
            startActivity(speechIntent);
        });

        // Sign out using Amplify Auth when button clicked
        Button signoutButton = findViewById(R.id.signout_button);
        signoutButton.setOnClickListener(v -> {
            Amplify.Auth.signOut(
                    () -> Log.i("AmplifyAuth", "Signed out successfully"),
                    error -> Log.e("AmplifyAuth", error.toString())
            );
            // start AuthActivity (login)
            startActivity(signoutIntent);
        });
    }

    // Unused function, implementation remaining to return user auth status
    private void userStatus(){
        // AWS Cognito Auth Plugin sends important Auth events through Amplify Hub.
        Amplify.Hub.subscribe(HubChannel.AUTH,
                hubEvent -> {
                    if (hubEvent.getName().equals(InitializationStatus.SUCCEEDED.toString())) {
                        Log.i("AmplifyAuthHub", "Auth Hub successfully initialized");
                    } else if (hubEvent.getName().equals(InitializationStatus.FAILED.toString())){
                        Log.i("AmplifyAuthHub", "Auth Hub failed to succeed");
                    } else {
                        switch (AuthChannelEventName.valueOf(hubEvent.getName())) {
                            case SIGNED_IN:
                                Log.i("AmplifyAuthHub", "Auth just became signed in.");
                                break;
                            case SIGNED_OUT:
                                Log.i("AmplifyAuthHub", "Auth just became signed out.");
                                break;
                            case SESSION_EXPIRED:
                                Log.i("AmplifyAuthHub", "Auth session just expired.");
                                break;
                            default:
                                Log.w("AmplifyAuthHub", "Unhandled Auth Event: " + AuthChannelEventName.valueOf(hubEvent.getName()));
                                break;
                        }
                    }
                }
        );
    }
}