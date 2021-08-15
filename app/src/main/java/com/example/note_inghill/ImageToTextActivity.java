package com.example.note_inghill;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ImageToTextActivity extends AppCompatActivity {

    // This Activity is responsible for handling Image to Text.
    // The screen will contain a camera view and an upload button.
        // - The Upload button will run amplify function to connect and upload image to S3
        // - On upload to S3 bucket, a lambda function is triggered which then performs OCR.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_text);

        

    }
}