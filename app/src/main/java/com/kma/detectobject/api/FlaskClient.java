package com.kma.detectobject.api;

import com.kma.detectobject.database.Item;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

public interface FlaskClient {
    //upload image
    @Multipart
    @POST("/upload")
    Call<UploadResult> uploadMultipleFiles(
            @PartMap Map<String, RequestBody> files);

    //upload image
    @Multipart
    @POST("/upload")
    Call<DetectResult> uploadDetectMultipleFiles(
            @PartMap Map<String, RequestBody> files);


    ///detections
    @POST("/detections")
    Call<DetectResult> detectFiles(
            @PartMap Map<String, RequestBody> path
    );

    @GET("detect")
    Call<Item> getData();

}
