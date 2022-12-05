package com.sis.clightapp.services

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sis.clightapp.model.Channel_BTCResponseData
import com.sis.clightapp.model.currency.CurrentSpecificRateData
import com.sis.clightapp.util.GlobalState
import com.sis.clightapp.util.Resource
import org.json.JSONException
import org.json.JSONObject
import tech.gusavila92.websocketclient.WebSocketClient
import java.net.URI
import java.net.URISyntaxException

class BTCService {
    private lateinit var webSocketClient: WebSocketClient

    private val _currentBtc: MutableLiveData<Resource<CurrentSpecificRateData>> =
        MutableLiveData()
    val currentBtc: LiveData<Resource<CurrentSpecificRateData>>
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
                                val btcRate = CurrentSpecificRateData()
                                btcRate.rateinbitcoin = btcResp.price
                                GlobalState.getInstance().currentSpecificRateData =
                                    btcRate
                                GlobalState.getInstance().channel_btcResponseData =
                                    btcResp
                                _currentBtc.postValue(Resource.success(btcRate))
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
            override fun onException(e: Exception) {}
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

}