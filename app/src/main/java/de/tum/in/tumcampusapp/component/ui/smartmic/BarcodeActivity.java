package de.tum.in.tumcampusapp.component.ui.smartmic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.CommonStatusCodes;
//import com.google.android.gms.vision.CameraSource;
//import com.google.android.gms.vision.Detector;
//import com.google.android.gms.vision.barcode.Barcode;
//import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import de.tum.in.tumcampusapp.R;

public class BarcodeActivity extends AppCompatActivity {

    Activity thisActivity;

    SurfaceView cameraPreview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        setActivity(this);
        cameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
//        createCameraSource();
    }

    private void setActivity(Activity activity){
        thisActivity = activity;
    }

//    private void createCameraSource() {
//
//        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();
//        final CameraSource cameraSource = new CameraSource.Builder(this, barcodeDetector).build();
//
//        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                if(ActivityCompat.checkSelfPermission(BarcodeActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
//                    ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.CAMERA}, 0);
//                }
//                else{
//                    try {
//                        cameraSource.start(cameraPreview.getHolder());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//                cameraSource.stop();
//            }
//        });
//
//        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
//            @Override
//            public void release() {
//
//            }
//
//            @Override
//            public void receiveDetections(Detector.Detections<Barcode> detections) {
//                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
//                if (barcodes.size()>=0){
//                    Intent intent = new Intent();
//                    intent.putExtra("barcode", barcodes.valueAt(0));
//                    setResult(CommonStatusCodes.SUCCESS, intent);
//                    finish();
//                }
//            }
//        });
//
//    }

}
