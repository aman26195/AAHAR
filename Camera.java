package com.example.aahar100;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.aahar100.ml. LinearRegressionModel;


public class Camera extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 3;
    private static final int GALLERY_REQUEST_CODE = 1;
    Button camera, gallery;
    ImageView imageView;
    TextView result;
    int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);

        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
            }
        });
    }

    public void classifyImage(Bitmap image) {
        try {
            // Display a toast to indicate that the method is called
//            Toast.makeText(Camera.this, "Method Called", Toast.LENGTH_LONG).show();

            // Load the linear regression model
            LinearRegressionModel model = LinearRegressionModel.newInstance(getApplicationContext());

            // Preprocess the input image
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, 224, 224, true);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(224 * 224 * 3 * 4); // Assuming 32-bit float
            byteBuffer.order(ByteOrder.nativeOrder());
            scaledBitmap.copyPixelsToBuffer(byteBuffer);
            byteBuffer.rewind();

            // Create inputs for reference
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Run model inference and get result
            LinearRegressionModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] outputArray = outputFeature0.getFloatArray();

// Find the index of the maximum value in the array
            int maxIndex = 0;
            float maxValue = outputArray[0];
            for (int i = 1; i < outputArray.length; i++) {
                if (outputArray[i] > maxValue) {
                    maxValue = outputArray[i];
                    maxIndex = i;
                }
            }

// Determine the class based on the index
            String className;
            if (maxIndex == 0) {
                className = "Recyclable";
            } else if (maxIndex == 1) {
                className = "Organic";
            } else {
                className = "Unknown"; // Handle other classes if needed
            }
            String outputMessage = "Output: " + className;
            Toast.makeText(Camera.this, outputMessage, Toast.LENGTH_LONG*2).show();

            // Release model resources
            model.close();
        } catch (IOException e) {
            // Handle the exception
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == 3){
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

                classifyImage(image);
//                Toast.makeText(Camera.this, "khuhugiu", Toast.LENGTH_LONG).show();
            }else {
                Uri dat = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
//                Toast.makeText(Camera.this, "jhkhku", Toast.LENGTH_LONG).show();
//            }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}