package com.example.note_inghill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.amplifyframework.auth.AuthChannelEventName;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.InitializationStatus;
import com.amplifyframework.hub.HubChannel;

// Main Activity only has a sign out button for now
// Will show Lecture recording and Image-to-Text feature on this screen in future
public class MainActivity extends AppCompatActivity {

    private Button signoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Unused, implementation remaining
        userStatus();

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