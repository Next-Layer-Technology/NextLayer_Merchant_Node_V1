package com.sis.clightapp.fragments.checkout

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.KeyguardManager
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.zxing.integration.android.IntentIntegrator
import com.sis.clightapp.Interface.*
import com.sis.clightapp.Network.CheckNetwork
import com.sis.clightapp.R
import com.sis.clightapp.activity.CheckOutMainActivity
import com.sis.clightapp.adapter.CheckOutMainListAdapter
import com.sis.clightapp.model.Channel_BTCResponseData
import com.sis.clightapp.model.GsonModel.Items
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemsDataMerchant
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeers
import com.sis.clightapp.model.GsonModel.Merchant.MerchantLoginResp
import com.sis.clightapp.model.GsonModel.Sendreceiveableresponse
import com.sis.clightapp.model.ImageRelocation.GetItemImageReloc
import com.sis.clightapp.model.REST.FundingNodeListResp
import com.sis.clightapp.model.Tax
import com.sis.clightapp.services.BTCService
import com.sis.clightapp.services.SessionService
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

class CheckOutFragment1 : CheckOutBaseFragment() {
    private val apiClient: ApiPaths2 by inject()
    private val webservice: Webservice by inject()
    private val btcService: BTCService by inject()
    private val sessionService: SessionService by inject()
    var setwidht = 0
    private var setheight = 0
    private lateinit var checkOutbtn: Button
    private lateinit var scanUPCbtn: Button
    private lateinit var cbList: CheckBox
    private lateinit var cbScan: CheckBox
    var checkOutListView: ListView? = null
    var checkOutMainListAdapter: CheckOutMainListAdapter? = null
    var btcRate: TextView? = null
    private var gdaxUrl = "ws://73.36.65.41:8095/SendCommands"
    var sharedPreferences: CustomSharedPreferences? = null
    var mScanedDataSourceItemList: ArrayList<Items>? = null
    var confirmingProgressDialog: ProgressDialog? = null
    lateinit var setTextWithSpan: TextView
    private var mSatoshiReceivable = 0.0
    private var btcReceivable = 0.0
    private var usdReceivable = 0.0
    private var mSatoshiCapacity = 0.0
    private var btcCapacity = 0.0
    private var usdCapacity = 0.0
    private var usdRemainingCapacity = 0.0
    private var btcRemainingCapacity = 0.0

    private var INTENT_AUTHENTICATE = 1234
    var isFundingInfoGetSuccefully = false
    lateinit var clearOutDialog: Dialog
    var TAG = "CheckOutFragment1"
    var isListMode = true
    var isScanMode = false

