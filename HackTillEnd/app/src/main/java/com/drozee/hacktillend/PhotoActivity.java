package com.drozee.hacktillend;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhotoActivity extends AppCompatActivity {

    private Uri file;
    FirebaseVisionFaceDetectorOptions options;
    FirebaseVisionFaceDetector detector;
    FirebaseStorage mFirebaseStorage;
    StorageReference mMessagesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        mFirebaseStorage = FirebaseStorage.getInstance();
        getSupportActionBar().hide();
        mMessagesReference = mFirebaseStorage.getReference();
        FirebaseApp.initializeApp(PhotoActivity.this);
        options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                        .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .setTrackingEnabled(true)
                        .build();
        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);
        Button cameraButton = (Button) findViewById(R.id.detect);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 100);
                }

        });
}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == RESULT_OK)
        {
            try {
                    final Bitmap photo = (Bitmap) data.getExtras().get("data");
                    final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(photo);

                Task<List<FirebaseVisionFace>> result =
                        detector.detectInImage(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                                            @Override
                                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                                if(faces != null)
                                                {
                                                    Log.i("Result: ", "Face Found");
                                                    mMessagesReference.child("Detected Images").putFile(getImageUri(PhotoActivity.this, photo));
                                                    Toast.makeText(PhotoActivity.this, "Image sent to respective authority", Toast.LENGTH_SHORT).show();

                                                }
                                                // ...
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                            }
                                        });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    public Uri getImageUri(Context context, Bitmap inImage)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    inImage.compress(Bitmap.CompressFormat.PNG,100, bytes);
    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Image", null);
    return Uri.parse(path); }
}
