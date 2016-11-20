package com.imagespeech.zaher.imagespeech2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.imagespeech.zaher.imagespeech2.api_connection.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * The CameraView program implements an application that
 * automatically take picture and show it in a ImagView.
 * Most of part in this Java file is based on a previous work which is from https://androidmyway.wordpress.com/2012/09/07/capture-image/
 *
 * @author Xiang Zhang
 * @author Abhinav Kalra
 * @version 1.2
 * @since 2016-05-25
 */

// This class implements the camera and picture interface along with listener
@SuppressWarnings("deprecation")
public class CameraView extends Activity implements SurfaceHolder.Callback, OnClickListener {
    private static final String TAG = "CameraTest";
    Camera mCamera;
    boolean mPreviewRunning = false;
    Bitmap image;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.e(TAG, "onCreate");
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.cameraview);
        ImageView img = (ImageView) findViewById(R.id.blankImage);
        if (MainActivity.isBlack)
            img.setBackgroundResource(android.R.color.black);
        else
            img.setBackgroundResource(android.R.color.white);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mSurfaceView.setOnClickListener(this);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    // standard method override
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            if (data != null) {
                mCamera.stopPreview();
                mPreviewRunning = false;
                mCamera.release();

                try {
                    BitmapFactory.Options opts = new BitmapFactory.Options();      // get the bitmap appray representation of pic
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 1920, 1080, false);
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int newWidth = 1920;
                    int newHeight = 1080;
                    // calculate the scale - in this case = 0.4f
                    float scaleWidth = ((float) newWidth) / width;
                    float scaleHeight = ((float) newHeight) / height;

                    // createa matrix for the manipulation
                    Matrix matrix = new Matrix();
                    // resize the bit map
                    //matrix.postScale(scaleWidth, scaleHeight);
                    //  // rotate the Bitmap
                    matrix.postRotate(90);
                    Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                            width, height, matrix, true);
                    Log.e("Setting", "  global variable");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss"); // set the timestamp with the image
                    String temp = sdf.format(new Date());
                    GlobalVariable.pictNum = temp;
                    MainActivity.image.setImageBitmap(resizedBitmap);
                    image = resizedBitmap;
                    Log.e("global variable set ", GlobalVariable.pictNum);
                    sendImageToServer(resizedBitmap);
                    saveToInternalStorage(resizedBitmap, temp);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                setResult(585);

                finish();
            }
        }
    };

    private void sendImageToServer(Bitmap bitmap) {


        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/jpeg");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();
        //  Log.d(TAG, String.format("Profile details => user_id: %d, size of data: %d", 5, data.length));


        final RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), data);
        Date date = new Date();
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("picture", data.toString() + ".jpg", requestFile);

        // add another part within the multipart request
        String descriptionString = "hello, this is description speaking";
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), descriptionString);
        Call<List<String>> call = new RestClient().getApiService().upload(body);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null)
                        printResult(response.body());
                    // Log.d("result", String.valueOf(response.body()));
                    Log.e(TAG, "Done");
                } else {

                    Log.e(TAG, "Error");
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    private void printResult(List<String> list) {
        for (String item : list) {
            Log.d("result", item);
        }
    }

    protected void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    @TargetApi(9)
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated");
        mCamera = Camera.open(MainActivity.cameraID);
    }

    // save the captured image to device storage before transmitting
    private String saveToInternalStorage(Bitmap bitmapImage, String order) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File newpath = new File("/storage/sdcard0/Pictures/Screenshots/");
        File mypath = new File(newpath, order + ".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    @SuppressWarnings("deprecation")
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.e(TAG, "surfaceChanged");
        // XXX stopPreview() will crash if preview is not running
        if (mPreviewRunning) {
            mCamera.stopPreview();
        }

        Camera.Parameters p = mCamera.getParameters();
        List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
        Camera.Size previewSize = previewSizes.get(0);
        Log.d("result", String.valueOf(previewSize.width) + " " + String.valueOf(previewSize.height));
        p.setPreviewSize(previewSize.width, previewSize.height);
        //    p.setPreviewSize(300, 300);

        if (MainActivity.cameraID == 0) {
            String stringFlashMode = p.getFlashMode();
            if (stringFlashMode.equals("torch"))
                p.setFlashMode("on"); // Light is set off, flash is set to normal 'on' mode
            else
                p.setFlashMode("torch");
        }

        mCamera.setParameters(p);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mCamera.startPreview();
        mPreviewRunning = true;
        mCamera.takePicture(null, mPictureCallback, mPictureCallback);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
        //mCamera.stopPreview();
        //mPreviewRunning = false;
        //mCamera.release();
    }

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    public void onClick(View v) {
        // TODO Auto-generated method stub
        mCamera.takePicture(null, mPictureCallback, mPictureCallback);
    }

}