package com.sis.clightapp.fragments.checkout

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.KeyguardManager
import android.app.ProgressDialog
import android.content.*
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.Network.CheckNetwork
import com.sis.clightapp.R
import com.sis.clightapp.activity.CheckOutMainActivity
import com.sis.clightapp.adapter.CheckOutMainListAdapter
import com.sis.clightapp.adapter.MerchantNodeAdapter
import com.sis.clightapp.fragments.printing.PrintDialogFragment
import com.sis.clightapp.model.GsonModel.*
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData
import com.sis.clightapp.model.REST.FundingNodeListResp
import com.sis.clightapp.model.REST.nearby_clients.NearbyClientResponse
import com.sis.clightapp.model.REST.nearby_clients.NearbyClients
import com.sis.clightapp.model.WebsocketResponse.MWSWebSocketResponse
import com.sis.clightapp.services.BTCService
import com.sis.clightapp.services.LightningService
import com.sis.clightapp.services.SessionService
import com.sis.clightapp.session.MyLogOutService
import com.sis.clightapp.util.*
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal
import java.net.URISyntaxException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class CheckOutsFragment3 : CheckOutBaseFragment() {
    val TAG = "CheckOutsFragment3"
    private val sessionService: SessionService by inject()
    private val webservice: Webservice by inject()
    private val lightningService: LightningService by inject()
    private val btcService = BTCService()
    lateinit var paywithclightbtn: Button
    lateinit var btnFlashPay: ImageView
    var totalGrandfinal = 0.0
    lateinit var checkoutPayItemslistview: ListView
    private lateinit var dialog: Dialog
    private lateinit var invoiceDialog: Dialog
    private var gdaxUrl = "ws://73.36.65.41:8095/SendCommands"
    lateinit var sharedPreferences: CustomSharedPreferences
    lateinit var btcRate: TextView
    private lateinit var totalpay: TextView
    lateinit var taxpay: TextView
    lateinit var grandtotal: TextView
    lateinit var checkOutPayItemAdapter: CheckOutMainListAdapter
    var priceInBTC = 0.0
    var priceInCurrency = 0.0
    var taxpayInBTC = 1.0
    var taxpayInCurrency = 1.0
    var grandTotalInCurrency = 0.0
    var getGrandTotalInBTC = 0.0
    lateinit var qRCodeImage: ImageView
    var currentTransactionLabel = ""
    var taxInBtcAmount = 0.0
    var taxtInCurennccyAmount = 0.0
    var taxBtcOnP3ToPopUp = 0.0
    var taxUsdOnP3ToPopUp = 0.0
    lateinit var setTextWithSpan: TextView
    var labelGlobal = "sale123"
    var blReceiver: BroadcastReceiver? = null
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
    var merchantData: MerchantData? = null
    lateinit var confirmingProgressDialog: ProgressDialog
    private var isCreatingInvoice = false
    private var broadcastReceiver: BroadcastReceiver? = null
    private val intentFilter = IntentFilter(AppConstants.PAYMENT_RECEIVED_NOTIFICATION)
    lateinit var distributeGetPaidDialog: Dialog
    var selectedItems: List<Items> = ArrayList()
    var selectedFlashPayClient: NearbyClients? = null
    override fun onDestroy() {
        super.onDestroy()
        requireContext().stopService(Intent(requireContext(), MyLogOutService::class.java))
        if (blReceiver != null) {
            requireActivity().unregisterReceiver(blReceiver)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_check_outs3, container, false)
        Log.d(TAG, "onCreateView: CheckOutsFragment3")
        isFundingInfoGetSuccefully = false
        setTextWithSpan = view.findViewById(R.id.footer)
        val boldStyle = StyleSpan(Typeface.BOLD)
        setTextWithSpan(
            setTextWithSpan,
            getString(R.string.welcome_text),
            getString(R.string.welcome_text_bold),
            boldStyle
        )
        confirmInvoicePamentProgressDialog = ProgressDialog(requireContext())
        confirmInvoicePamentProgressDialog?.setMessage("Confirming Payment")
        updatingInventoryProgressDialog = ProgressDialog(requireContext())
        updatingInventoryProgressDialog?.setMessage("Updating..")
        createInvoiceProgressDialog = ProgressDialog(requireContext())
        createInvoiceProgressDialog?.setMessage("Creating Invoice")
        exitFromServerProgressDialog = ProgressDialog(requireContext())
        exitFromServerProgressDialog?.setMessage("Exiting")
        getItemListprogressDialog = ProgressDialog(requireContext())
        getItemListprogressDialog?.setMessage("Loading...")
        btcRate = view.findViewById(R.id.btcRateTextview)
        totalpay = view.findViewById(R.id.totalpay)
        taxpay = view.findViewById(R.id.taxpay)
        grandtotal = view.findViewById(R.id.grandtotal)
        fContext = requireContext()
        clearout = view.findViewById(R.id.clearout)
        gdaxUrl = CustomSharedPreferences().getvalueofMWSCommand("mws_command", requireContext())
        sharedPreferences = CustomSharedPreferences()
        merchantData = sessionService.getMerchantData()
        if (GlobalState.getInstance().tax != null) {
            taxpayInBTC = GlobalState.getInstance().tax.taxInBTC
            taxpayInCurrency = GlobalState.getInstance().tax.taxInUSD
        }
        if (CheckNetwork.isInternetAvailable(fContext)) {
            subscribeChannel()
            fundingNodeInfo
        } else {
            setReceivableAndCapacity("0", "0", false)
            setcurrentrate("Not Found")
        }
        paywithclightbtn = view.findViewById(R.id.imageView5)
        paywithclightbtn.setOnClickListener {
            createGrandTotalForInvoice(false)
        }
        checkoutPayItemslistview = view.findViewById(R.id.checkout2listview)

        clearout.setOnClickListener {
            lightningService.listPeers().observe(viewLifecycleOwner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        if (it.data != null && it.data.getChannels().isNotEmpty()) {
                            isReceivableGet = true
                            var msat = 0.0
                            var mcap = 0.0
                            for (tempListFundChanel in it.data.getChannels()) {
                                if (it.data.isConnected && tempListFundChanel.state.equals(
                                        "CHANNELD_NORMAL",
                                        ignoreCase = true
                                    )
                                ) {
                                    val tempmsat = tempListFundChanel.getReceivable_msatoshi()
                                        .toString() + ""
                                    val tempmCap =
                                        tempListFundChanel.getSpendable_msatoshi()
                                            .toString() + ""
                                    var tmsat = 0.0
                                    var tmcap = 0.0
                                    try {
                                        tmsat = tempmsat.toDouble()
                                        tmcap = tempmCap.toDouble()
                                        val value = BigDecimal(tempmCap)
                                        val doubleValue = value.toDouble()
                                        Log.e("StringToDouble:", doubleValue.toString())
                                    } catch (e: Exception) {
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
                        } else {
                            setReceivableAndCapacity("0", "0", false)
                        }
                    }

                    Status.ERROR -> {
                        confirmingProgressDialog.dismiss()
                        showToast(it.message.toString())
                    }

                    Status.LOADING -> {
                        confirmingProgressDialog.dismiss()
                        showToast(it.message.toString())
                    }
                }
            }

        }
        btnFlashPay = view.findViewById(R.id.btnFlashPay)
        btnFlashPay.setOnClickListener { v: View? ->
            confirmingProgressDialog.show()
            nearbyClients
        }
        confirmingProgressDialog = ProgressDialog(fContext)
        confirmingProgressDialog.setMessage("Confirming...")
        confirmingProgressDialog.setCancelable(false)
        confirmingProgressDialog.setCanceledOnTouchOutside(false)
        return view
    }


    override fun onResume() {
        super.onResume()
        selectedItems = GlobalState.getInstance().selectedItems.toList()
        setAdapter()
        btcService.currentBtc.observe(viewLifecycleOwner) {
        }
    }

    private fun createGrandTotalForInvoice(fromFlashPay: Boolean) {
        Log.d("TEST_DOUBLE_CHECKOUT", "createGrandTotalForInvoice: ")
        if (selectedItems.isNotEmpty()) {
            priceInCurrency = 0.0
            priceInBTC = 0.0
            grandTotalInCurrency = 0.0
            getGrandTotalInBTC = 0.0
            for (q in selectedItems.indices) {
                priceInCurrency += selectedItems[q].price.toDouble()
                if (btcService.btcPrice != 0.0) {
                    priceInBTC = 1 / btcService.btcPrice
                    priceInBTC *= priceInCurrency
                    priceInBTC = round(priceInBTC, 9)
                    grandTotalInCurrency = priceInCurrency + taxtInCurennccyAmount
                    getGrandTotalInBTC = priceInBTC + taxInBtcAmount
                    getGrandTotalInBTC = round(getGrandTotalInBTC, 9)
                    dialogBoxForInvoice(fromFlashPay)
                } else {
                    showToast("No BTC Rate")
                }
            }
        } else {
            showToast("Cart is Empty")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setcurrentrate(x: String) {
        btcRate.text = "$" + x + "BTC/USD"
    }

    fun refreshAdapter() {
        setAdapter()
    }

    @SuppressLint("SetTextI18n")
    fun setAdapter() {
        var countitem = 0
        for (items in selectedItems) {
            countitem += items.selectQuatity
        }
        (requireActivity() as CheckOutMainActivity).updateCartIcon(countitem)
        checkOutPayItemAdapter = CheckOutMainListAdapter(requireContext(), selectedItems)
        checkoutPayItemslistview.adapter = checkOutPayItemAdapter
        checkoutPayItemslistview.onItemLongClickListener =
            OnItemLongClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                val builder = android.app.AlertDialog.Builder(requireContext())
                builder.setTitle(getString(R.string.delete_title))
                builder.setMessage(getString(R.string.delete_subtitle))
                builder.setCancelable(true)

                // Action if user selects 'yes'
                builder.setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    val tem = selectedItems[i]
                    if (tem.isManual != null) {
                        checkOutPayItemAdapter.notifyDataSetChanged()
                        setAdapter()
                    } else {
                        checkOutPayItemAdapter.notifyDataSetChanged()
                        setAdapter()
                    }
                }

                // Actions if user selects 'no'
                builder.setNegativeButton("No") { _: DialogInterface?, _: Int -> }

                // Create the alert dialog using alert dialog builder
                val dialog = builder.create()
                dialog.setCanceledOnTouchOutside(false)
                // Finally, display the dialog when user press back button
                dialog.show()
                true
            }

        if (selectedItems.isNotEmpty()) {
            GlobalState.getInstance().isCheckoutBtnPress = false
            priceInCurrency = 0.0
            priceInBTC = 0.0
            for (q in selectedItems.indices) {
                val total: Double = if (selectedItems[q].price != null) {
                    selectedItems[q].selectQuatity * selectedItems[q].price.toDouble()
                } else {
                    selectedItems[q].selectQuatity * "0".toDouble()
                }
                priceInCurrency += total
                //format  ::   Total : 1.25 BTC /  $34.95 USD
                if (btcService.btcPrice != 0.0) {
                    priceInBTC = 1 / btcService.btcPrice
                    priceInBTC *= priceInCurrency
                    priceInBTC = round(priceInBTC, 9)
                    priceInCurrency = round(priceInCurrency, 2)
                    val precision = DecimalFormat("0.00")
                    totalpay.text =
                        "Total:" + exactFigure(priceInBTC) + " BTC/ $" + precision.format(
                            priceInCurrency
                        )
                    val percent = GlobalState.getInstance().tax.taxpercent / 100
                    taxInBtcAmount = priceInBTC * percent
                    taxBtcOnP3ToPopUp = taxInBtcAmount
                    taxInBtcAmount = round(taxInBtcAmount, 9)
                    taxtInCurennccyAmount = priceInCurrency * percent
                    taxUsdOnP3ToPopUp = taxtInCurennccyAmount
                    taxtInCurennccyAmount = round(taxtInCurennccyAmount, 2)
                    taxpay.text =
                        "Tax:" + exactFigure(taxInBtcAmount) + " BTC/ $" + precision.format(
                            taxtInCurennccyAmount
                        )
                    grandTotalInCurrency = priceInCurrency + taxtInCurennccyAmount
                    getGrandTotalInBTC = priceInBTC + taxInBtcAmount
                    getGrandTotalInBTC = round(getGrandTotalInBTC, 9)
                    grandTotalInCurrency = round(grandTotalInCurrency, 2)
                    grandtotal.text =
                        exactFigure(getGrandTotalInBTC) + " BTC/ $" + precision.format(
                            grandTotalInCurrency
                        )
                    totalGrandfinal = getGrandTotalInBTC
                } else {
                    totalpay.text = "Total:" + "0.0 BTC" + " / " + "0.00 $"
                }
            }
        } else {
            (requireActivity() as CheckOutMainActivity).updateCartIcon(0)
            //set default rates
            totalpay.text = "Total:" + "0.0 BTC" + " / " + "0.00 $"
            taxpay.text = "Tax:0.0 BTC/0.00 $"
            grandtotal.text = "0.0 BTC/ 0.00 $"
        }
        checkOutPayItemAdapter.notifyDataSetChanged()
    }

    private fun dialogBoxForInvoice(fromFlashPay: Boolean) {
        Log.d("TEST_DOUBLE_CHECKOUT", "dialogBoxForInvoice: ")
        val tsLong = System.currentTimeMillis() / 1000
        val uNixtimeStamp = tsLong.toString()
        val dmSatoshi: Double
        val dSatoshi: Double = totalGrandfinal * AppConstants.btcToSathosi
        dmSatoshi = dSatoshi * AppConstants.satoshiToMSathosi
        val formatter: NumberFormat = DecimalFormat("#0")
        val rMSatoshi = formatter.format(dmSatoshi)
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        invoiceDialog = Dialog(requireContext())
        invoiceDialog.setContentView(R.layout.dialoglayoutinvoice)
        invoiceDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        invoiceDialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        invoiceDialog.setCancelable(true)
        val et_msatoshi = invoiceDialog.findViewById<EditText>(R.id.et_msatoshi)
        et_msatoshi.inputType = InputType.TYPE_NULL
        et_msatoshi.setText(rMSatoshi)
        val et_label = invoiceDialog.findViewById<EditText>(R.id.et_lable)
        et_label.inputType = InputType.TYPE_NULL
        val label = "sale$uNixtimeStamp"
        et_label.setText(label)
        labelGlobal = "sale$uNixtimeStamp"
        val et_description = invoiceDialog.findViewById<EditText>(R.id.et_description)
        val ivBack = invoiceDialog.findViewById<ImageView>(R.id.iv_back_invoice)
        qRCodeImage = invoiceDialog.findViewById(R.id.imgQR)
        val btnCreatInvoice = invoiceDialog.findViewById<Button>(R.id.btn_createinvoice)
        qRCodeImage.visibility = GONE
        ivBack.setOnClickListener { invoiceDialog.dismiss() }
        btnCreatInvoice.setOnClickListener {
            val msatoshi = et_msatoshi.text.toString()
            val label1 = et_label.text.toString()
            val descrption = et_description.text.toString()
            if (msatoshi.isEmpty()) {
                showToast("MSATOSHI" + getString(R.string.empty))
                return@setOnClickListener
            }
            if (label1.isEmpty()) {
                showToast("Label" + getString(R.string.empty))
                return@setOnClickListener
            }
            if (descrption.isEmpty()) {
                showToast("Description" + getString(R.string.empty))
                return@setOnClickListener
            }
            currentTransactionLabel = label1
            isCreatingInvoice = false
            createInvoice(msatoshi, label1, descrption, fromFlashPay)
            invoiceDialog.dismiss()
        }
        invoiceDialog.show()
        //   createInvoice(rMSatoshi, label, "Flashpay")
    }

    private fun getBitMapImg(hex: String?, width: Int, height: Int): Bitmap {
        val multiFormatWriter = MultiFormatWriter()
        var bitMatrix: BitMatrix? = null
        try {
            Log.d(QR_CODE, hex!!)
            bitMatrix = multiFormatWriter.encode(hex, BarcodeFormat.QR_CODE, width, height)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.createBitmap(bitMatrix)
    }

    private fun exactFigure(value: Double): String {
        val d = BigDecimal(value.toString())
        return d.toPlainString()
    }

    override fun onActivityResult(mRequestCode: Int, mResultCode: Int, mDataIntent: Intent?) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent)
        if (mRequestCode == 1234) { // HANDLE LockIng
            if (mResultCode == Activity.RESULT_OK) {
                sendReceivable()
            }
        }
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

    private val nearbyClients: Unit
        get() {
            val accessToken =
                CustomSharedPreferences().getvalue("accessTokenLogin", requireContext())
            val token = "Bearer $accessToken"
            val call = webservice.getNearbyClients(token)
            call.enqueue(object : Callback<NearbyClientResponse?> {
                override fun onResponse(
                    call: Call<NearbyClientResponse?>,
                    response: Response<NearbyClientResponse?>
                ) {
                    requireActivity().runOnUiThread { confirmingProgressDialog.dismiss() }
                    if (response.body() != null) {
                        val clientListModel = response.body()
                        val list = clientListModel?.data
                        if (list != null) {
                            if (list.size > 0) {
                                //ArrayList<StoreClients> list1=list;
                                showDialogNearbyClients(list)
                            }
                        }
                    } else {
                        showToast(response.message())
                    }
                }

                override fun onFailure(call: Call<NearbyClientResponse?>, t: Throwable) {
                    requireActivity().runOnUiThread { confirmingProgressDialog.dismiss() }
                    showToast(t.message)
                }
            })
        }

    private fun showDialogNearbyClients(list: List<NearbyClients>) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_merchant_node_scrollbar)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = MerchantNodeAdapter(list, requireContext()) {
            dialog.dismiss()
            isCreatingInvoice = false
            selectedFlashPayClient = it
            createGrandTotalForInvoice(fromFlashPay = true)
        }
        recyclerView.adapter = adapter
        dialog.show()
    }

    private fun createSocketIOInstance(): Socket? {
        val accessToken =
            CustomSharedPreferences().getvalue("accessTokenLogin", requireContext())
        val options = IO.Options()
        val headers: MutableMap<String, List<String>> = HashMap()
        val bearer = "Bearer $accessToken"
        headers["Authorization"] = listOf(bearer)
        options.extraHeaders = headers
        var mSocket: Socket? = null
        try {
            mSocket = IO.socket("https://realtime.nextlayer.live", options)
            mSocket?.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        mSocket?.on("err") { args: Array<Any> ->
            requireActivity().runOnUiThread {
                val data: JSONObject = args[0] as JSONObject
                Log.d("Socket", data.toString())
                Toast.makeText(requireContext(), data["message"].toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return mSocket
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
        btcRemainingCapacity = btcCapacity
        usdRemainingCapacity = usdCapacity
        goToClearOutDialog(sta)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
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
                val km =
                    requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (km.isKeyguardSecure) {
                    val authIntent = km.createConfirmDeviceCredentialIntent("Authorize Payment", "")
                    startActivityForResult(authIntent, INTENT_AUTHENTICATE)
                } else {
                    sendReceivable()
                }
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("Please Try Again")
                    .setCancelable(false)
                    .setPositiveButton("Retry!") { dialog: DialogInterface, id: Int ->
                        dialog.cancel()
                        clearOutDialog.dismiss()
                    }.show()
            }
        }
        noBtn.setOnClickListener { view: View? -> clearOutDialog.dismiss() }
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
                getRoute(routingNodeId, mSatoshiSpendableTotal.toString() + "")
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("Funding Node Id is Missing")
                    .setCancelable(false)
                    .setPositiveButton("Retry!") { dialog: DialogInterface, id: Int ->
                        dialog.cancel()
                        fundingNodeInfo
                    }.show()
            }
        } else {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Funding Node Id is Missing")
                .setCancelable(false)
                .setPositiveButton("Retry!") { dialog: DialogInterface, id: Int ->
                    dialog.cancel()
                    fundingNodeInfo
                }.show()
        }
    }

    private fun subscribeChannel() {
        btcService.currentBtc.observe(viewLifecycleOwner) {
            if (it.status == Status.SUCCESS) {
                setcurrentrate(it.data?.price.toString())
                setAdapter()
            }
        }
    }

    private var globalInvoice: MWSWebSocketResponse? = null
    var globalRMSatoshi: String? = null
    private var globalLabel: String? = null
    private var globalDescription: String? = null

    fun createInvoice(
        rMSatoshi: String?,
        label: String?,
        description: String?,
        fromFlashPay: Boolean,
    ) {
        globalRMSatoshi = rMSatoshi
        globalLabel = label
        globalDescription = description

        lightningService.createInvoice(rMSatoshi, label, description).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    confirmingProgressDialog.dismiss()
                    val resp = it.data
                    resp?.let { mws ->
                        if (mws.isError) {
                            Log.e(TAG, "Error: " + mws.message)
                            showToast(mws.message)
                        } else if (mws.code == 724) {
                            Log.e(TAG, mws.code.toString() + "")
                            goTo2FaPasswordDialog()
                        } else if (mws.payment_hash != null) {
                            Log.e(TAG, "Hash: " + mws.payment_hash)
                            globalInvoice = null
                            globalInvoice = resp
                            try {
                                if (fromFlashPay) {
                                    val socket = createSocketIOInstance()
                                    val jsonObject = JSONObject()
                                    try {
                                        jsonObject.accumulate(
                                            "to",
                                            selectedFlashPayClient!!.client_id
                                        )
                                        jsonObject.accumulate("type", "FP_Merchant_Payment_Req")
                                        val payloadJsonObject = JSONObject()
                                        payloadJsonObject.accumulate("rMSatoshi", rMSatoshi)
                                        payloadJsonObject.accumulate("label", label)
                                        payloadJsonObject.accumulate("description", description)
                                        payloadJsonObject.accumulate("bolt11", resp.bolt11)
                                        jsonObject.accumulate("payload", payloadJsonObject)
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                    Log.d(this.TAG, "sending msg -> $jsonObject")
                                    socket?.emit("msg", jsonObject, object : Acknowledgement() {
                                        override fun call(vararg args: Any) {
                                            super.call(*args)
                                            requireActivity().runOnUiThread {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Acknowledgement",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }
                                        }
                                    })
                                } else {
                                    dialogBoxQRCodePayment()
                                }

                                listenToFcmBroadcast()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    confirmingProgressDialog.dismiss()
                    showToast(it.message.toString())
                }

                Status.LOADING -> {
                    confirmingProgressDialog.show()
                }
            }
        }

    }

    private fun listInvoices(label: String) {
        val clientCoinPrice = OkHttpClient()
        val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val token =
                    sharedPreferences.getvalueofaccestoken("accessToken", requireContext())
                val json =
                    "{\"token\" : \"$token\", \"commands\" : [\"lightning-cli listinvoices $label\"] }"
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

    private fun getRoute(noteId: String, receivable: String) {
        lightningService.getRoute(noteId, receivable).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    confirmingProgressDialog.dismiss()
                    sendReceivables(noteId, it.data!!)
                }

                Status.ERROR -> {
                    confirmingProgressDialog.dismiss()
                    showToast(it.message.toString())
                }

                Status.LOADING -> {
                    confirmingProgressDialog.show()
                }
            }
        }
    }

    private fun sendReceivables(nodeId: String, receivable: String) {
        lightningService.sendReceivables(nodeId, receivable).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    confirmingProgressDialog.dismiss()
                    showToast(it.data!!.msatoshi.toString())
                }

                Status.ERROR -> {
                    confirmingProgressDialog.dismiss()
                    showToast(it.message.toString())
                }

                Status.LOADING -> {
                    confirmingProgressDialog.show()
                }
            }
        }
    }

    private fun listenToFcmBroadcast() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    if (intent.extras != null) {
                        requireActivity().runOnUiThread {
                            val notificationModel = Gson().fromJson(
                                intent.getStringExtra(AppConstants.PAYMENT_INVOICE),
                                FirebaseNotificationModel::class.java
                            )
                            Log.d(TAG, notificationModel.invoice_label)
                            fcmReceived()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        requireContext().registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun fcmReceived() {
        if (distributeGetPaidDialog.isShowing) {
            distributeGetPaidDialog.dismiss()
        }
        unregisterBroadcastReceiver()
        confirmPayment()
    }

    override fun onStop() {
        super.onStop()
        unregisterBroadcastReceiver()
    }

    private fun unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            requireContext().unregisterReceiver(broadcastReceiver)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun dialogBoxQRCodePayment() {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        distributeGetPaidDialog = Dialog(requireContext())
        distributeGetPaidDialog.setContentView(R.layout.dialoglayoutgetpaiddistribute)
        distributeGetPaidDialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        distributeGetPaidDialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        distributeGetPaidDialog.setCancelable(false)
        val title = distributeGetPaidDialog.findViewById<TextView>(R.id.tv_title)
        val btnCreateInvoice = distributeGetPaidDialog.findViewById<Button>(R.id.btn_createinvoice)
        val etMsatoshi = distributeGetPaidDialog.findViewById<EditText>(R.id.et_msatoshi)
        val etLabel = distributeGetPaidDialog.findViewById<EditText>(R.id.et_lable)
        val etDescription = distributeGetPaidDialog.findViewById<EditText>(R.id.et_description)
        val ivBack = distributeGetPaidDialog.findViewById<ImageView>(R.id.iv_back_invoice)
        qRCodeImage = distributeGetPaidDialog.findViewById(R.id.imgQR)
        title.text = "Get Paid"
        etLabel.inputType = InputType.TYPE_NULL
        etLabel.setText(globalLabel)
        etMsatoshi.setText(globalRMSatoshi)
        etDescription.setText(globalDescription)
        qRCodeImage.visibility = GONE
        btnCreateInvoice.visibility = GONE
        if (globalInvoice != null) {
            if (globalInvoice!!.bolt11 != null) {
                val temHax = globalInvoice!!.bolt11
                val multiFormatWriter = MultiFormatWriter()
                try {
                    Log.d(QR_CODE, temHax)
                    val bitMatrix =
                        multiFormatWriter.encode(temHax, BarcodeFormat.QR_CODE, 600, 600)
                    val barcodeEncoder = BarcodeEncoder()
                    val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                    qRCodeImage.setImageBitmap(bitmap)
                    qRCodeImage.visibility = VISIBLE
                } catch (e: WriterException) {
                    e.printStackTrace()
                }
            }
        }
        ivBack.setOnClickListener { v: View? -> distributeGetPaidDialog.dismiss() }
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                try {
                    timer.cancel()
                    requireActivity().runOnUiThread { distributeGetPaidDialog.dismiss() }
                } catch (e: Exception) {
                    Log.d("Timer Exception", (e.message.toString()))
                }
            }
        }, (1000 * 60 * 5).toLong())
        distributeGetPaidDialog.show()
    }

    private fun confirmPayment() {
        globalLabel?.let { it ->
            lightningService.confirmPayment(it).observe(viewLifecycleOwner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        confirmingProgressDialog.dismiss()
                        if (it.data != null) {
                            if (it.data.status == "paid") {
                                dialogBoxForConfirmPaymentInvoice(it.data)
                                confirmInvoicePamentProgressDialog?.dismiss()
                                confirmingProgressDialog.dismiss()
                            } else {
                                confirmingProgressDialog.dismiss()
                                distributeGetPaidDialog.dismiss()
                                confirmInvoicePamentProgressDialog?.dismiss()
                                AlertDialog.Builder(requireContext())
                                    .setMessage("Payment Not Received")
                                    .setPositiveButton("Retry", null)
                                    .show()
                            }
                        } else {
                            confirmingProgressDialog.dismiss()
                            distributeGetPaidDialog.dismiss()
                            confirmInvoicePamentProgressDialog?.dismiss()
                            AlertDialog.Builder(requireContext())
                                .setMessage("Payment Not Received")
                                .setPositiveButton("Retry", null)
                                .show()
                        }
                    }

                    Status.ERROR -> {
                        confirmingProgressDialog.dismiss()
                        showToast(it.message.toString())
                    }

                    Status.LOADING -> {
                        confirmingProgressDialog.show()
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun dialogBoxForConfirmPaymentInvoice(invoice: Invoice?) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        distributeGetPaidDialog = Dialog(requireContext())
        distributeGetPaidDialog.setContentView(R.layout.customlayoutofconfirmpaymentdialogformerchantadmin)
        distributeGetPaidDialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        distributeGetPaidDialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        //        dialog.getWindow().setLayout(500, 500);
        distributeGetPaidDialog.setCancelable(false)
        //init dialog views
        val ivBack = distributeGetPaidDialog.findViewById<ImageView>(R.id.iv_back_invoice)
        val amount = distributeGetPaidDialog.findViewById<TextView>(R.id.et_amount)
        val paymentPreimage = distributeGetPaidDialog.findViewById<ImageView>(R.id.et_preimage)
        val paidAt = distributeGetPaidDialog.findViewById<TextView>(R.id.et_paidat)
        val purchasedItems =
            distributeGetPaidDialog.findViewById<TextView>(R.id.et_perchaseditems)
        val printInvoice = distributeGetPaidDialog.findViewById<Button>(R.id.btn_printinvoice)
        amount.visibility = GONE
        paymentPreimage.visibility = GONE
        paidAt.visibility = GONE
        purchasedItems.visibility = GONE
        //   tax.setVisibility(View.GONE);
        printInvoice.visibility = GONE
        if (invoice != null) {
            if (invoice.status == "paid") {
                amount.visibility = VISIBLE
                paymentPreimage.visibility = VISIBLE
                paidAt.visibility = VISIBLE
                purchasedItems.visibility = VISIBLE
                //    tax.setVisibility(View.VISIBLE);
                printInvoice.visibility = VISIBLE
                val amounttempusd = round(btcService.btcToUsd(satoshiToBtc(invoice.msatoshi)), 2)
                val precision = DecimalFormat("0.00")
                amount.text = """
                    ${exactFigure(round(btcService.btcToUsd(invoice.msatoshi), 9))}BTC
                    ${"$"}${precision.format(round(amounttempusd, 2))}USD
                    """.trimIndent()
                paymentPreimage.setImageBitmap(getBitMapImg(invoice.payment_preimage, 300, 300))
                paidAt.text =
                    dateStringUTCTimestamp(invoice.paid_at, AppConstants.OUTPUT_DATE_FORMATE)
                purchasedItems.text = invoice.description
            } else {
                amount.visibility = VISIBLE
                paymentPreimage.visibility = VISIBLE
                paidAt.visibility = VISIBLE
                purchasedItems.visibility = VISIBLE
                //    tax.setVisibility(View.VISIBLE);
                printInvoice.visibility = VISIBLE
                val precision = DecimalFormat("0.00")
                amount.text = """
                    ${exactFigure(round(satoshiToBtc(invoice.msatoshi), 9))}BTC
                    ${"$"}${
                    precision.format(
                        round(
                            btcService.btcToUsd(satoshiToBtc(invoice.msatoshi)),
                            2
                        )
                    )
                }USD
                    """.trimIndent()
                paidAt.text =
                    dateStringUTCTimestamp(invoice.paid_at, AppConstants.OUTPUT_DATE_FORMATE)
                paymentPreimage.setImageBitmap(getBitMapImg(invoice.payment_preimage, 300, 300))
                purchasedItems.text = "N/A"
            }
        }
        printInvoice.setOnClickListener {
            if (invoice != null && invoice.status == "paid") {
                PrintDialogFragment(invoice, null, selectedItems) {
                    GlobalState.getInstance().selectedItems.clear()
                    GlobalState.getInstance().itemsList.clear()
                    (requireActivity() as CheckOutMainActivity).swipeToCheckOutFragment3(
                        0
                    )
                }.show(childFragmentManager, null)
                distributeGetPaidDialog.dismiss()
            }
        }
        ivBack.setOnClickListener { v: View? -> distributeGetPaidDialog.dismiss() }
        distributeGetPaidDialog.show()
    }

    companion object {
        var isReceivableGet = false
    }
}