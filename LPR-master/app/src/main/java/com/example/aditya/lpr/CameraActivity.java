package com.example.aditya.lpr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.aditya.lpr.views.CameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity {

    private static final String LOG_TAG = CameraActivity.class.getSimpleName();

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.Parameters params;

    public static final int MEDIA_TYPE_IMAGE = 1;
    private static int image_counter = 1; // This is used while naming the images, see getOutputMediaFile()
    private static Context mContext;

    private enum FlashState {ON,OFF,AUTO};
    FlashState currentFlashState = FlashState.AUTO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);
        mContext = getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();


        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // Create an instance of Camera
                mCamera = getCameraInstance();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                // get Camera parameters
                params = mCamera.getParameters();
                // set the focus mode
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                // set Camera parameters
                mCamera.setParameters(params);

                // Create our Preview view and set it as the content of our activity.
                mPreview = new CameraPreview(CameraActivity.this, mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);
            }
        }.execute();

        addButtonListeners();

    }

    public void addButtonListeners() {
        // Add a listener to the Capture button
        Button captureButton = (Button)findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        Log.v(LOG_TAG,"Clicked the capture button!");
                        mCamera.takePicture(mShutter, null, mPicture);
                    }
                }
        );

        final Button flashButton = (Button)findViewById(R.id.button_flash);
        flashButton.setText("Flash Auto");
        flashButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (currentFlashState) {
                            case OFF:
                                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                                currentFlashState = FlashState.ON;
                                flashButton.setText("Flash On");
                                break;
                            case ON:
                                params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                                currentFlashState = FlashState.AUTO;
                                flashButton.setText("Flash Auto");
                                break;
                            case AUTO:
                                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                currentFlashState = FlashState.OFF;
                                flashButton.setText("Flash Off");
                                break;
                        }
                        mCamera.setParameters(params);
                    }
                }
        );

        Button galleryButton = (Button)findViewById(R.id.button_gallery);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        int rear_camera_id = findBackFacingCamera();
        try {
            c = Camera.open(rear_camera_id); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
            Log.d(LOG_TAG,"Exception raised in getCameraInstance()");
        }
        return c; // returns null if camera is unavailable
    }

    private static int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the rear facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Log.v(LOG_TAG,"onPictureTaken");

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(LOG_TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            mCamera.stopPreview();
            mCamera.startPreview();

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    private final Camera.ShutterCallback mShutter = new Camera.ShutterCallback() {
        public void onShutter() {
            Log.v(LOG_TAG,"Shutter sound given");
            AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    @Nullable
    private static File getOutputMediaFile(int type){

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        Log.v(LOG_TAG,"Inside: getOutputMediaFile");
        String CURRENT_MEDIA_STATE = Environment.getExternalStorageState();
        if(!CURRENT_MEDIA_STATE.equals(Environment.MEDIA_MOUNTED)) {
            Log.d(LOG_TAG,"Media is not mounted!");
            return null;
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "LPRApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("LPRApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()).toString();
        Log.v(LOG_TAG,"Timestamp is: " + timeStamp);
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + image_counter + "_" + timeStamp + ".jpg");
            image_counter++;
        } else {
            return null;
        }

        return mediaFile;
    }

}
