package com.sis.clightapp.fragments.checkout

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.Network.CheckNetwork
import com.sis.clightapp.R
import com.sis.clightapp.activity.CheckOutMainActivity
import com.sis.clightapp.model.Channel_BTCResponseData
import com.sis.clightapp.model.GsonModel.Items
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeers
import com.sis.clightapp.model.GsonModel.Sendreceiveableresponse
import com.sis.clightapp.model.REST.FundingNodeListResp
import com.sis.clightapp.services.BTCService
import com.sis.clightapp.session.MyLogOutService
import com.sis.clightapp.util.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tech.gusavila92.websocketclient.WebSocketClient
import java.math.BigDecimal
import java.net.URI
import java.net.URISyntaxException
import java.util.*

class CheckOutsFragment2 : CheckOutBaseFragment(), View.OnClickListener {
    private val webservice: Webservice by inject()
    private val btcService: BTCService by inject()

    lateinit var amount: EditText
    lateinit var rcieptnum: EditText
    var btn = arrayOfNulls<TextView>(12)
    lateinit var btnAddItem: TextView
    lateinit var btnCheckOut: TextView
    var sharedPreferences: CustomSharedPreferences? = null
    lateinit var btcRate: TextView
    var newManualItem: Items? = null
    lateinit var setTextWithSpan: TextView
    private var gdaxUrl = "wss://73.36.65.41:8095/SendCommands"
    lateinit var clearout: TextView
    var mSatoshiReceivable = 0.0
    var btcReceivable = 0.0
    var usdReceivable = 0.0
    var mSatoshiCapacity = 0.0
    var btcCapacity = 0.0
    var usdCapacity = 0.0
    var usdRemainingCapacity = 0.0
    var btcRemainingCapacity = 0.0
    var INTENT_AUTHENTICATE = 1234
    var isFundingInfoGetSuccefully = false
    lateinit var clearOutDialog: Dialog
    override fun onDestroy() {
        super.onDestroy()
        requireContext().stopService(Intent(context, MyLogOutService::class.java))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_check_outs2, container, false)
        isFundingInfoGetSuccefully = false
        setTextWithSpan = view.findViewById(R.id.footer)
        val boldStyle = StyleSpan(Typeface.BOLD)
        setTextWithSpan(
            setTextWithSpan,
            getString(R.string.welcome_text),
            getString(R.string.welcome_text_bold),
            boldStyle
        )
        fContext = context
        clearout = view.findViewById(R.id.clearout)
        exitFromServerProgressDialog = ProgressDialog(context)
        exitFromServerProgressDialog!!.setMessage("Exiting")
        btcRate = view.findViewById(R.id.btcRateTextview)
        btnAddItem = view.findViewById(R.id.button)
        btnCheckOut = view.findViewById(R.id.imageView5)
        amount = view.findViewById(R.id.et_amount)
        rcieptnum = view.findViewById(R.id.recieptno)
        amount.showSoftInputOnFocus = false
        gdaxUrl = CustomSharedPreferences().getvalueofMWSCommand("mws_command", context)
        sharedPreferences = CustomSharedPreferences()
        btnAddItem.setOnClickListener {
            val amountValue = amount.text.toString()
            val recieptNumValue = rcieptnum.text.toString()
            if (amountValue.isEmpty() || recieptNumValue.isEmpty()) {
                showToast("Enter the Amount and Receipt Number")
            } else {
                val newItem = Items()
                newItem.name = recieptNumValue
                newItem.price = amountValue
                newItem.upc = recieptNumValue
                newItem.quantity = "100000000"
                newItem.selectQuatity = 1
                newItem.isManual = "Yes"
                addItemToCartDialog(newItem)
            }
        }
        btnCheckOut.setOnClickListener {
            (requireActivity() as CheckOutMainActivity).swipeToCheckOutFragment3(
                2
            )
        }
        btcService.currentBtc.observe(viewLifecycleOwner) {
            it.data?.let {
                setcurrentrate(it.price.toString())
            }
        }
        if (CheckNetwork.isInternetAvailable(fContext)) {
            fundingNodeInfo
        } else {
            setcurrentrate("Not Found")
            setReceivableAndCapacity("0", "0", false)
        }
        btn[0] = view.findViewById<View>(R.id.num1) as TextView
        btn[1] = view.findViewById<View>(R.id.num2) as TextView
        btn[2] = view.findViewById<View>(R.id.num3) as TextView
        btn[3] = view.findViewById<View>(R.id.num4) as TextView
        btn[4] = view.findViewById<View>(R.id.num5) as TextView
        btn[5] = view.findViewById<View>(R.id.num6) as TextView
        btn[6] = view.findViewById<View>(R.id.num7) as TextView
        btn[7] = view.findViewById<View>(R.id.num8) as TextView
        btn[8] = view.findViewById<View>(R.id.num9) as TextView
        btn[9] = view.findViewById<View>(R.id.num0) as TextView
        btn[10] = view.findViewById<View>(R.id.numdot) as TextView
        btn[11] = view.findViewById<View>(R.id.numC) as TextView
        for (i in 0..11) {
            btn[i]!!.setOnClickListener(this)
        }
        clearout.setOnClickListener {
            val km = requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardSecure) {
                val authIntent = km.createConfirmDeviceCredentialIntent("Authorize Payment", "")
                startActivityForResult(authIntent, INTENT_AUTHENTICATE)
            } else {
                listPeers
            }
        }
        return view
    }

    private fun addItemToCartDialog(newItem: Items) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.additem_title))
        builder.setMessage(getString(R.string.additem_subtitle))
        builder.setCancelable(true)
        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
            newManualItem = newItem
            amount.text.clear()
            rcieptnum.text.clear()
            if (newManualItem != null) {
                GlobalState.getInstance().selectedItems.add(newManualItem)
                var count = 0
                for (items in GlobalState.getInstance().selectedItems) {
                    count += items.selectQuatity
                }
                (requireActivity() as CheckOutMainActivity).updateCartIcon(count)
            }
            newManualItem = null
            dialogInterface.dismiss()
        }

        // Actions if user selects 'no'
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->
            amount.text.clear()
            rcieptnum.text.clear()
            newManualItem = null
            dialogInterface.dismiss()
        }

        // Create the alert dialog using alert dialog builder
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    @SuppressLint("NonConstantResourceId")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.num1 -> amount.append("1")
            R.id.num2 -> amount.append("2")
            R.id.num3 -> amount.append("3")
            R.id.num4 -> amount.append("4")
            R.id.num5 -> amount.append("5")
            R.id.num6 -> amount.append("6")
            R.id.num7 -> amount.append("7")
            R.id.num8 -> amount.append("8")
            R.id.num9 -> amount.append("9")
            R.id.num0 -> amount.append("0")
            R.id.numdot -> amount.append(".")
            R.id.numC -> amount.setText("")
            else -> {}
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setcurrentrate(x: String) {
        btcRate.text = "$$x"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1234) { // HANDLE LockIng
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                listPeers
            }
        }
    }

    private val fundingNodeInfo: Unit
        //Get Funding Node Info
        private get() {
            val call: Call<FundingNodeListResp> = webservice.fundingNodeList()
            call.enqueue(object : Callback<FundingNodeListResp?> {
                override fun onResponse(
                    call: Call<FundingNodeListResp?>,
                    response: Response<FundingNodeListResp?>
                ) {
                    if (response.body() != null) {
                        if (response.body()?.fundingNodesList != null) {
                            if (response.body()!!.fundingNodesList.size > 0) {
                                isFundingInfoGetSuccefully = true
                                val fundingNode = response.body()!!.fundingNodesList[0]
                                GlobalState.getInstance().fundingNode = fundingNode
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<FundingNodeListResp?>, t: Throwable) {
                    Log.e("get-funding-nodes:", t.message.toString())
                }
            })
        }
    private val listPeers: Unit
        get() {
            val clientCoinPrice = OkHttpClient()
            val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
            val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                    val token = sharedPreferences!!.getvalueofaccestoken("accessToken", context)
                    val json =
                        "{\"token\" : \"$token\", \"commands\" : [\"lightning-cli listpeers\"] }"
                    try {
                        val obj = JSONObject(json)
                        Log.d("My App", obj.toString())
                        webSocket.send(obj.toString())
                    } catch (t: Throwable) {
                        Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.e("TAG", "MESSAGE: $text")
                    requireActivity().runOnUiThread {
                        try {
                            val jsonObject = JSONObject(text)
                            if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                                requireActivity().runOnUiThread {
                                    webSocket.close(1000, null)
                                    webSocket.cancel()
                                    goTo2FaPasswordDialog()
                                }
                            } else {
                                parseJSONForListPeers(text)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    Log.e("TAG", "MESSAGE: " + bytes.hex())
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(1000, null)
                    webSocket.cancel()
                    Log.e("TAG", "CLOSE: $code $reason")
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    //TODO: stuff
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: okhttp3.Response?
                ) {
                    //TODO: stuff
                    requireActivity().runOnUiThread { showToast(response.toString()) }
                }
            }
            clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice)
            clientCoinPrice.dispatcher.executorService.shutdown()
        }

    private fun parseJSONForListPeers(jsonresponse: String) {
        var listFunds: ListPeers?
        var sta: Boolean
        var jsonArr: JSONArray? = null
        var jsonObject: JSONObject?
        try {
            jsonObject = JSONObject(jsonresponse)
            val ja_data: JSONArray? = null
            jsonArr = jsonObject.getJSONArray("peers")


        } catch (e: Exception) {
            Log.e("ListFundParsing1", e.message!!)
        }
        var jsonObj: JSONObject? = null
        try {
            jsonObj = jsonArr!!.getJSONObject(0)
            sta = true
        } catch (e: Exception) {
            //e.printStackTrace();
            sta = false
            Log.e("ListFundParsing2", e.message!!)
        }
        if (sta) {
            val gson = Gson()
            try {
                listFunds = gson.fromJson(jsonObj.toString(), ListPeers::class.java)
                if (listFunds != null) {
                    if (listFunds.getChannels() != null) {
                        if (listFunds.getChannels().size > 0) {
                            isReceivableGet = true
                            var msat = 0.0
                            var mcap = 0.0
                            for (tempListFundChanel in listFunds.getChannels()) {
                                if (listFunds.isConnected && tempListFundChanel.state.equals(
                                        "CHANNELD_NORMAL",
                                        ignoreCase = true
                                    )
                                ) {
                                    val tempmsat =
                                        tempListFundChanel.getReceivable_msatoshi().toString() + ""
                                    val tempmCap =
                                        tempListFundChanel.getSpendable_msatoshi().toString() + ""
                                    var tmsat = 0.0
                                    var tmcap = 0.0
                                    try {
                                        tmsat = tempmsat.toDouble()
                                        tmcap = tempmCap.toDouble()
                                        val value = BigDecimal(tempmCap)
                                        val doubleValue = value.toDouble()
                                        Log.e("StringToDouble:", doubleValue.toString())
                                    } catch (e: Exception) {
                                        Log.e("StringToDouble:", e.message!!)
                                    }
                                    msat = msat + tmsat
                                    mcap = mcap + tmcap
                                }
                            }
                            Log.e("Receivable", excatFigure2(msat))
                            Log.e("Capcaity", excatFigure2(mcap))
                            setReceivableAndCapacity(
                                msat.toString(),
                                (mcap + msat).toString(),
                                true
                            )
                        }
                    }
                } else {
                    setReceivableAndCapacity("0", "0", false)
                }
            } catch (exception: IllegalStateException) {
                Log.e("ListFundParsing3", exception.message!!)
            } catch (exception: JsonSyntaxException) {
                Log.e("ListFundParsing3", exception.message!!)
            }
        } else {
            Log.e("Error", "Error")
            showToast("Wrong Response!!!")
        }
    }

    //Manipulate Receivable Amount
    private fun setReceivableAndCapacity(
        receivableMSat: String,
        capcaityMSat: String,
        sta: Boolean
    ) {
        mSatoshiReceivable = receivableMSat.toDouble()
        btcReceivable = mSatoshiReceivable / AppConstants.satoshiToMSathosi
        btcReceivable /= AppConstants.btcToSathosi
        usdReceivable = btcService.btcToUsd(btcReceivable)
        mSatoshiCapacity = capcaityMSat.toDouble()
        btcCapacity = mSatoshiCapacity / AppConstants.satoshiToMSathosi
        btcCapacity /= AppConstants.btcToSathosi
        usdCapacity = btcService.btcToUsd(btcCapacity)
        btcRemainingCapacity = btcCapacity /*- btcReceivable*/
        usdRemainingCapacity = usdCapacity /*- usdReceivable*/
        goToClearOutDialog(sta)
    }

    private fun goToClearOutDialog(isFetchData: Boolean) {
        clearOutDialog = Dialog(requireContext())
        clearOutDialog.setContentView(R.layout.clearout_dialog_layout)
        clearOutDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        clearOutDialog.setCancelable(false)
        val receivedVal = clearOutDialog.findViewById<TextView>(R.id.receivedVal)
        val capicityVal = clearOutDialog.findViewById<TextView>(R.id.capicityVal)
        val clearoutVal = clearOutDialog.findViewById<TextView>(R.id.clearoutVal)
        Log.e("BeforeDialogCap", usdRemainingCapacity.toString())
        Log.e("BeforeDialogRecv", usdReceivable.toString())
        if (isFetchData) {
            if (isReceivableGet) {
                capicityVal.text = ":$" + String.format("%.2f", round(usdRemainingCapacity, 2))
                receivedVal.text = ":$" + String.format("%.2f", round(usdReceivable, 2))
                clearoutVal.text =
                    ":$" + String.format("%.2f", round(usdRemainingCapacity - usdReceivable, 2))
            } else {
                capicityVal.text = "N/A"
                receivedVal.text = "N/A"
                clearoutVal.text = "N/A"
            }
        } else {
            capicityVal.text = "N/A"
            receivedVal.text = "N/A"
            clearoutVal.text = "N/A"
        }
        val ivBack = clearOutDialog.findViewById<ImageView>(R.id.iv_back_invoice)
        val noBtn = clearOutDialog.findViewById<Button>(R.id.noBtn)
        val yesBtn = clearOutDialog.findViewById<Button>(R.id.yesBtn)
        ivBack.setOnClickListener { clearOutDialog.dismiss() }
        yesBtn.setOnClickListener {
            if (isFetchData) {
                sendReceivable()
            } else {
                val builder = AlertDialog.Builder(fContext!!)
                builder.setMessage("Please Try Again!!")
                    .setCancelable(false)
                    .setPositiveButton("Retry!") { dialog: DialogInterface, id: Int ->
                        dialog.cancel()
                        clearOutDialog.dismiss()
                    }.show()
            }
        }
        noBtn.setOnClickListener { clearOutDialog.dismiss() }
        clearOutDialog.show()
    }

    //Clear Out All Receivable Amount to Destination
    private fun sendReceivable() {
        val routingNodeId: String
        val fundingNode = GlobalState.getInstance().fundingNode
        if (fundingNode != null) {
            if (fundingNode.node_id != null) {
                routingNodeId = fundingNode.node_id
                val label = "clearout$unixTimeStamp"
                val mSatoshiSpendableTotal = (mSatoshiCapacity - mSatoshiReceivable).toLong()
                sendreceiveables(routingNodeId, mSatoshiSpendableTotal.toString() + "")
            } else {
                val builder = AlertDialog.Builder(fContext!!)
                builder.setMessage("Funding Node Id is Missing")
                    .setCancelable(false)
                    .setPositiveButton("Retry!") { dialog: DialogInterface, id: Int ->
                        dialog.cancel()
                        fundingNodeInfo
                    }.show()
            }
        } else {
            val builder = AlertDialog.Builder(fContext!!)
            builder.setMessage("Funding Node Id is Missing")
                .setCancelable(false)
                .setPositiveButton("Retry!") { dialog: DialogInterface, id: Int ->
                    dialog.cancel()
                    fundingNodeInfo
                }.show()
        }
    }

    fun sendreceiveables(routingnodeId: String, mstoshiReceivable: String) {
        val clientCoinPrice = OkHttpClient()
        val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val token = sharedPreferences!!.getvalueofaccestoken("accessToken", context)
                val json =
                    "{\"token\" : \"$token\", \"commands\" : [\"lightning-cli keysend $routingnodeId $mstoshiReceivable\"] }"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                requireActivity().runOnUiThread {
                    try {
                        val jsonObject = JSONObject(text)
                        if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                            webSocket.close(1000, null)
                            webSocket.cancel()
                            goTo2FaPasswordDialog()
                        } else {
                            val gson = Gson()
                            val sendreceiveableresponse =
                                gson.fromJson(text, Sendreceiveableresponse::class.java)
                            showToast(sendreceiveableresponse.msatoshi.toString())
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.e("TAG", "MESSAGE: " + bytes.hex())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                webSocket.cancel()
                Log.e("TAG", "CLOSE: $code $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {}
            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: okhttp3.Response?
            ) {
                requireActivity().runOnUiThread { showToast(response.toString()) }
            }
        }
        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice)
        clientCoinPrice.dispatcher.executorService.shutdown()
    }

    companion object {
        var isReceivableGet = false
    }
}