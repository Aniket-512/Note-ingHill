package com.example.note_inghill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class SpeechActivity extends AppCompatActivity {
    // Audio recording is a dangerous permission and hence requires to be asked for at run-time

    // Setting permission request code instead of having it created by system
    private static final int REQUEST_RECORD_AUDIO = 200;
    // Store permissions request String from Manifest file
    private static final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    // Store permission status (granted/not)
    private boolean audioRecordingPermissionGranted;

    // IBM Watson Speech-to-Text API Key, URL
    private static final String API_KEY = "exDcMHqaQLPF7cmfgJIo9f3_w-Me4BmMuA5Tp_Py0vPL";
    private static final String URL = "https://api.eu-gb.speech-to-text.watson.cloud.ibm.com/instances/3dcd7d72-a638-4e28-88fb-cb746781714f";

    // Store list of results, results adapter
    private List<Result> results;
    private ResultsRecyclerAdapter resultsRecyclerAdapter;

    // Android API to create recording of speech
    private MediaRecorder mediaRecorder;
    // UI Record, Convert Buttons
    private Button recordButton;
    private Button convertButton;
    // Variables to store filenames
    private String recordedFileName;
    private String convertedFileName;
    // Recording status
    private boolean isRecording;
    // Main thread handler
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        // Request user for audio recording permissions
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO);

        // Initially not recording
        isRecording = false;

        mainHandler = new Handler();

        results = new ArrayList<>();

        RecyclerView resultsRecyclerView = findViewById(R.id.results_recycler);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsRecyclerAdapter = new ResultsRecyclerAdapter();
        resultsRecyclerView.setAdapter(resultsRecyclerAdapter);


        // Connect UI Button to Java code by id
        recordButton = findViewById(R.id.record_button);
        // Listen for click event on button
        recordButton.setOnClickListener(v -> {
            // If not recording currently and recording permission is granted
            if(!isRecording){
                if(audioRecordingPermissionGranted){
                    // Try to call audio recording function
                    try {
                        startAudioRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else{
                toggleRecording();
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                convertButton.setVisibility(View.VISIBLE);
            }
        });

        // Listen for click on convert button
        convertButton = findViewById(R.id.convert_button);
        convertButton.setOnClickListener(v -> {
            Thread thread = new Thread(() -> {
                // Call convertSpeech() function
                try {
                    convertSpeech();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        });
    }

    // Toggle recording status and change button text accordingly.
    private void toggleRecording() {
        isRecording = !isRecording;
        if (isRecording) {
            recordButton.setText(R.string.stop_record_button);
        } else {
            recordButton.setText(R.string.record_button_text);
        }
    }

    // Automatically called function in the background when permissions request is sent
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Call parent function
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Additional modifications -
        // Store result of request for audio recording permission
        if (requestCode==REQUEST_RECORD_AUDIO){
                audioRecordingPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                Log.i("AudioPermission", "onRequestPermissionsResult: "+audioRecordingPermissionGranted);
        }
        // If permission was not granted, close the activity (app)
        if (!audioRecordingPermissionGranted) {
            Log.i("AudioPermission", "Recording Permission Denied");
            finish();
        }
    }

    // Start recording audio from microphone
    private void startAudioRecording() throws IOException {
        toggleRecording();
        convertButton.setVisibility(View.INVISIBLE);
        // Use random UUIDs for filenames
        String uuid = UUID.randomUUID().toString();
        recordedFileName = getFilesDir().getPath() + "/" + uuid + ".3gp";
        convertedFileName = getFilesDir().getPath() + "/" + uuid + ".mp3";

        // Android MediaRecorder API records by default in 3GPP format
        // Setup for recorder is as follows -
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(recordedFileName);

        // Prepare recorder and begin capturing and encoding data in 3GPP format
        mediaRecorder.prepare();
        mediaRecorder.start();
    }

    private void convertSpeech() throws FileNotFoundException {
        // Convert recorded audio in 3GPP format to MP3 format using FFMPEG
        int rc = FFmpeg.execute(String.format("-i %s -c:a libmp3lame %s", recordedFileName, convertedFileName));

        if (rc == RETURN_CODE_SUCCESS) {
            Log.i(Config.TAG, "Command execution completed successfully.");

            // Create authenticator object for access to IBM Watson API
            IamAuthenticator authenticator = new IamAuthenticator(API_KEY);
            // Setup Speech-to-Text API
            SpeechToText speechToText = new SpeechToText(authenticator);
            speechToText.setServiceUrl(URL);

            File audioFile = new File(convertedFileName);

            // Narrowband Model uses 8kHz instead of 16kHz sampling rate, good for low internet connectivity
            RecognizeOptions options = new RecognizeOptions.Builder()
                    .audio(audioFile)
                    .contentType(HttpMediaType.AUDIO_MP3)
                    .model("en-AU_NarrowbandModel")
                    .build();

            // Final transcript of conversion
            final SpeechRecognitionResults transcript = speechToText.recognize(options).execute().getResult();
            Log.i("Transcript", ""+transcript);

            mainHandler.post(() -> {
                try {
                    results.clear();
                    JSONObject jsonObject = new JSONObject(transcript.toString());
                    JSONArray resultsArray = jsonObject.getJSONArray("results");
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONArray alternativesArray = resultsArray.getJSONObject(i).getJSONArray("alternatives");
                        for (int j = 0; j < alternativesArray.length(); j++) {
                            JSONObject resultObject = alternativesArray.getJSONObject(j);
                            results.add(
                                    new Result(
                                            resultObject.getString("transcript"),
                                            resultObject.getDouble("confidence")
                                    )
                            );
                        }
                    }
                    resultsRecyclerAdapter.setResults(results);
                    Log.i("RecyclerAdapter", "results: "+results);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(Config.TAG, "Command execution cancelled by user.");
        } else {
            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            Config.printLastCommandOutput(Log.INFO);
        }
    }
}