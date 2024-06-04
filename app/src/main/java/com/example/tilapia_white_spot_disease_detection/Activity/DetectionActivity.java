package com.example.tilapia_white_spot_disease_detection.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tilapia_white_spot_disease_detection.R;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetectionActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private static final int IMAGE_CAPTURE_REQUEST_CODE = 1;

    private ImageView imageView;
    private Button captureImageButton;
    private TextView resultTextView;
    private TextView confidenceTextView;

    private Interpreter svmInterpreter;
    private Interpreter rfInterpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        imageView = findViewById(R.id.imageView);
        captureImageButton = findViewById(R.id.captureImageButton);
        resultTextView = findViewById(R.id.resultTextView);
        confidenceTextView = findViewById(R.id.confidenceTextView);

        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allPermissionsGranted()) {
                    startCamera();
                } else {
                    ActivityCompat.requestPermissions(DetectionActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                }
            }
        });

        try {
            svmInterpreter = new Interpreter(loadModelFile(this, "svm_model.tflite"));
            rfInterpreter = new Interpreter(loadModelFile(this, "rf_model.tflite"));
        } catch (IOException e) {
            Log.e("DetectionActivity", "Error loading model: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, IMAGE_CAPTURE_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if (imageBitmap != null) {
                classifyImage(imageBitmap);
                imageView.setImageBitmap(imageBitmap);
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void classifyImage(Bitmap image) {
        Bitmap scaledBitmap = ThumbnailUtils.extractThumbnail(image, 224, 224);
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(224 * 224 * 3 * 4)
                .order(ByteOrder.nativeOrder());
        scaledBitmap.getPixels(new int[224 * 224], 0, 224, 0, 0, 224, 224);
        for (int i = 0; i < 224 * 224; ++i) {
            int pixelValue = scaledBitmap.getPixel(i % 224, i / 224);
            inputBuffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f);
            inputBuffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);
            inputBuffer.putFloat((pixelValue & 0xFF) / 255.0f);
        }

        TensorBuffer svmOutputBuffer = TensorBuffer.createFixedSize(new int[]{1, 1}, DataType.FLOAT32);
        TensorBuffer rfOutputBuffer = TensorBuffer.createFixedSize(new int[]{1, 1}, DataType.FLOAT32);

        svmInterpreter.run(inputBuffer, svmOutputBuffer.getBuffer());
        rfInterpreter.run(inputBuffer, rfOutputBuffer.getBuffer());

        float svmConfidence = svmOutputBuffer.getFloatValue(0);
        float rfConfidence = rfOutputBuffer.getFloatValue(0);

        // Combine the results or use them individually
        float combinedConfidence = (svmConfidence + rfConfidence) / 2;

        resultTextView.setText("Combined Result");
        confidenceTextView.setText("Combined Confidence: " + combinedConfidence);
    }

    private ByteBuffer loadModelFile(Context context, String modelFileName) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = context.openFileInput(modelFileName);
            byte[] modelData = new byte[inputStream.available()];
            inputStream.read(modelData);
            return ByteBuffer.wrap(modelData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
