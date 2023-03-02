package com.sis.clightapp.services

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

class BTCService {
    private lateinit var webSocketClient: WebSocketClient
    var btcPrice = 0.0

    private val _currentBtc: MutableLiveData<Resource<Channel_BTCResponseData>> =
        MutableLiveData()
    val currentBtc: LiveData<Resource<Channel_BTCResponseData>>
        get() = _currentBtc

    init {
        subscribeChannel()
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
    fun setStatic(){
        btcPrice = 23779.00
        val btcResp = Channel_BTCResponseData()
        btcResp.price = 23779.00
        _currentBtc.postValue(Resource.success(btcResp))
    }
}