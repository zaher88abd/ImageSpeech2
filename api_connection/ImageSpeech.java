package com.imagespeech.zaher.imagespeech2.api_connection;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by Zaher on 9/22/2016.
 */
public interface ImageSpeech {

    //uploadPhoto
    @Multipart
    @POST("ZUPLOAD")
    Call<List<String>> upload(@Part MultipartBody.Part file);


}
