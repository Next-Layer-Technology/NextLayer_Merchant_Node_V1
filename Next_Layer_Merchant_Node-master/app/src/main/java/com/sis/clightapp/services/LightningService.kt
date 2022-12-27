package com.sis.clightapp.services

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.sis.clightapp.fragments.shared.Auth2FaFragment
import com.sis.clightapp.model.GsonModel.DecodePayBolt11
import com.sis.clightapp.model.GsonModel.Invoice
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeers
import com.sis.clightapp.model.GsonModel.Pay
import com.sis.clightapp.model.GsonModel.Sendreceiveableresponse
import com.sis.clightapp.model.Invoices.InvoicesResponse
import com.sis.clightapp.model.REST.GetRouteResponse
import com.sis.clightapp.model.RefundsData.RefundResponse
import com.sis.clightapp.model.WebsocketResponse.MWSWebSocketResponse
import com.sis.clightapp.util.Resource
import okhttp3.*
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject

class LightningService(val context: Context) {
    private var gdaxUrl: String
    private val gson = Gson()
    private var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var client = OkHttpClient()

    init {
        gdaxUrl = sharedPreferences.getString("mws_command", "") ?: ""
    }

    fun confirmPayment(label: String): MutableLiveData<Resource<Invoice>> {
        val liveData: MutableLiveData<Resource<Invoice>> = MutableLiveData(Resource.loading())
        liveData.postValue(Resource.loading())
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
                    liveData.postValue(Resource.error(message = "Error sending request."))
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                try {
                    val jsonObject = JSONObject(text)
                    val jsonArray = jsonObject.getJSONArray("invoices")
                    if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                        webSocket.close(1000, null)
                        webSocket.cancel()
                        liveData.postValue(
                            Resource.error(
                                message = "2fa"
                            )
                        )
                        return
                    }
                    val invoice = gson.fromJson(
                        jsonArray[0].toString(),
                        Invoice::class.java
                    )
                    liveData.postValue(Resource.success(invoice))
                } catch (e: JSONException) {
                    liveData.postValue(Resource.error(message = "Error parsing json"))
                    e.printStackTrace()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                webSocket.cancel()
                Log.e("TAG", "CLOSE: $code $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                liveData.postValue(Resource.error(message = "Error"))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)

        return liveData
    }

    fun createInvoice(
        rMSatoshi: String?,
        label: String?,
        description: String?,
    ): MutableLiveData<Resource<MWSWebSocketResponse>> {
        val liveData =
            MutableLiveData<Resource<MWSWebSocketResponse>>(Resource.loading())
        liveData.postValue(Resource.loading())
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
                    liveData.postValue(Resource.error(message = "Error sending request"))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG_onMessage", "MESSAGE: $text")
                val jsonObject = JSONObject(text)
                if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                    webSocket.close(1000, null)
                    webSocket.cancel()
                    liveData.postValue(
                        Resource.error(
                            message = "2fa"
                        )
                    )
                    return
                }
                val response = gson.fromJson(
                    text,
                    MWSWebSocketResponse::class.java
                )
                liveData.postValue(Resource.success(response))
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
                liveData.postValue(Resource.error(message = t.message.toString()))
                Log.e("LightningService", t.message.toString())
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)

