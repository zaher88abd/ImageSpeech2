package com.imagespeech.zaher.imagespeech2.api_connection;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RestClient {
  private static final String BASE_URL = "http://teddyapi.azurewebsites.net/api/MainT/";


    private ImageSpeech imageSpeechService;

    public ImageSpeech getApiService() {
        return imageSpeechService;
    }

    public RestClient() {
//        OkHttpClient client = new OkHttpClient.Builder()
//                .readTimeout(1, TimeUnit.SECONDS)
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
                .build();
        imageSpeechService = retrofit.create(ImageSpeech.class);
    }
}
