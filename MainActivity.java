package com.imagespeech.zaher.imagespeech2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.imagespeech.zaher.imagespeech2.api_connection.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "imageSpeech";
    ImageView result;
    Bitmap imageBitmap;
    public static   ImageView image;

    public static boolean isBlack = true;
    public static int cameraID = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button click = (Button) findViewById(R.id.btnCamera);
        image = (ImageView) findViewById(R.id.imageView);

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(view);
            }
        });
    }

    public void dispatchTakePictureIntent(View view) {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
        Intent camera = new Intent(MainActivity.this,CameraView.class);
        startActivity(camera);
        //startActivityForResult(camera, 300);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            imageBitmap = (Bitmap) extras.get("data");
//            result.setImageBitmap(imageBitmap);
//            sendImageToServer(imageBitmap);
//        }
    }
    private void sendImageToServer(Bitmap bitmap) {


        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/jpeg");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();
        //  Log.d(TAG, String.format("Profile details => user_id: %d, size of data: %d", 5, data.length));


        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), data);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("picture", "testphoto.jpg", requestFile);

        // add another part within the multipart request
        String descriptionString = "hello, this is description speaking";
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), descriptionString);
        Call<List<String>> call = new RestClient().getApiService().upload(body);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful())

                Log.e(TAG, "Done");
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }
}
