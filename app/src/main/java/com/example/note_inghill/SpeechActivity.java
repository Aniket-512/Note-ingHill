package com.example.note_inghill;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.IOException;
import java.util.UUID;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;


public class SpeechActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String recordedFileName = null;
    private static String convertedFileName = null;


    private MediaRecorder recorder = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAudio = false;
    private final String [] permissions = {Manifest.permission.RECORD_AUDIO};

    boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        // Record to the external cache directory for visibility
        String uuid = UUID.randomUUID().toString();
        recordedFileName = getExternalCacheDir().getAbsolutePath() + "/" + uuid + ".3gp";
        convertedFileName = getExternalCacheDir().getAbsolutePath() + "/" + uuid + ".mp3";

        Button recordButton = findViewById(R.id.record_button);
        Button uploadButton = findViewById(R.id.upload_button);

        recordButton.setOnClickListener(v -> {
            if(!recording){
                startRecording();
                recordButton.setText(R.string.stop_record_button);
                uploadButton.setVisibility(View.INVISIBLE);
            }
            else {
                stopRecording();
                recordButton.setText(R.string.record_button_text);
                uploadButton.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAudio = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAudio) finish();
    }

    private void startRecording() {
        recording = !recording;

        // Android MediaRecorder setup
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordedFileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("MediaRecorder", "prepare() failed");
        }
        recorder.start();
    }

    private void stopRecording() {
        recording = !recording;

        recorder.stop();
        recorder.release();
        recorder = null;

        // Convert 3GPP recorded audio to MP3 format
        int convert = FFmpeg.execute(String.format("-i %s -c:a libmp3lame %s", recordedFileName, convertedFileName));
        if (convert == RETURN_CODE_SUCCESS){
            Log.i(Config.TAG, "Command execution completed successfully.");
        }
    }
}