package com.example.note_inghill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImageView;

public class ImageToTextActivity extends AppCompatActivity {

    // This Activity is responsible for handling Image to Text.
    // The screen will contain a camera view and an upload button.
        // - Camera needs a cropper to crop the image into a square image.
        // - The Upload button will run amplify function to connect and upload image to S3
        // - On upload to S3 bucket, a lambda function is triggered which then performs OCR.

    // Additional Feature would be to provide a gallery option as well.
    
    static final int REQUEST_IMAGE_CAPTURE = 1;
    
    // XML Asset Declarations
    private Button openCamera, openGallery;
    private ImageView imageView;

    private CropImageView mCropImageView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_text);
        
        //Button Declarations
        openCamera = findViewById(R.id.Camera_open_button);
        openGallery = findViewById(R.id.Gallery_open_button);

        //Crop Image View
        mCropImageView = (CropImageView) findViewById(R.id.cropImageView);
        
        //Open camera onClick Listener
        openCamera.setOnClickListener(v -> {
            dispatchTakePictureIntent();
        });

        // Open Gallery
        openGallery.setOnClickListener(v-> {
            dispatchOpenGalleryIntent();
        });

    }

    //Function to pick image from gallery
    private void dispatchOpenGalleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }

    //Function to take an image with hardware camera
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Unable to Open Camera", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mCropImageView.setImageBitmap(imageBitmap);
        }
    }
}