        return liveData
    }

    fun listPeers(): MutableLiveData<Resource<ListPeers>> {
        val liveData: MutableLiveData<Resource<ListPeers>> =
            MutableLiveData(Resource.loading())

        liveData.postValue(Resource.loading())
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
                    liveData.postValue(Resource.error(message = "Error sending request"))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                val jsonObject = JSONObject(text)
                if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                    webSocket.close(1000, null)
                    webSocket.cancel()
                    liveData.postValue(
                        Resource.error(
                            message = "2fa"
                        )
                    )
                    return
                }
                val jsonArr = jsonObject.getJSONArray("peers")
                val jsonObj: JSONObject = jsonArr.getJSONObject(0)
                val funds = gson.fromJson(jsonObj.toString(), ListPeers::class.java)
                liveData.postValue(Resource.success(funds))
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
                liveData.postValue(Resource.error(message = t.message.toString()))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)

        return liveData
    }

    fun sendReceivables(
        routingNodeId: String,
        msatoshiReceivable: String
    ): MutableLiveData<Resource<Sendreceiveableresponse>> {
        val liveData: MutableLiveData<Resource<Sendreceiveableresponse>> =
            MutableLiveData(Resource.loading())

        liveData.postValue(Resource.loading())
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
                    liveData.postValue(Resource.error(message = "Error sending request."))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e(TAG, text)
                val jsonObject = JSONObject(text)
                if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                    webSocket.close(1000, null)
                    webSocket.cancel()
                    liveData.postValue(
                        Resource.error(
                            message = "2fa"
                        )
                    )
                    return
                }
                val resp = gson.fromJson(text, Sendreceiveableresponse::class.java)
                liveData.postValue(Resource.success(resp))
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
                liveData.postValue(Resource.error(message = t.message.toString()))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)

        return liveData
    }

    fun getRoute(nodeId: String, receivable: String): MutableLiveData<Resource<kotlin.String>> {
        val liveData: MutableLiveData<Resource<String>> =
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
                    liveData.postValue(Resource.error(message = "Error sending request."))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                val jsonObject = JSONObject(text)
                if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                    webSocket.close(1000, null)
                    webSocket.cancel()
                    liveData.postValue(
                        Resource.error(
                            message = "2fa"
                        )
                    )
                    return
                }
                val resp = gson.fromJson(text, GetRouteResponse::class.java)
                val fee =
                    (receivable.toLong() - (resp.routes[0].msatoshi - receivable.toLong())).toString()
                liveData.postValue(Resource.success(fee))
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
        client.newWebSocket(request, webSocketListenerCoinPrice)

        return liveData
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
                        liveData.postValue(
                            Resource.error(
                                message = "2fa"
                            )
                        )
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
                val jsonObject = JSONObject(text)
                if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                    webSocket.close(1000, null)
                    webSocket.cancel()
                    liveData.postValue(
                        Resource.error(
                            message = "2fa"
                        )
                    )
                    return
                }
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

    fun decodeInvoice(bolt11: String): MutableLiveData<Resource<DecodePayBolt11>> {
        val liveData = MutableLiveData<Resource<DecodePayBolt11>>(Resource.loading())
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
                        liveData.postValue(
                            Resource.error(
                                message = "2fa"
                            )
                        )
                    } else {
                        if (text.contains("error")) {
                            val jsonObject1 = JSONObject(text)
                            val message = jsonObject1.getString("message")
                            liveData.postValue(Resource.error(message = message))
                        } else {
                            liveData.postValue(
                                Resource.success(
                                    data = gson.fromJson(
                                        text,
                                        DecodePayBolt11::class.java
                                    )
                                )
                            )
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

    fun payRequestToOther(
        bolt11: String,
        msatoshi: String?,
        label: String
    ): MutableLiveData<Resource<Pay>> {
        val liveData = MutableLiveData<Resource<Pay>>(Resource.loading())
        val request: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val token = sharedPreferences.getString("accessToken", "")
                val json =
                    """{"token" : "$token", "commands" : ["lightning-cli pay $bolt11 null $label"] }"""
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                    liveData.postValue(Resource.error(message = "Error sending request"))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                try {
                    val jsonObject = JSONObject(text)
                    if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                        webSocket.close(1000, null)
                        webSocket.cancel()
                        liveData.postValue(
                            Resource.error(
                                message = "2fa"
                            )
                        )
                    } else {
                            if (text.contains("error")) {
                            liveData.postValue(
                                Resource.error(
                                    message = "Error paying invoice."
                                )
                            )
                        } else {
                            liveData.postValue(
                                Resource.success(
                                    data = gson.fromJson(
                                        text,
                                        Pay::class.java
                                    )
                                )
                            )
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
                Log.e("TAG", "CLOSE: $code $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                liveData.postValue(Resource.error(message = "Error occurred"))
            }
        }
        client.newWebSocket(request, webSocketListenerCoinPrice)
        return liveData
    }

    companion object {
        val TAG: String = "LightningService"
    }

}