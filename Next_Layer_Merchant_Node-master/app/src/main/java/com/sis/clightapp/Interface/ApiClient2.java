//package com.sis.clightapp.Interface;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.sis.clightapp.services.SessionService;
//import com.sis.clightapp.util.GlobalState;
//import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
//
//import java.util.concurrent.TimeUnit;
//
//import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//import retrofit2.converter.scalars.ScalarsConverterFactory;
//
//public class ApiClient2 {
//    public static Retrofit retrofit = null;
//    public static Retrofit getRetrofit() {
//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .create();
//        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//        httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
//        MerchantData merchantData = GlobalState.getInstance().getMerchantData();
//        if (merchantData != null) {
//            new SessionService().getMerchantData();
//            String url = "http://" + merchantData.getContainer_address() + ":" + merchantData.getMws_port();
//            if (retrofit == null) {
//                OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                        .connectTimeout(3, TimeUnit.MINUTES)
//                        .readTimeout(3, TimeUnit.MINUTES)
//                        .addNetworkInterceptor(httpLoggingInterceptor)
//                        .writeTimeout(3, TimeUnit.MINUTES)
//                        .build();
//                retrofit = new Retrofit.Builder().baseUrl(url).client(okHttpClient)
//                        .addConverterFactory(ScalarsConverterFactory.create())
//                        .addConverterFactory(GsonConverterFactory.create(gson))
//                        .build();
//
//            }
//        }
//        return retrofit;
//    }
//}
