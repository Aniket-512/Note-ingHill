package com.example.note_inghill;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.options.StorageDownloadFileOptions;

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

        submitButton.setOnClickListener(v-> {
            //String result = "Selected Course: ";
            String result = (ShortSum.isChecked())?"Short Summary":(MedSum.isChecked())?"Medium Summary":(LongSum.isChecked())?"Long Summary":"";
            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            if(result.equals("Short Summary")){
                amplify_download_summary("S");
            }
            if(result.equals("Medium Summary")){
                amplify_download_summary("M");
            }
            if(result.equals("Long Summary")){
                amplify_download_summary("L");
            }
            if(result.isEmpty()){
                Toast.makeText(this, "Please Select a Type of Summary", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void amplify_download_summary(String sumType) {

//        Amplify.Storage.downloadFile(
//                "TextOutput" + sumType + ".txt",
//                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/TextOutput.txt"),
//                result ->  Log.i("MyAmplifyApp", "Successfully downloaded: " + result.getFile().getName()),
//                error -> Log.e("MyAmplifyApp",  "Download Failure", error)
//        );
        Toast.makeText(this, "TextOutput" + sumType + ".txt Downloaded", Toast.LENGTH_SHORT).show();
    }


}