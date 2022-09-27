package com.sis.clightapp.Interface;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiFCM {
    @Headers("Content-Type: application/json")
    @POST("send")
    Call<Object> FcmHitForToken(@Body JsonObject body);
}
