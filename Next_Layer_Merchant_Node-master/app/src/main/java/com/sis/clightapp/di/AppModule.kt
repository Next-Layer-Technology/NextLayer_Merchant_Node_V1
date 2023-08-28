package com.sis.clightapp.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.GsonBuilder
import com.sis.clightapp.Interface.ApiPaths2
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.R
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData
import com.sis.clightapp.services.BTCService
import com.sis.clightapp.services.FCMService
import com.sis.clightapp.services.LightningService
import com.sis.clightapp.services.SessionService
import com.sis.clightapp.util.CustomTrustManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext


val appModule = module {
    single { FCMService() }
    single(createdAtStart = true) { BTCService(androidApplication()) }
    single { LightningService(androidApplication()) }
    single { SessionService(androidApplication()) }
    factory<ApiPaths2> {
        val sessionService by inject<SessionService>()
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val merchantData: MerchantData? = sessionService.getMerchantData()
        val url = "https://" + merchantData?.container_address + ":" + merchantData?.mws_port


        val sslContext = SSLContext.getInstance("SSL")
        val caCertificateInputStream: InputStream = this.androidContext().resources.openRawResource(R.raw.certificate)
        sslContext.init(null, arrayOf(CustomTrustManager(caCertificateInputStream)), java.security.SecureRandom())

        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, CustomTrustManager(caCertificateInputStream))
            .connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .addNetworkInterceptor(httpLoggingInterceptor)
            .writeTimeout(3, TimeUnit.MINUTES)
            .addInterceptor(
                ChuckerInterceptor.Builder(this.androidContext()).build()
            )
            .build()
        Retrofit.Builder().baseUrl(url).client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build().create(ApiPaths2::class.java)
    }
    single<Webservice> {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                ChuckerInterceptor.Builder(this.androidContext()).build()
            )
            .addNetworkInterceptor(httpLoggingInterceptor)
            .build()
        Retrofit.Builder().baseUrl("https://mainframe.nextlayer.live/api/").client(httpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(Webservice::class.java)
    }

}

fun getMerchantContainerUrl(sessionService: SessionService): String {
    val merchantData: MerchantData? = sessionService.getMerchantData()
    return "https://" + merchantData?.container_address + ":" + merchantData?.mws_port
}