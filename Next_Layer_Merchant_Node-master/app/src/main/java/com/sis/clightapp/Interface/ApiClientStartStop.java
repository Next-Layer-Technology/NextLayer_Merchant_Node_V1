package com.sis.clightapp.Interface;

import android.content.Context;

import com.chuckerteam.chucker.api.ChuckerInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClientStartStop {

    public static final String NEW_BASE_URL = "http://104.128.189.40/boostterminal/ssh-files/";
    public static Retrofit retrofit = null;

    public static Retrofit getRetrofit(Context context) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.MINUTES)
                .readTimeout(3, TimeUnit.MINUTES)
                .addNetworkInterceptor(httpLoggingInterceptor)
                .writeTimeout(3, TimeUnit.MINUTES)
                .addInterceptor(
                        new ChuckerInterceptor.Builder(context).build()
                )
                .build();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(NEW_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(getClient(context))
                    .build();


        }
        return retrofit;
    }

    private static OkHttpClient getClient(Context context) {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .addInterceptor(
                        new ChuckerInterceptor.Builder(context).build()
                )
                .build();
    }
}