    @SuppressLint("SetTextI18n")
    private fun setcurrentrate(x: String) {
        btcRate!!.text = "$" + x + "BTC/USD"
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().stopService(Intent(context, MyLogOutService::class.java))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_check_out1, container, false)
        setTextWithSpan = view.findViewById(R.id.footervtv)
        isFundingInfoGetSuccefully = false
        val boldStyle = StyleSpan(Typeface.BOLD)
        setTextWithSpan(
            setTextWithSpan,
            getString(R.string.welcome_text),
            getString(R.string.welcome_text_bold),
            boldStyle
        )
        checkOutbtn = view.findViewById(R.id.checkoutbtn)
        cbList = view.findViewById(R.id.cbList)
        cbScan = view.findViewById(R.id.cbScan)
        cbList.setOnClickListener {
            isListMode = true
            isScanMode = false
            cbScan.isChecked = false
            cbList.isChecked = true
            checkOutMainListAdapter = CheckOutMainListAdapter(requireContext(), GlobalState.getInstance().itemsList)
            if (checkOutListView != null) checkOutListView!!.adapter = checkOutMainListAdapter
        }
        cbScan.setOnClickListener {
            isScanMode = true
            isListMode = false
            cbList.isChecked = false
            cbScan.isChecked = true
            checkOutMainListAdapter =
                CheckOutMainListAdapter(
                    requireContext(),
                    GlobalState.getInstance().selectedItems.toList()
                )
            checkOutListView!!.adapter = checkOutMainListAdapter
        }
        scanUPCbtn = view.findViewById(R.id.scanUPC)
        checkOutListView = view.findViewById(R.id.checkoutitemlist)
        confirmingProgressDialog = ProgressDialog(context)
        confirmingProgressDialog?.setCancelable(false)
        confirmingProgressDialog?.setMessage("Loading ...")
        mScanedDataSourceItemList = ArrayList()
        //create scan object
        val qrScan = IntentIntegrator(activity)
        qrScan.setOrientationLocked(false)
        val prompt = resources.getString(R.string.enter_upc_code_via_scanner)
        qrScan.setPrompt(prompt)
        addItemprogressDialog = ProgressDialog(context)
        addItemprogressDialog.setMessage("Adding Item")
        exitFromServerProgressDialog = ProgressDialog(context)
        exitFromServerProgressDialog?.setMessage("Exiting")
        getItemListprogressDialog = ProgressDialog(context)
        getItemListprogressDialog?.setMessage("Loading...")
        btcRate = view.findViewById(R.id.btcRateTextview)
        gdaxUrl = CustomSharedPreferences().getvalueofMWSCommand("mws_command", context)
        findMerchant(
            CustomSharedPreferences().getvalueofMerchantname("merchant_name", context),
            CustomSharedPreferences().getvalueofMerchantpassword("merchant_pass", context)
        )
        fContext = context
        sharedPreferences = CustomSharedPreferences()
        btcService.currentBtc.observe(viewLifecycleOwner) {
            it.data?.let {
                setcurrentrate(it.price.toString())
            }
        }
        if (CheckNetwork.isInternetAvailable(fContext)) {
            fundingNodeInfo
        } else {
            setReceivableAndCapacity("0", "0", true)
            setcurrentrate("Not Found")
        }
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        setwidht = width * 45
        setwidht /= 100
        setheight = height / 2
        scanUPCbtn.setOnClickListener {
            IntentIntegrator.forSupportFragment(
                this@CheckOutFragment1
            ).initiateScan()
        }
        checkOutbtn.setOnClickListener {
            (requireActivity() as CheckOutMainActivity).swipeToCheckOutFragment3(
                2
            )
        }
        view.findViewById<View>(R.id.clearout).setOnClickListener {
            val km = requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardSecure) {
                val authIntent = km.createConfirmDeviceCredentialIntent("Authorize Payment", "")
                startActivityForResult(authIntent, INTENT_AUTHENTICATE)
            } else {
                listPeers
            }
        }
        allItems
        return view
    }

    //Getting the scan results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1234 -> {
                // HANDLE LockIng
                super.onActivityResult(requestCode, resultCode, data)
                if (resultCode == Activity.RESULT_OK) {
                    listPeers
                }
            }

            49374 -> {
                // HANDLE QRSCAN
                val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                if (result != null) {
                    //if qrcode has nothing in it
                    if (result.contents == null) {
                        Toast.makeText(context, "Result Not Found", Toast.LENGTH_LONG).show()
                    } else {
                        addItemprogressDialog.show()
                        addItemprogressDialog.setCancelable(false)
                        addItemprogressDialog.setCanceledOnTouchOutside(false)
                        val getUpc = result.contents
                        showToast(getUpc)
                        if (GlobalState.getInstance().itemsList.size > 0) {
                            var itr = 0
                            while (itr < GlobalState.getInstance().itemsList.size) {
                                if (GlobalState.getInstance().itemsList[itr].upc == getUpc) {
                                    if (GlobalState.getInstance().selectedItems.contains(GlobalState.getInstance().itemsList[itr])) {
                                        android.app.AlertDialog.Builder(context)
                                            .setMessage("Item Already Add")
                                            .setPositiveButton("OK", null)
                                            .show()
                                        showToast("Item Already Add")
                                    } else {
                                        GlobalState.getInstance().itemsList[itr].selectQuatity = 1
                                        GlobalState.getInstance().selectedItems.add(GlobalState.getInstance().itemsList[itr])
                                        Log.d(TAG, "onActivityResult: 372")
                                        setAdapter()
                                        break
                                    }
                                }
                                itr++
                            }
                            addItemprogressDialog.dismiss()
                        } else {
                            showToast("No Item In Inventory")
                            addItemprogressDialog.dismiss()
                        }
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    //Reloaded ALl Adapter
    fun setAdapter() {
        var countItem = 0
        for (items in GlobalState.getInstance().selectedItems) {
            countItem += items.selectQuatity
        }
        (requireActivity() as CheckOutMainActivity).updateCartIcon(countItem)
        val dataSource = if (isListMode) {
            GlobalState.getInstance().itemsList
        } else {
            GlobalState.getInstance().selectedItems.toList()
        }
        Log.d(
            TAG,
            "setAdapter: dataSource != null: " + Arrays.toString(
                Arrays.stream<Any>(dataSource.toTypedArray()).toArray()
            )
        )
        if (dataSource.isNotEmpty()) {
            checkOutMainListAdapter = CheckOutMainListAdapter(requireContext(), dataSource)
            checkOutListView!!.adapter = checkOutMainListAdapter
            checkOutListView!!.onItemLongClickListener =
                OnItemLongClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long ->
                    val builder = android.app.AlertDialog.Builder(
                        context
                    )
                    builder.setTitle(getString(R.string.delete_title))
                    builder.setMessage(getString(R.string.delete_subtitle))
                    builder.setCancelable(true)
                    // Action if user selects 'yes'
                    builder.setPositiveButton("Yes") { _: DialogInterface?, _: Int -> checkOutMainListAdapter!!.notifyDataSetChanged() }
                    // Create the alert dialog using alert dialog builder
                    val dialog = builder.create()
                    dialog.setCanceledOnTouchOutside(false)
                    // Finally, display the dialog when user press back button
                    dialog.show()
                    true
                }
            GlobalState.getInstance().isCheckoutBtnPress = false
        } else {
            checkOutMainListAdapter = CheckOutMainListAdapter(requireContext(), dataSource)
            checkOutListView!!.adapter = checkOutMainListAdapter
            (requireActivity() as CheckOutMainActivity).updateCartIcon(0)
        }
    }

    private fun parseJSON() {
        val itemImageRelocArrayList = GlobalState.getInstance().currentItemImageRelocArrayList
        GlobalState.getInstance().itemsList.clear()
        for (j in itemImageRelocArrayList.indices) {
            val items = Items()
            items.upc = itemImageRelocArrayList[j].upc_number
            items.imageUrl = itemImageRelocArrayList[j].image
            items.name = itemImageRelocArrayList[j].name
            if (itemImageRelocArrayList[j].quantity != null) {
                items.quantity = itemImageRelocArrayList[j].quantity
            } else {
                items.quantity = "1"
            }
            items.price = itemImageRelocArrayList[j].price
            items.totalPrice = itemImageRelocArrayList[j].total_price
            items.imageInHex = itemImageRelocArrayList[j].image_in_hex
            items.additionalInfo = itemImageRelocArrayList[j].additional_info
            GlobalState.getInstance().itemsList.add(j, items)
        }
        for (items in GlobalState.getInstance().itemsList) {
            Log.e(
                "ItemsDetails",
                "Name:" + items.name + "-" + "Quantity:" + items.quantity + "-" + "Price:" + items.price + "-" + "UPC:" + items.upc + "-" + "ImageURl:" + items.imageUrl
            )
        }
        setAdapter()
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }
    private val allItems: Unit
        get() {
            val refToken = CustomSharedPreferences().getvalueofRefresh("refreshToken", context)
            val token = "Bearer $refToken"
            val jsonObject1 = JsonObject()
            jsonObject1.addProperty("refresh", refToken)
            val call = apiClient.getInventoryItems(token)
            call.enqueue(object : Callback<ItemsDataMerchant?> {
                override fun onResponse(
                    call: Call<ItemsDataMerchant?>,
                    response: Response<ItemsDataMerchant?>
                ) {
                    if (response.body() != null) {
                        val itemsDataMerchant = response.body()
                        val itemImageRelocArrayList = ArrayList<GetItemImageReloc>()
                        if (itemsDataMerchant!!.success) {
                            val lIstModelList = itemsDataMerchant.list
                            for (i in lIstModelList.indices) {
                                val itemLIstModel = lIstModelList[i]
                                val getItemImageReloc = GetItemImageReloc(
                                    itemLIstModel.id.toInt(),
                                    1,
                                    itemLIstModel.upc_code,
                                    itemLIstModel.image_path,
                                    itemLIstModel.name,
                                    itemLIstModel.quantity_left,
                                    itemLIstModel.unit_price,
                                    "i",
                                    "1",
                                    0.0,
                                    "i",
                                    "i",
                                    "i"
                                )
                                itemImageRelocArrayList.add(getItemImageReloc)
                            }
                            if (itemImageRelocArrayList.size > 0) {
                                GlobalState.getInstance().currentItemImageRelocArrayList =
                                    itemImageRelocArrayList
                                parseJSON()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ItemsDataMerchant?>, t: Throwable) {
                    Log.e("get-funding-nodes:", t.message ?: "")
                }
            })
        }

    //Get Funding Node Info
    private val fundingNodeInfo: Unit
        get() {
            val call = webservice.fundingNodeList()
            call.enqueue(object : Callback<FundingNodeListResp?> {
                override fun onResponse(
                    call: Call<FundingNodeListResp?>,
                    response: Response<FundingNodeListResp?>
                ) {
                    if (response.body() != null) {
                        if (response.body()!!.fundingNodesList != null) {
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

    private fun parseJSONForListPeers(jsonresponse: String) {
        Log.d("ListPeersParsingResponse", jsonresponse)
        val listFunds: ListPeers
        var sta = false
        var jsonArr: JSONArray? = null
        val jsonObject: JSONObject
        try {
            jsonObject = JSONObject(jsonresponse)
            jsonArr = jsonObject.getJSONArray("peers")
        } catch (e: Exception) {
            Log.e("ListFundParsing1", e.message.toString())
        }
        var jsonObj: JSONObject? = null
        try {
            assert(jsonArr != null)
            jsonObj = jsonArr!!.getJSONObject(0)
            sta = true
        } catch (e: Exception) {
            //e.printStackTrace();
            Log.e("ListFundParsing2", e.message.toString())
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
                                        Log.e("StringToDouble:", e.message.toString())
                                    }
                                    msat += tmsat
                                    mcap += tmcap
                                }
                            }
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
                Log.e("ListFundParsing3", exception.message.toString())
            } catch (exception: JsonSyntaxException) {
                Log.e("ListFundParsing3", exception.message.toString())
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

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun goToClearOutDialog(isFetchData: Boolean) {
        clearOutDialog = Dialog(requireContext())
        clearOutDialog.setContentView(R.layout.clearout_dialog_layout)
        clearOutDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //dialog.getWindow().setLayout(500, 500);
        clearOutDialog.setCancelable(false)
        val receivedVal = clearOutDialog.findViewById<TextView>(R.id.receivedVal)
        val capicityVal = clearOutDialog.findViewById<TextView>(R.id.capicityVal)
        val clearoutVal = clearOutDialog.findViewById<TextView>(R.id.clearoutVal)
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
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("Please Try Again!!")
                    .setCancelable(false)
                    .setPositiveButton("Retry!") { dialog: DialogInterface, _: Int ->
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
                val mSatoshiSpendableTotal = (mSatoshiCapacity - mSatoshiReceivable).toLong()
                sendReceiveables(routingNodeId, mSatoshiSpendableTotal.toString() + "")
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("Funding Node Id is Missing")
                    .setCancelable(false)
                    .setPositiveButton("Retry!") { dialog: DialogInterface, _: Int ->
                        dialog.cancel()
                        fundingNodeInfo
                    }.show()
            }
        } else {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Funding Node Id is Missing")
                .setCancelable(false)
                .setPositiveButton("Retry!") { dialog: DialogInterface, _: Int ->
                    dialog.cancel()
                    fundingNodeInfo
                }.show()
        }
    }

    private fun findMerchant(id: String, pass: String) {
        confirmingProgressDialog?.show()
        val paramObject = JsonObject()
        paramObject.addProperty("user_id", id)
        paramObject.addProperty("password", pass)
        val call = webservice.merchant_Loging(paramObject)
        call.enqueue(object : Callback<MerchantLoginResp?> {
            override fun onResponse(
                call: Call<MerchantLoginResp?>,
                response: Response<MerchantLoginResp?>
            ) {
                confirmingProgressDialog?.dismiss()
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.message == "successfully login") {
                            val merchantData = response.body()!!.merchantData
                            sessionService.setMerchantData(merchantData)
                            GlobalState.getInstance().merchant_id = id
                            val tax = Tax()
                            tax.taxInUSD = 1.0
                            tax.taxInBTC = 0.00001
                            tax.taxpercent = merchantData.tax_rate.toDouble()
                            GlobalState.getInstance().tax = tax
                            sharedPreferences?.setString(
                                merchantData.ssh_password,
                                "sshkeypass",
                                context
                            )
                            CustomSharedPreferences().setvalueofMerchantname(
                                id,
                                "merchant_name",
                                context
                            )
                            CustomSharedPreferences().setvalueofMerchantpassword(
                                pass,
                                "merchant_pass",
                                context
                            )
                            CustomSharedPreferences().setvalueofMerchantId(
                                merchantData.id,
                                "merchant_id",
                                context
                            )
                        }
                    }
                }
            }

            override fun onFailure(call: Call<MerchantLoginResp?>, t: Throwable) {
                confirmingProgressDialog?.dismiss()
            }
        })
    }

    private val listPeers: Unit
        get() {
            val clientCoinPrice = OkHttpClient()
            val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
            val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                    val token = sharedPreferences?.getvalueofaccestoken("accessToken", context)
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
                                webSocket.close(1000, null)
                                webSocket.cancel()
                                goTo2FaPasswordDialog()
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

    private fun sendReceiveables(routingNodeId: String, mstoshiReceivable: String) {
        val clientCoinPrice = OkHttpClient()
        val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val token = sharedPreferences?.getvalueofaccestoken("accessToken", context)
                val json =
                    "{\"token\" : \"$token\", \"commands\" : [\"lightning-cli keysend $routingNodeId $mstoshiReceivable\"] }"
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