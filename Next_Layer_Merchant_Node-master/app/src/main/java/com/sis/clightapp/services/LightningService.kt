package com.sis.clightapp.services

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.sis.clightapp.model.GsonModel.Invoice
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeers
import com.sis.clightapp.model.GsonModel.Sendreceiveableresponse
import com.sis.clightapp.model.REST.GetRouteResponse
import com.sis.clightapp.model.WebsocketResponse.MWSWebSocketResponse
import com.sis.clightapp.util.GlobalState
import com.sis.clightapp.util.Resource
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import kotlin.String as String1

class LightningService(val context: Context) {
    private var gdaxUrl: String1
    private val gson = Gson()

    private var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private var client = OkHttpClient()

    init {
        gdaxUrl = sharedPreferences.getString("mws_command", "") ?: ""
    }

    fun confirmPayment(label: String1): MutableLiveData<Resource<Invoice>> {
        val paymentResult: MutableLiveData<Resource<Invoice>> = MutableLiveData()
        paymentResult.postValue(Resource.loading())
        val request: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val token: String1 = sharedPreferences.getString("accessToken", "") ?: ""
                val json =
                    """{"token" : "$token", "commands" : ["lightning-cli listinvoices $label"] }"""
                try {
                    val obj = JSONObject(json)
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    paymentResult.postValue(Resource.error(message = "Error sending request."))
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String1) {
                Log.e("TAG", "MESSAGE: $text")
                try {
                    val jsonArray = JSONObject(text).getJSONArray("invoices")
                    val invoice = gson.fromJson(
                        jsonArray[0].toString(),
                        Invoice::class.java
                    )
                    GlobalState.getInstance().invoice = invoice
                    paymentResult.postValue(Resource.success(invoice))
                } catch (e: JSONException) {
                    paymentResult.postValue(Resource.error(message = "Error parsing json"))
                    e.printStackTrace()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String1) {
                webSocket.close(1000, null)
                webSocket.cancel()
                Log.e("TAG", "CLOSE: $code $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                paymentResult.postValue(Resource.error(message = "Error"))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        client.dispatcher.executorService.shutdown()
        return paymentResult
    }

    fun createInvoice(
        rMSatoshi: String1?,
        label: String1?,
        description: String1?,
    ): MutableLiveData<Resource<MWSWebSocketResponse>> {
        val createInvoiceResult: MutableLiveData<Resource<MWSWebSocketResponse>> =
            MutableLiveData()
        createInvoiceResult.postValue(Resource.loading())
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
                    createInvoiceResult.postValue(Resource.error(message = "Error sending request"))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String1) {
                Log.e("TAG_onMessage", "MESSAGE: $text")
                val response = gson.fromJson(
                    text,
                    MWSWebSocketResponse::class.java
                )
                createInvoiceResult.postValue(Resource.success(response))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String1) {
                webSocket.close(1000, null)
                webSocket.cancel()
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                createInvoiceResult.postValue(Resource.error(message = t.message.toString()))
                Log.e("LightningService", t.message.toString())
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        client.dispatcher.executorService.shutdown()
        return createInvoiceResult
    }

    fun listPeers(): MutableLiveData<Resource<ListPeers>> {
        val peersResult: MutableLiveData<Resource<ListPeers>> =
            MutableLiveData()

        peersResult.postValue(Resource.loading())
        val request: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val token =
                    sharedPreferences.getString("accessToken", "")
                val json =
                    """{"token" : "$token", "commands" : ["lightning-cli listpeers"] }"""
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                    peersResult.postValue(Resource.error(message = "Error sending request"))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String1) {
                Log.e("TAG", "MESSAGE: $text")
                val jsonObject = JSONObject(text)
                val jsonArr = jsonObject.getJSONArray("peers")
                val jsonObj: JSONObject = jsonArr.getJSONObject(0)
                val funds = gson.fromJson(jsonObj.toString(), ListPeers::class.java)
                peersResult.postValue(Resource.success(funds))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String1) {
                webSocket.close(1000, null)
                webSocket.cancel()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String1) {}
            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                peersResult.postValue(Resource.error(message = t.message.toString()))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        client.dispatcher.executorService.shutdown()
        return peersResult
    }

    fun sendReceivables(
        routingNodeId: String1,
        msatoshiReceivable: String1
    ): MutableLiveData<Resource<Sendreceiveableresponse>> {
        val receivableResult: MutableLiveData<Resource<Sendreceiveableresponse>> =
            MutableLiveData()

        receivableResult.postValue(Resource.loading())
        val request: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val token =
                    sharedPreferences.getString("accessToken", "")
                val json =
                    """{"token" : "$token", "commands" : ["lightning-cli keysend $routingNodeId $msatoshiReceivable"] }"""
                try {
                    val obj = JSONObject(json)
                    Log.d(TAG, obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e(TAG, t.message.toString())
                    receivableResult.postValue(Resource.error(message = "Error sending request."))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String1) {
                Log.e(TAG, text)
                val resp = gson.fromJson(text, Sendreceiveableresponse::class.java)
                receivableResult.postValue(Resource.success(resp))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String1) {
                webSocket.close(1000, null)
                webSocket.cancel()
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                receivableResult.postValue(Resource.error(message = t.message.toString()))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        client.dispatcher.executorService.shutdown()
        return receivableResult
    }

    fun getRoute(nodeId: String1, receivable: String1): MutableLiveData<Resource<kotlin.String>> {
        val routeResult: MutableLiveData<Resource<String1>> =
            MutableLiveData()
        val request: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val token =
                    sharedPreferences.getString("accessToken", "")
                val json =
                    """{"token" : "$token", "commands" : ["lightning-cli getroute $nodeId $receivable 1"] }"""
                try {
                    val obj = JSONObject(json)
                    Log.d(TAG, obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e(TAG, "Could not parse malformed JSON: \"$json\"")
                    routeResult.postValue(Resource.error(message = "Error sending request."))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String1) {
                Log.e("TAG", "MESSAGE: $text")
                val resp = gson.fromJson(text, GetRouteResponse::class.java)
                val fee =
                    (receivable.toLong() - (resp.routes[0].msatoshi - receivable.toLong())).toString()
                routeResult.postValue(Resource.success(fee))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String1) {
                webSocket.close(1000, null)
                webSocket.cancel()
                Log.e(TAG, "CLOSE: $code $reason")
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                routeResult.postValue(Resource.error(message = t.message.toString()))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        client.dispatcher.executorService.shutdown()
        return routeResult
    }

    companion object {
        val TAG: String1 = "LightningService"
    }

}