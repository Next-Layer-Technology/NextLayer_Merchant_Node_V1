package com.sis.clightapp.Interface;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient2 {
//    https://airhomerestaurant.com/api/v1/
//    http://98.226.215.246:8095
    //public static final String NEW_BASE_URL = "http://98.226.215.246:8095";
    public static final String NEW_BASE_URL = "http://73.36.65.41:34001";//ws://73.36.65.41:34001/SendCommands


    public static Retrofit retrofit = null;

    public static Retrofit getRetrofit() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);


        MerchantData merchantData = GlobalState.getInstance().getMerchantData();
        if(merchantData != null) {
            String url = "http://"+merchantData.getContainer_address()+":"+merchantData.getMws_port();
            if (retrofit == null) {

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(3, TimeUnit.MINUTES)
                        .readTimeout(3, TimeUnit.MINUTES)
                        .addNetworkInterceptor(httpLoggingInterceptor)
                        .writeTimeout(3, TimeUnit.MINUTES)
                        .build();
                retrofit = new Retrofit.Builder().baseUrl(url).client(okHttpClient)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

            }
        }else {

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.MINUTES)
                    .readTimeout(3, TimeUnit.MINUTES)
                    .addNetworkInterceptor(httpLoggingInterceptor)
                    .writeTimeout(3, TimeUnit.MINUTES)
                    .build();
           Retrofit retrofit = new Retrofit.Builder().baseUrl(NEW_BASE_URL).client(okHttpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
           return retrofit;

        }
        return retrofit;
    }
}
