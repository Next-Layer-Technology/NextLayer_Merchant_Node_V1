package com.sis.clightapp.services

import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sis.clightapp.model.Channel_BTCResponseData
import com.sis.clightapp.util.Resource
import org.json.JSONException
import org.json.JSONObject
import tech.gusavila92.websocketclient.WebSocketClient
import java.net.URI
import java.net.URISyntaxException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class BTCService(app: Application) {
    private lateinit var webSocketClient: WebSocketClient
    var btcPrice = 0.0

    private val _currentBtc: MutableLiveData<Resource<Channel_BTCResponseData>> =
        MutableLiveData()
    val currentBtc: LiveData<Resource<Channel_BTCResponseData>>
        get() = _currentBtc

    init {
        subscribeChannel()
        if (Settings.Secure.getString(
                app.contentResolver,
                Settings.Secure.ANDROID_ID
            ) == "4609ce6a958ba817"
        ) {
            Log.d(this.javaClass.name, "This is Pitam's Phone")
            setStatic()
        }

    }

    private fun subscribeChannel() {
        val uri: URI = try {
            URI("wss://ws.bitstamp.net/")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen() {
                val json =
                    "{\"event\":\"bts:subscribe\",\"data\":{\"channel\":\"live_trades_btcusd\"}}"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocketClient.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                }
                Log.i("WebSocket", "Session is starting")
            }

            override fun onTextReceived(s: String) {
                Log.i("WebSocket", "Message received")
                if (s.isNotEmpty()) {
                    try {
                        val jsonObject = JSONObject(s)
                        val subscription = jsonObject.getString("event")
                        val objects = jsonObject.getJSONObject("data")
                        if (subscription != "bts:subscription_succeeded") {
                            try {
                                val btcResp = Channel_BTCResponseData()
                                btcResp.id = objects.getInt("id")
                                btcResp.timestamp =
                                    objects.getString("timestamp")
                                btcResp.amount = objects.getDouble("amount")
                                btcResp.amount_str =
                                    objects.getString("amount_str")
                                btcResp.price = objects.getDouble("price")
                                btcResp.price_str =
                                    objects.getString("price_str")
                                btcResp.type = objects.getInt("type")
                                btcResp.microtimestamp =
                                    objects.getString("microtimestamp")
                                btcResp.buy_order_id =
                                    objects.getInt("buy_order_id")
                                btcResp.sell_order_id =
                                    objects.getInt("sell_order_id")
                                _currentBtc.postValue(Resource.success(btcResp))
                                btcPrice = btcResp.price
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    } catch (ignored: JSONException) {
                    }
                }
            }

            override fun onBinaryReceived(data: ByteArray) {}
            override fun onPingReceived(data: ByteArray) {}
            override fun onPongReceived(data: ByteArray) {}
            override fun onException(e: Exception) {
                Log.e("WebSocket", e.localizedMessage)
            }

            override fun onCloseReceived() {
                Log.i("WebSocket", "Closed ")
                println("onCloseReceived")
            }
        }

        // Install the all-trusting trust manager
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
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
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
        webSocketClient.setSSLSocketFactory(sslSocketFactory)
        webSocketClient.setConnectTimeout(100000)
        webSocketClient.setReadTimeout(600000)
        webSocketClient.enableAutomaticReconnection(5000)
        webSocketClient.connect()
    }

    fun btcToUsd(btc: Double): Double {
        return if (btcPrice != 0.0) {
            val priceInUSD = btcPrice * btc
            priceInUSD
        } else {
            0.0
        }
    }

    fun setStatic() {
        btcPrice = 23779.00
        val btcResp = Channel_BTCResponseData()
        btcResp.price = 23779.00
        _currentBtc.postValue(Resource.success(btcResp))
    }
}