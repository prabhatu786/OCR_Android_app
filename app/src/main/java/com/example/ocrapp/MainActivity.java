package com.example.ocrapp;

import static androidx.core.provider.FontsContractCompat.FontRequestCallback.RESULT_OK;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActivityChooserView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    Button capture;
    Button copy;
    TextView textView;
    Bitmap bitmap;
    private static final int REQUEST_CAMERA_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        capture = findViewById(R.id.capturebnt);
        copy = findViewById(R.id.copytextbnt);
        textView = findViewById(R.id.text1);



        // checking fro camra  permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
            Log.e("100", "request sended");
        }
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start picker to get image for cropping and then use the image in cropping activity
                // Here we will pick image from gallery or camera
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MainActivity.this);
            }
        });
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scanned_text = textView.getText().toString();
                copyToClipBoard(scanned_text);

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            // here we are storing the crop images
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri uri = result.getUri();
                try {
                    bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                    getTextFromImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

    }
    private void getTextFromImage(Bitmap bitmap){
        TextRecognizer recognizer= new TextRecognizer.Builder(this).build();
        if (!recognizer.isOperational()){
            Toast.makeText(MainActivity.this, "something went wrong!!", Toast.LENGTH_SHORT).show();
        }else {
            Frame frame= new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray= recognizer.detect(frame);
            StringBuilder stringBuilder= new StringBuilder();
            for (int i=0;i<textBlockSparseArray.size();i++){
                TextBlock textBlock =  textBlockSparseArray.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");

            }
            textView.setText(stringBuilder.toString());
            capture.setText("Retake");
            copy.setVisibility(View.VISIBLE);
        }
    }
    private void copyToClipBoard(String text){
        ClipboardManager clipboard =(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip= ClipData.newPlainText("Copied data",text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();

    }

}
