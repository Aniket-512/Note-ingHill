package com.example.note_inghill;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.core.Amplify;

import java.io.File;


public class TextSummarization extends AppCompatActivity {

    RadioGroup radioGroup;
    RadioButton ShortSum, MedSum, LongSum;

    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_summarization);


        //Radio Buttons Declaration Configuration
        radioGroup = findViewById(R.id.radioGroup);
        ShortSum = findViewById(R.id.shortSummaryButton);
        MedSum = findViewById(R.id.mediumSummaryButton);
        LongSum = findViewById(R.id.longSummaryButton);

        submitButton = findViewById(R.id.SubmitTextSumTypeButton);

        //Based on the type of summary selected, we simply download the relevant file from S3.
        // Note : All 3 types of summarization is done on upload of file. We are simply downloading the necessary one.
        submitButton.setOnClickListener(v-> {
            String result = (ShortSum.isChecked())?"Short Summary":(MedSum.isChecked())?"Medium Summary":(LongSum.isChecked())?"Long Summary":"";
            if(result.equals("Short Summary")){
                amplify_download_summary("-Short");
            }
            if(result.equals("Medium Summary")){
                amplify_download_summary("-Med");
            }
            if(result.equals("Long Summary")){
                amplify_download_summary("-Long");
            }
            if(result.isEmpty()){
                Toast.makeText(this, "Please Select a Type of Summary", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to stop repeating code. Based on type, Amplify is asked to download from S3 based on the file name.
    private void amplify_download_summary(String sumType) {
// Amplify Code will be uncommented once the summarization code is hosted on AWS.
        Amplify.Storage.downloadFile(
                Amplify.Auth.getCurrentUser().getUserId()+"/" + "test" + sumType + ".txt",
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/test" + sumType + ".txt"),
                result ->  Log.i("MyAmplifyApp", "Successfully downloaded: " + result.getFile().getName()),
                error -> Log.e("MyAmplifyApp",  "Download Failure", error)
        );
        Toast.makeText(this, "TextOutput" + sumType + ".txt Downloaded", Toast.LENGTH_SHORT).show(); // Dev Code to know if code is working correctly
    }
    
}