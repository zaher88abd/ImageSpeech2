package com.imagespeech.zaher.imagespeech2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.imagespeech.zaher.imagespeech2.api_connection.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "imageSpeech";
    ImageView result;
    Bitmap imageBitmap;
    public static ImageView image;
Button btnLocation;
    public static List<String> stringsTokens = new ArrayList<>();
    private TextToSpeech myTTS;
    private int MY_DATA_CHECK_CODE = 0;

    public static boolean isBlack = true;
    public static int cameraID = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button click = (Button) findViewById(R.id.btnCamera);
        image = (ImageView) findViewById(R.id.imageView);
        btnLocation= (Button) findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateFormat dateFormat = new SimpleDateFormat("h:mm");
                Date date = new Date();
                Log.d(TAG,dateFormat.format(date)+ " You are at the Collider Dalhousie University");
                speakWords( " You are at the Collider Dalhousie University. the time is" +dateFormat.format(date)+" PM");
            }
        });
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(view);
            }
        });
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    public void dispatchTakePictureIntent(View view) {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
        Intent camera = new Intent(MainActivity.this, CameraView.class);
        startActivity(camera);
        //startActivityForResult(camera, 300);
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

    private void speakWords(String speech) {

        //speak straight away
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == cameraID) {
            speech();
        }
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //the user has the necessary data - create the TTS
                myTTS = new TextToSpeech(this, this);
            } else {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    public void onInit(int initStatus) {
        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if (myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    public void speech() {
        new CountDownTimer(100, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                //  chooseLangauge();//choose language
                if (stringsTokens.size() > 0) {
                    String words = "We can see ";
                    for (String word : stringsTokens) {
                        if (word.equals("indoors")) {
                            words = changeTheWords(stringsTokens);
                            break;
                        }
                        words = words + " " + word;
                    }
                    Log.d(TAG, words);
                    Log.d(TAG, "done words");
                    if (words.length() > 0)
                        speakWords(words);
                    else
                        Toast.makeText(getApplicationContext(), "Enter the text", Toast.LENGTH_LONG).show();
                    stringsTokens = new ArrayList<>();
                    speech();
                } else {
                    speech();
                }
            }
        }.start();

    }

    private String changeTheWords(List<String> stringsTokens) {

        String strings = "";
        strings = "You are indoors                                                                               ";
        strings += "We can see";
        for (int i = 0; i < stringsTokens.size(); i++) {
            if (!stringsTokens.get(i).equals("indoors"))
                strings += " " + stringsTokens.get(i);
        }
        Log.d(TAG, strings);
        return strings;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        GlobalVariable.Longitude = Double.toString(location.getLongitude());
        GlobalVariable.Latitude = Double.toString(location.getLatitude());
    }
}
