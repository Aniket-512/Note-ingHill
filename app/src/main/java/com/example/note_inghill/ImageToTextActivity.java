package com.example.note_inghill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ImageToTextActivity extends AppCompatActivity {

    // This Activity is responsible for handling Image to Text.
    // The screen will contain a camera view and an upload button.
        // - Camera needs a cropper to crop the image into a square image.
        // - The Upload button will run amplify function to connect and upload image to S3
        // - On upload to S3 bucket, a lambda function is triggered which then performs OCR.

    // Additional Feature would be to provide a gallery option as well.

    
    // XML Asset Declarations
    private Button openGallery, uploadButton;
    private ImageView imageView;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_text);
        
        // Button Declarations
        openGallery = findViewById(R.id.Gallery_open_button);
        uploadButton = findViewById(R.id.Upload_image_button);

        // Image View
        imageView = findViewById(R.id.imageView2);

        // Open Gallery : Opens a image picker which is native to the OS.
        openGallery.setOnClickListener(v-> {
            startCropActivity();
        });

    }

    private void startCropActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri(); // Result uri holds the cropped image
                imageView.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("Crop Image Error ", error.toString());
            }
        }
    }

}