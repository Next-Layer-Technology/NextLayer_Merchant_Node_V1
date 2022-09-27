package com.sis.clightapp.Interface;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClientBoost {

    //public static final String NEW_BASE_URL= "http://104.128.189.40/boostterminal/api/";
    //public static final String NEW_BASE_URL= "https://merchantnode.nextlayer.live/api/";
                                          //https://mainframe.nextlayer.live/api
    public static final String NEW_BASE_URL= "https://mainframe.nextlayer.live/api/";

    //public static final String NEW_BASE_URL= "http://104.128.189.40/merchantnode/api/";

    public static Retrofit retrofit = null;

    public static Retrofit getRetrofit(){

        if(retrofit==null){
            Gson gson = new GsonBuilder().setLenient().create();
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(httpLoggingInterceptor)
                    .build();
            retrofit = new Retrofit.Builder().baseUrl(NEW_BASE_URL).client(httpClient).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
