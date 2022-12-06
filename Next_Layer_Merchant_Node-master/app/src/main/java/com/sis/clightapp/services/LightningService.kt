package com.sis.clightapp.services

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.sis.clightapp.model.GsonModel.Invoice
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeers
import com.sis.clightapp.model.GsonModel.Sendreceiveableresponse
import com.sis.clightapp.model.Invoices.InvoicesResponse
import com.sis.clightapp.model.REST.GetRouteResponse
import com.sis.clightapp.model.RefundsData.RefundResponse
import com.sis.clightapp.model.WebsocketResponse.MWSWebSocketResponse
import com.sis.clightapp.util.GlobalState
import com.sis.clightapp.util.Resource
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import kotlin.String as String

class LightningService(val context: Context) {
    private var gdaxUrl: String
    private val gson = Gson()

    private var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private var client = OkHttpClient()

    init {
        gdaxUrl = sharedPreferences.getString("mws_command", "") ?: ""
    }

    fun confirmPayment(label: String): MutableLiveData<Resource<Invoice>> {
        val paymentResult: MutableLiveData<Resource<Invoice>> = MutableLiveData(Resource.loading())
        paymentResult.postValue(Resource.loading())
        val request: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val token: String = sharedPreferences.getString("accessToken", "") ?: ""
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

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                try {
                    val jsonArray = JSONObject(text).getJSONArray("invoices")
                    val invoice = gson.fromJson(
                        jsonArray[0].toString(),
                        Invoice::class.java
                    )
                    paymentResult.postValue(Resource.success(invoice))
                } catch (e: JSONException) {
                    paymentResult.postValue(Resource.error(message = "Error parsing json"))
                    e.printStackTrace()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                webSocket.cancel()
                Log.e("TAG", "CLOSE: $code $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                paymentResult.postValue(Resource.error(message = "Error"))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        
        return paymentResult
    }

    fun createInvoice(
        rMSatoshi: String?,
        label: String?,
        description: String?,
    ): MutableLiveData<Resource<MWSWebSocketResponse>> {
        val createInvoiceResult =
            MutableLiveData<Resource<MWSWebSocketResponse>>(Resource.loading())
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

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG_onMessage", "MESSAGE: $text")
                val response = gson.fromJson(
                    text,
                    MWSWebSocketResponse::class.java
                )
                createInvoiceResult.postValue(Resource.success(response))
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
                createInvoiceResult.postValue(Resource.error(message = t.message.toString()))
                Log.e("LightningService", t.message.toString())
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        
        return createInvoiceResult
    }

    fun listPeers(): MutableLiveData<Resource<ListPeers>> {
        val peersResult: MutableLiveData<Resource<ListPeers>> =
            MutableLiveData(Resource.loading())

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

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                val jsonObject = JSONObject(text)
                val jsonArr = jsonObject.getJSONArray("peers")
                val jsonObj: JSONObject = jsonArr.getJSONObject(0)
                val funds = gson.fromJson(jsonObj.toString(), ListPeers::class.java)
                peersResult.postValue(Resource.success(funds))
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
                peersResult.postValue(Resource.error(message = t.message.toString()))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        
        return peersResult
    }

    fun sendReceivables(
        routingNodeId: String,
        msatoshiReceivable: String
    ): MutableLiveData<Resource<Sendreceiveableresponse>> {
        val receivableResult: MutableLiveData<Resource<Sendreceiveableresponse>> =
            MutableLiveData(Resource.loading())

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

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e(TAG, text)
                val resp = gson.fromJson(text, Sendreceiveableresponse::class.java)
                receivableResult.postValue(Resource.success(resp))
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
                receivableResult.postValue(Resource.error(message = t.message.toString()))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        
        return receivableResult
    }

    fun getRoute(nodeId: String, receivable: String): MutableLiveData<Resource<kotlin.String>> {
        val routeResult: MutableLiveData<Resource<String>> =
            MutableLiveData(Resource.loading())
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

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                val resp = gson.fromJson(text, GetRouteResponse::class.java)
                val fee =
                    (receivable.toLong() - (resp.routes[0].msatoshi - receivable.toLong())).toString()
                routeResult.postValue(Resource.success(fee))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
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
        
        return routeResult
    }

    fun refundList(): MutableLiveData<Resource<RefundResponse>> {
        val liveData: MutableLiveData<Resource<RefundResponse>> =
            MutableLiveData(Resource.loading())
        val request: Request = Request.Builder().url(gdaxUrl).build()
        val listener: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val token = sharedPreferences.getString("accessToken", "")
                val json =
                    """{"token" : "$token", "commands" : ["lightning-cli listsendpays"] }"""
                try {
                    val obj = JSONObject(json)
                    Log.d(TAG, obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e(TAG, "Could not parse malformed JSON: \"$json\"")
                    liveData.postValue(Resource.error(message = "Error sending request"))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e(TAG, "MESSAGE: $text")
                try {
                    val jsonObject = JSONObject(text)
                    if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                        webSocket.close(1000, null)
                        webSocket.cancel()
                    } else {
                        val refundResponse = gson.fromJson(text, RefundResponse::class.java)
                        liveData.postValue(Resource.success(refundResponse))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                    liveData.postValue(Resource.error(message = "Error parsing json"))
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                webSocket.cancel()
                Log.e(TAG, "CLOSE: $code $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                liveData.postValue(Resource.error(message = t.message.toString()))

            }
        }
        client.newWebSocket(request, listener)
        
        return liveData
    }

    fun invoiceList(): MutableLiveData<Resource<InvoicesResponse>> {
        val liveData = MutableLiveData<Resource<InvoicesResponse>>(Resource.loading())
        val request = Request.Builder().url(gdaxUrl).build()
        val listener: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val token = sharedPreferences.getString("accessToken", "")
                val json =
                    """{"token" : "$token", "commands" : ["lightning-cli listinvoices"] }"""
                try {
                    val obj = JSONObject(json)
                    Log.d(TAG, obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e(TAG, "Could not parse malformed JSON: \"$json\"")
                    liveData.postValue(Resource.error(message = "Error sending request"))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                try {
                    val resp = gson.fromJson(text, InvoicesResponse::class.java)
                    liveData.postValue(Resource.success(data = resp))
                } catch (e: JSONException) {
                    e.printStackTrace()
                    liveData.postValue(Resource.error(message = "Error parsing json"))
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                webSocket.cancel()
                Log.e(TAG, "CLOSE: $code $reason")
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                liveData.postValue(Resource.error(message = t.message.toString()))
            }
        }
        client.newWebSocket(request, listener)
        
        return liveData
    }

    fun refundDecodePay(bolt11: String): MutableLiveData<Resource<String>> {
        val liveData = MutableLiveData<Resource<String>>(Resource.loading())
        val request: Request = Request.Builder().url(gdaxUrl).build()
        val listener: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val token = sharedPreferences.getString("accessToken", "")
                val json =
                    """{"token" : "$token", "commands" : ["lightning-cli decodepay $bolt11"] }"""
                try {
                    val obj = JSONObject(json)
                    Log.d(TAG, obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e(TAG, "Could not parse malformed JSON: \"$json\"")
                    liveData.postValue(Resource.error(message = "Error sending request"))

                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e(TAG, "MESSAGE: $text")
                try {
                    val jsonObject = JSONObject(text)
                    if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                        webSocket.close(1000, null)
                        webSocket.cancel()
                    } else {
                        if (!text.contains("error")) {
                            liveData.postValue(Resource.error(message = "Error occurred"))
                        } else {
                            val jsonObject1 = JSONObject(text)
                            val message = jsonObject1.getString("message")
                            liveData.postValue(Resource.success(data = message))
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    liveData.postValue(Resource.error(message = "Error parsing json"))
                }

            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                webSocket.cancel()
                Log.e(TAG, "CLOSE: $code $reason")
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
            }
        }
        client.newWebSocket(request, listener)
        
        return liveData
    }


    companion object {
        val TAG: String = "LightningService"
    }

}