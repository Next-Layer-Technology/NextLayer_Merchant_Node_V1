package com.sis.clightapp.services

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.sis.clightapp.model.GsonModel.Invoice
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeers
import com.sis.clightapp.model.WebsocketResponse.MWSWebSocketResponse
import com.sis.clightapp.util.GlobalState
import com.sis.clightapp.util.Resource
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class LightningService(val context: Context) {
    private var gdaxUrl: String
    private val gson = Gson()

    private var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private var client = OkHttpClient()
    private val _paymentResult: MutableLiveData<Resource<Invoice>> = MutableLiveData()
    val paymentResult: LiveData<Resource<Invoice>>
        get() = _paymentResult

    private val _createInvoiceResult: MutableLiveData<Resource<MWSWebSocketResponse>> =
        MutableLiveData()
    val createInvoiceResult: LiveData<Resource<MWSWebSocketResponse>>
        get() = _createInvoiceResult

    private val _peersResult: MutableLiveData<Resource<ListPeers>> =
        MutableLiveData()
    val peersResult: LiveData<Resource<ListPeers>>
        get() = _peersResult

    init {
        gdaxUrl = sharedPreferences.getString("mws_command", "") ?: ""
    }

    fun confirmPayment(label: String) {
        val request: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val token: String = sharedPreferences.getString("accessToken", "") ?: ""
                val json =
                    "{\"token\" : \"$token\", \"commands\" : [\"lightning-cli listinvoices $label\"] }"
                try {
                    val obj = JSONObject(json)
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    _paymentResult.postValue(Resource.error(message = "Error sending request."))

                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                val jsonArray: JSONArray
                var json = ""
                try {
                    jsonArray = JSONObject(text).getJSONArray("invoices")
                    json = jsonArray[0].toString()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                val invoice = gson.fromJson(
                    json,
                    Invoice::class.java
                )
                GlobalState.getInstance().invoice = invoice
                _paymentResult.postValue(Resource.success(invoice))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                webSocket.cancel()
                Log.e("TAG", "CLOSE: $code $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _paymentResult.postValue(Resource.error(message = "Error"))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        client.dispatcher.executorService.shutdown()
    }

    fun createInvoice(
        rMSatoshi: String?,
        label: String?,
        description: String?,
    ) {
        val request: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val token =
                    sharedPreferences.getString("accessToken", "")
                val json =
                    """
                        {
                            "token" : "$token", 
                            "commands" : ["lightning-cli invoice $rMSatoshi $label $description 300"] 
                        }
                        """.trimIndent()
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e("LightningService", t.message + "$json\"")
                    _createInvoiceResult.postValue(Resource.error(message = "Error sending request"))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG_onMessage", "MESSAGE: $text")
                val response = gson.fromJson(
                    text,
                    MWSWebSocketResponse::class.java
                )
                _createInvoiceResult.postValue(Resource.success(response))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                webSocket.cancel()
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                _createInvoiceResult.postValue(Resource.error(message = t.message.toString()))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        client.dispatcher.executorService.shutdown()
    }

    fun listPeers() {
        val request: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val token =
                    sharedPreferences.getString("accessToken", "")
                val json =
                    "{\"token\" : \"$token\", \"commands\" : [\"lightning-cli listpeers\"] }"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                    _peersResult.postValue(Resource.error(message = "Error sending request"))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                val jsonObject = JSONObject(text)
                val jsonArr = jsonObject.getJSONArray("peers")
                val jsonObj: JSONObject = jsonArr.getJSONObject(0)
                val funds = gson.fromJson(jsonObj.toString(), ListPeers::class.java)
                _peersResult.postValue(Resource.success(funds))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                webSocket.cancel()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {}
            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                _peersResult.postValue(Resource.error(message = t.message.toString()))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        client.dispatcher.executorService.shutdown()
    }

}