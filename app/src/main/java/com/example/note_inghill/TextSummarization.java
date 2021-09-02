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

    Button submitButton, downloadButton;

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
        downloadButton = findViewById(R.id.downloadButton);

        submitButton.setOnClickListener(v-> {
            String result = "Selected Course: ";
            result+= (ShortSum.isChecked())?"Short Summary":(MedSum.isChecked())?"Medium Summary":(LongSum.isChecked())?"Long Summary":"";
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
        });

        //Amplify
        downloadButton.setOnClickListener(v -> {
            Amplify.Storage.downloadFile(
                    "TextOutput.txt",
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/TextOutput.txt"),
                    result ->  Log.i("MyAmplifyApp", "Successfully downloaded: " + result.getFile().getName()),
                    error -> Log.e("MyAmplifyApp",  "Download Failure", error)
            );

            //Getting Download URL :
//            String resultUrl;
//            Amplify.Storage.getUrl(
//                    "TextOutput.txt",
//                    result -> Log.i("MyAmplifyApp", "Successfully generated: " + result.getUrl()),
//                    error -> Log.e("MyAmplifyApp", "URL generation failure", error)
//            );

        });


    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        String str="";
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.shortSummaryButton:
                if(checked)
                    str = "Android Selected";
                break;
            case R.id.mediumSummaryButton:
                if(checked)
                    str = "AngularJS Selected";
                break;
            case R.id.longSummaryButton:
                if(checked)
                    str = "Java Selected";
                break;
        }
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

}