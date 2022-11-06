package com.sis.clightapp.Interface;


import com.google.gson.JsonObject;
import com.sis.clightapp.model.FCMResponse;
import com.sis.clightapp.model.GsonModel.ItemsMerchant.AddItemsModel;
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemPhotoPath;
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemsDataMerchant;
import com.sis.clightapp.model.WebsocketResponse.WebSocketOTPresponse;
import com.sis.clightapp.model.WebsocketResponse.WebSocketResponse;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiPaths2 {
    @Headers("Content-Type: application/json")
    @POST("/Refresh")
    Call<WebSocketResponse> getotp(@Body JsonObject params);

    @Headers("Content-Type: application/json")
    @POST("/Refresh")
    Call<WebSocketOTPresponse> gettoken(@Body JsonObject params);

    @Headers("Content-Type: application/json")
    @POST("/admin/invoice-to-client")
    Call<WebSocketOTPresponse> flashPay(
            @Header("Authorization") String token, @Body JsonObject params);
    //SetFCMRegToken

    @Headers("Content-Type: application/json")
    @POST("/SetFCMRegToken")
    Call<FCMResponse> setFcmToken(
            @Header("Authorization") String token, @Body JSONObject params);

    @Headers("Content-Type: application/json")
    @POST("/SetFCMRegToken")
    Call<FCMResponse> setFcmToken(
            @Body JsonObject params);

    @Multipart
    //@Headers("Content-Type: application/json")
    @POST("/UserStorage/upload")
    Call<ItemPhotoPath> uploadImage(
            @Header("Authorization") String token, @Part MultipartBody.Part file);

    @Headers("Content-Type: application/json")
    @GET("/UserStorage/inventory/")
    Call<ItemsDataMerchant> getInventoryItems(
            @Header("Authorization") String token
            //        @Body JsonObject params
    );

    @Headers("Content-Type: application/json")
    @POST("/UserStorage/inventory/store")
    Call<AddItemsModel> addInventoryItems(
            @Header("Authorization") String token, @Body JsonObject params);

    @Headers("Content-Type: application/json")
    @PUT("/UserStorage/inventory/{id}/update")
    Call<AddItemsModel> updateInventoryItems(
            @Header("Authorization") String token, @Path("id") int id, @Body JsonObject params);

    @Headers("Content-Type: application/json")
    @DELETE("/UserStorage/inventory/{id}/delete")
    Call<AddItemsModel> deleteInventoryItems(
            @Header("Authorization") String token, @Path("id") int id);

    /*POST http://ip:port/UserStorage/upload
multipart/form-data

fields are---
refresh: string
file: the file to upload

Response: json msg
success: true, message: blah blah, data: the path... this you have to use later
Inventory API endpoints:
GET http://ip:port/UserStorage/inventory/ -> success, message, data: json array of inventory items
POST http://ip:port/UserStorage/inventory/store -> input is inventory item fields + refresh -> success, message, data
PUT http://ip:port/UserStorage/inventory/{id}/update -> input is inventory item fields + refresh -> success, message, data
DELETE http://ip:port/UserStorage/inventory/{id}/delete -> input is inventory item fields + refresh -> success, message, data

merchant fields are :
    name: 'nullable|string',
    upc: 'nullable|string',
    quantity: 'nullable|number',
    price: 'nullable|number',
    img_path: 'nullable|string',

must remember to pass refresh in all api calls
*/
}
