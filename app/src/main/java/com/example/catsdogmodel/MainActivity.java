package com.example.catsdogmodel;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.catsdogmodel.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private ImageView  img;
    private Bitmap bitmap;
    private Button btSelect, btCamera, btPredict;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = (ImageView) findViewById(R.id.image);
        btCamera = (Button)findViewById(R.id.camera);
        btPredict = (Button)findViewById(R.id.predict);
        btSelect = (Button)findViewById(R.id.select);

        //CChecking for runtime camera permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                Manifest.permission.CAMERA
            },10);
        }

         btCamera.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                 startActivityForResult(intent,10);

             }
         });
         btSelect.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                 intent.setType("image/*");
                 startActivityForResult(intent,100);

             }
         });
         btPredict.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 bitmap= Bitmap.createScaledBitmap(bitmap,200,200,true);
                 try {
                     Model model = Model.newInstance(getApplicationContext());

                     // Creates inputs for reference.
                     TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 200, 200, 3}, DataType.FLOAT32);
                     TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                     tensorImage.load(bitmap);
                     ByteBuffer byteBuffer = tensorImage.getBuffer();

                     inputFeature0.loadBuffer(byteBuffer);

                     // Runs model inference and gets result.
                     Model.Outputs outputs = model.process(inputFeature0);
                     TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                     // Releases model resources if no longer used.
                     model.close();

                     textView.setText(outputFeature0.getFloatArray()[0] + " \n"+ outputFeature0.getFloatArray()[1]);
//
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

             }
         });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100){
            img.setImageURI(data.getData());
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == 10){
            bitmap = (Bitmap) data.getExtras().get("data");
            img.setImageBitmap(bitmap);

        }
    }
}