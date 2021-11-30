package com.capstone.note_inghill;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.example.note_inghill.R;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


public class SpeechActivity extends AppCompatActivity {

    // Provide our own request code for permission request
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    // Audio files - recorded in 3GPP, converted to MP3
    private static String recordedFileName = null;
    private static String convertedFileName = null;

    // Android API - MediaRecorder to perform mic audio recording
    private MediaRecorder recorder = null;

    // Store permission to record audio
    private boolean permissionToRecordAudio = false;
    private final String [] permissions = {Manifest.permission.RECORD_AUDIO};

    // Random filename generated and stored
    private String uuid;

    // Store recording state
    boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        try {
            // Add the AWS Auth(Cognito) and Storage(S3) plugins
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(new AWSS3StoragePlugin());
            Amplify.configure(getApplicationContext());

            Log.i("Amplify", "Initialized Amplify");
        } catch (AmplifyException error) {
            Log.e("Amplify", "Could not initialize Amplify", error);
        }
        // Request for audio recording permissions from the user -> result in onRequestPermissionsResult() below
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        /*
        // Sign in to test
        Amplify.Auth.signIn(
                "test@example.com",
                "Test123@#",
                result -> Log.i("AmplifyAuth", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                error -> Log.e("AmplifyAuth", error.toString())
        );*/

        // Connect UI buttons to Java code via element IDs in activity_speech.xml
        Button recordButton = findViewById(R.id.record_button);
        Button uploadButton = findViewById(R.id.upload_button);
        Button summarizeButton = findViewById(R.id.summarize_button);

        // Listen for clicks on record button
        recordButton.setOnClickListener(v -> {
            // If not recording -> start recording, change button text to "Stop" and make upload button disappear
            if(!isRecording){
                startRecording();
                recordButton.setText(R.string.stop_record_button);  // "Stop" - Button text
                uploadButton.setVisibility(View.GONE);
            }
            // If recording -> stop recording,
            else {
                stopRecording();
                recordButton.setText(R.string.record_button_text);  // "Record" - Button text
                uploadButton.setVisibility(View.VISIBLE);
            }
        });

        // Listen for clicks on upload button
        uploadButton.setOnClickListener(v -> {
            File audioFile = new File(convertedFileName);
            Amplify.Storage.uploadFile(Amplify.Auth.getCurrentUser().getUserId()+"/"+uuid+".mp3", audioFile,
                    result -> Log.i("AmplifyStorage", "Storage: "+ result.getKey()),
                    err -> Log.e("AmplifyStorage", "Upload failed: ", err)
                    );
        });

        summarizeButton.setOnClickListener(v->{
            Intent intent = new Intent(this, TextSummarization.class); // Switching to a new activity
            //intent.putExtra("ImageName", "im2.jpg");
            startActivity(intent);
        });

    }

    // Called when activity is stopped, i.e., no longer visible to user
    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    // Store result of audio recording permission granted/denied
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAudio = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAudio) finish();
    }

    private void startRecording() {
        // Update recording state
        isRecording = !isRecording;

        // Write files to the external cache directory for visibility
        uuid = UUID.randomUUID().toString();
        recordedFileName = getExternalCacheDir().getAbsolutePath() + "/" + uuid + ".3gp";
        convertedFileName = getExternalCacheDir().getAbsolutePath() + "/" + uuid + ".mp3";

        // Android MediaRecorder setup
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordedFileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        // Prepare recorder
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("MediaRecorder", "prepare() failed");
        }
        // Start recording mic audio
        recorder.start();
    }

    private void stopRecording() {
        // Update recording state
        isRecording = !isRecording;

        // Stop recording and release the recorder
        recorder.stop();
        recorder.release();
        recorder = null;

        // Convert 3GPP recorded audio to MP3 format
        int convert = FFmpeg.execute(String.format("-i %s -c:a libmp3lame %s", recordedFileName, convertedFileName));
        if (convert == RETURN_CODE_SUCCESS){
            Log.i(Config.TAG, "Command execution completed successfully.");
            Log.i("MediaRecorder", "Audio File: ");
        }
    }
}