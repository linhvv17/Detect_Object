package com.kma.detectobject.api;

import com.kma.detectobject.database.Item;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DetectAPI {


    @GET("detect")
    Call<Item> getData();

}
