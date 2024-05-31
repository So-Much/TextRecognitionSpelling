package com.example.textrecognitionspelling;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final int REQUEST_IMAGE_SELECT = 1;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        initializeCamera
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                imageCapture = new ImageCapture.Builder().build();
                PreviewView previewView = findViewById(R.id.previewView);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                Log.e(TAG, "initial camera failed: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));

//        Capture button
        Button captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(v -> {
//            take photo
            File photoFile = new File(
                    getExternalFilesDir(null),
                    new SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg"
            );

            imageCapture.takePicture(
                    new ImageCapture.OutputFileOptions.Builder(photoFile).build(),
                    ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Uri savedUri = Uri.fromFile(photoFile);
//                            Recognize text
                            try {
                                InputImage image = InputImage.fromFilePath(MainActivity.this, savedUri);
                                TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                                recognizer.process(image)
                                        .addOnSuccessListener(visionText -> {
//                                            displayRecognizedText
                                            Log.d(TAG, "Recognize text success: " + visionText.getText());
                                            for (Text.TextBlock block : visionText.getTextBlocks()) {
                                                String blockText = block.getText();
                                                Rect blockFrame = block.getBoundingBox();

                                                TextView textView = new TextView(MainActivity.this);
                                                textView.setText(blockText);
                                                textView.setTextColor(Color.BLACK);
                                                textView.setTextSize(20);

                                                ImageView imageView = new ImageView(MainActivity.this);
                                                imageView.setImageResource(R.drawable.bubblechat);
                                                LinearLayout parentLayout = findViewById(R.id.parentLayout);
                                                parentLayout.addView(textView);
                                                parentLayout.addView(imageView);

                                                LinearLayout.LayoutParams textViewParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
                                                textViewParams.leftMargin = blockFrame.left;
                                                textViewParams.topMargin = blockFrame.top;
                                                textView.setLayoutParams(textViewParams);

                                                LinearLayout.LayoutParams imageViewParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                                                imageViewParams.leftMargin = blockFrame.left - 10;
                                                imageViewParams.topMargin = blockFrame.top;
                                                imageView.setLayoutParams(imageViewParams);


                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Recognize text failed: " + e.getMessage());
                                        });
                            } catch (Exception e) {
                                Log.e(TAG, "Recognize text failed: " + e.getMessage());
                            }

                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.e(TAG, "Take picture failed: " + exception.getMessage());
                        }
                    }
            );
        });
//        Select button
        Button selectButton = findViewById(R.id.selectButton);
        selectButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if (o.getResultCode() == RESULT_OK) {
                                Uri uri = o.getData().getData();
                                try {
                                    InputImage image = InputImage.fromFilePath(MainActivity.this, uri);
                                    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                                    recognizer.process(image)
                                            .addOnSuccessListener(visionText -> {
                                                Log.d(TAG, "Recognize text success: " + visionText.getText());
                                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                                    String blockText = block.getText();
                                                    Rect blockFrame = block.getBoundingBox();

                                                    TextView textView = new TextView(MainActivity.this);
                                                    textView.setText(blockText);
                                                    textView.setTextColor(Color.BLACK);
                                                    textView.setTextSize(20);

                                                    ImageView imageView = new ImageView(MainActivity.this);
                                                    imageView.setImageResource(R.drawable.bubblechat);
                                                    LinearLayout parentLayout = findViewById(R.id.parentLayout);
                                                    parentLayout.addView(textView);
                                                    parentLayout.addView(imageView);

                                                    LinearLayout.LayoutParams textViewParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
                                                    textViewParams.leftMargin = blockFrame.left;
                                                    textViewParams.topMargin = blockFrame.top;
                                                    textView.setLayoutParams(textViewParams);

                                                    LinearLayout.LayoutParams imageViewParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                                                    imageViewParams.leftMargin = blockFrame.left - 10;
                                                    imageViewParams.topMargin = blockFrame.top;
                                                    imageView.setLayoutParams(imageViewParams);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Recognize text failed: " + e.getMessage());
                                            });
                                } catch (Exception e) {
                                    Log.e(TAG, "Recognize text failed: " + e.getMessage());
                                }
                            }
                        }
                    }
            );
        });
    }
}