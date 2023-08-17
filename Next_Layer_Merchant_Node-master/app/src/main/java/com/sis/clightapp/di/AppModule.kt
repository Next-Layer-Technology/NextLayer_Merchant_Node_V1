package com.sis.clightapp.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.GsonBuilder
import com.sis.clightapp.Interface.ApiPaths2
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData
import com.sis.clightapp.services.BTCService
import com.sis.clightapp.services.FCMService
import com.sis.clightapp.services.LightningService
import com.sis.clightapp.services.SessionService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

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
        Retrofit.Builder().baseUrl(url).client(getOkHttpBuilder().build())
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

fun getOkHttpBuilder(): OkHttpClient.Builder =
    getUnsafeOkHttpClient()

private fun getUnsafeOkHttpClient(): OkHttpClient.Builder =
    try {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts: Array<TrustManager> = arrayOf(
            object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate?>?,
                                                authType: String?) = Unit

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate?>?,
                                                authType: String?) = Unit

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )
        // Install the all-trusting trust manager
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
        val builder = OkHttpClient.Builder()
        builder.sslSocketFactory(sslSocketFactory,
            trustAllCerts[0] as X509TrustManager)
        builder.hostnameVerifier { _, _ -> true }
        builder
    } catch (e: Exception) {
        throw RuntimeException(e)
    }



private fun getUnsafeOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor, scope: Scope): OkHttpClient? {
    return try {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                    return arrayOf()
                }
            }
        )

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory
        val trustManagerFactory: TrustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers: Array<TrustManager> =
            trustManagerFactory.trustManagers
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            "Unexpected default trust managers:" + trustManagers.contentToString()
        }

        val trustManager =
            trustManagers[0] as X509TrustManager


        var builder = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .addNetworkInterceptor(httpLoggingInterceptor)
            .writeTimeout(3, TimeUnit.MINUTES)
            .addInterceptor(
                ChuckerInterceptor.Builder(scope.androidContext()).build()
            )
        builder.sslSocketFactory(sslSocketFactory, trustManager)
        builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
        builder.build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

fun getMerchantContainerUrl(sessionService: SessionService): String {
    val merchantData: MerchantData? = sessionService.getMerchantData()
    return "http://" + merchantData?.container_address + ":" + merchantData?.mws_port
}