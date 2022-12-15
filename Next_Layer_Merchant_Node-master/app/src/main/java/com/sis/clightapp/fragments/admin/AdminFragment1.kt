package com.sis.clightapp.fragments.admin

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.sis.clightapp.Interface.ApiClient
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.R
import com.sis.clightapp.adapter.AdminReceiveablesListAdapter
import com.sis.clightapp.adapter.AdminSendablesListAdapter
import com.sis.clightapp.fragments.merchant.MerchantBaseFragment
import com.sis.clightapp.fragments.printing.PrintDialogFragment
import com.sis.clightapp.fragments.shared.Auth2FaFragment
import com.sis.clightapp.model.GsonModel.*
import com.sis.clightapp.model.REST.TransactionResp
import com.sis.clightapp.services.BTCService
import com.sis.clightapp.services.LightningService
import com.sis.clightapp.session.MyLogOutService
import com.sis.clightapp.util.AppConstants
import com.sis.clightapp.util.CustomSharedPreferences
import com.sis.clightapp.util.GlobalState
import com.sis.clightapp.util.Status
import org.json.JSONException
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

/**
 * By
 * khuwajahassan15@gmail.com
 * 17/09/2020
 */
class AdminFragment1 : AdminBaseFragment() {

    private val btcService: BTCService by inject()
    private val lightningService: LightningService by inject()

    private var INTENT_AUTHENTICATE = 1234
    private var setwidht = 0
    var setheight = 0
    lateinit var distributebutton: Button
    private lateinit var commandeerbutton: Button
    private lateinit var qRCodeImage: ImageView
    private lateinit var receiveableslistview: ListView
    private lateinit var sendeableslistview: ListView
    var adminReceiveablesListAdapter: AdminReceiveablesListAdapter? = null
    var adminSendablesListAdapter: AdminSendablesListAdapter? = null
    private var fcmBroadcastReceiver: BroadcastReceiver? = null

    lateinit var progressDialog: ProgressDialog


    var sharedPreferences = CustomSharedPreferences()


    var currentTransactionLabel = ""
    var bolt11fromqr = ""
    var distributeDescription = ""
    private var gdaxUrl = "ws://73.36.65.41:8095/SendCommands"
    lateinit var fromDaterReceivables: EditText
    lateinit var toDateReceivables: EditText
    lateinit var fromDateSendables: EditText
    lateinit var toDateSendables: EditText
    lateinit var picker: DatePickerDialog
    var isInApp = true
    var setTextWithSpan: TextView? = null
    private var AMOUNT_BTC = 0.0
    private var AMOUNT_USD = 0.0
    private var CONVERSION_RATE = 0.0
    private var MSATOSHI = 0.0
    var getPaidLABEL = ""
    private var distributeGetPaidDialog: Dialog? = null

    private var receivables = arrayListOf<Sale>()
    private var sendables = arrayListOf<Refund>()

    private var invoiceLabel: String? = null

    override fun onDestroy() {
        super.onDestroy()
        requireContext().stopService(Intent(context, MyLogOutService::class.java))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin1, container, false)
        setTextWithSpan = view.findViewById(R.id.poweredbyimage)
        val boldStyle = StyleSpan(Typeface.BOLD)
        setTextWithSpan(
            setTextWithSpan,
            getString(R.string.welcome_text),
            getString(R.string.welcome_text_bold),
            boldStyle
        )
        fromDaterReceivables = view.findViewById(R.id.et_from_date_sale)
        toDateReceivables = view.findViewById(R.id.et_to_date_sale)
        fromDateSendables = view.findViewById(R.id.et_from_date_refund)
        toDateSendables = view.findViewById(R.id.et_to_date_refund)
        distributebutton = view.findViewById(R.id.distributebutton)
        commandeerbutton = view.findViewById(R.id.commandeerbutton)
        val qrScan = IntentIntegrator(activity)
        qrScan.setOrientationLocked(false)
        val prompt = resources.getString(R.string.scanqrforbolt11)
        qrScan.setPrompt(prompt)

        progressDialog = ProgressDialog(context)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Loading ...")

        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        setwidht = width * 45
        setwidht /= 100
        setheight = height / 2
        receiveableslistview = view.findViewById(R.id.receivablesListview)
        sendeableslistview = view.findViewById(R.id.sednablesListview)
        receiveableslistview.minimumWidth = setwidht
        val lp = receiveableslistview.layoutParams
        lp.width = setwidht
        receiveableslistview.layoutParams = lp
        sharedPreferences = CustomSharedPreferences()
        val lp2 = sendeableslistview.layoutParams
        lp2.width = setwidht
        sendeableslistview.layoutParams = lp2
        gdaxUrl = CustomSharedPreferences().getvalueofMWSCommand("mws_command", context)
        btcService.currentBtc.observe(viewLifecycleOwner) {

        }
        distributebutton.setOnClickListener { dialogBoxForGetPaidDistribute() }
        commandeerbutton.setOnClickListener {
            PrintDialogFragment().show(childFragmentManager,null)
            return@setOnClickListener
            isInApp = false
            val km = requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardSecure) {
                val authIntent = km.createConfirmDeviceCredentialIntent("Authorize Payment", "")
                startActivityForResult(authIntent, INTENT_AUTHENTICATE)
            } else {
                dialogBoxForRefundCommandeer()
            }
        }
        fromDaterReceivables.inputType = InputType.TYPE_NULL
        toDateReceivables.inputType = InputType.TYPE_NULL
        fromDateSendables.inputType = InputType.TYPE_NULL
        toDateSendables.inputType = InputType.TYPE_NULL

        fromDaterReceivables.setOnClickListener {
            val cldr = Calendar.getInstance()
            val day = cldr[Calendar.DAY_OF_MONTH]
            val month = cldr[Calendar.MONTH]
            val year = cldr[Calendar.YEAR]
            // date picker dialog
            picker = DatePickerDialog(
                requireContext(),
                { _: DatePicker?, year1: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val date = getDateInCorrectFormat(year1, monthOfYear, dayOfMonth)
                    fromDaterReceivables.setText(date)
                    fromDateSendables.setText("")
                    toDateSendables.setText("")
                    toDateReceivables.setText("")
                    loadObservers()
                }, year, month, day
            )
            picker.datePicker.maxDate = System.currentTimeMillis()
            picker.show()
        }
        toDateReceivables.setOnClickListener {
            val cldr = Calendar.getInstance()
            val day = cldr[Calendar.DAY_OF_MONTH]
            val month = cldr[Calendar.MONTH]
            val year = cldr[Calendar.YEAR]
            picker = DatePickerDialog(
                requireContext(),
                { _: DatePicker?, year12: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val date = getDateInCorrectFormat(year12, monthOfYear, dayOfMonth)
                    toDateReceivables.setText(date)
                    fromDaterReceivables.setText("")
                    fromDateSendables.setText("")
                    toDateSendables.setText("")
                    loadObservers()
                }, year, month, day
            )
            picker.datePicker.maxDate = System.currentTimeMillis()
            picker.show()
        }
        fromDateSendables.setOnClickListener {
            val cldr = Calendar.getInstance()
            val day = cldr[Calendar.DAY_OF_MONTH]
            val month = cldr[Calendar.MONTH]
            val year = cldr[Calendar.YEAR]
            // date picker dialog
            picker = DatePickerDialog(
                requireContext(),
                { _: DatePicker?, year13: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val date = getDateInCorrectFormat(year13, monthOfYear, dayOfMonth)
                    fromDateSendables.setText(date)
                    toDateSendables.setText("")
                    fromDaterReceivables.setText("")
                    toDateReceivables.setText("")
                    loadObservers()
                }, year, month, day
            )
            picker.datePicker.maxDate = System.currentTimeMillis()
            picker.show()
        }
        toDateSendables.setOnClickListener {
            val cldr = Calendar.getInstance()
            val day = cldr[Calendar.DAY_OF_MONTH]
            val month = cldr[Calendar.MONTH]
            val year = cldr[Calendar.YEAR]
            picker = DatePickerDialog(
                requireContext(),
                { _: DatePicker?, year14: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val date = getDateInCorrectFormat(year14, monthOfYear, dayOfMonth)
                    toDateSendables.setText(date)
                    fromDateSendables.setText("")
                    fromDaterReceivables.setText("")
                    toDateReceivables.setText("")
                    loadObservers()
                }, year, month, day
            )
            picker.datePicker.maxDate = System.currentTimeMillis()
            picker.show()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        loadObservers()
    }

    private fun loadObservers() {
        lightningService.refundList().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    it.data?.refundArrayList?.let { data ->
                        setSendablesableAdapter()
                        this.sendables = data
                    }
                }
                Status.ERROR -> {
                    if (it.message == "2fa") {
                        Auth2FaFragment().show(childFragmentManager, null)
                    } else {
                        showToast(it.message)
                    }
                    showToast(it.message)
                }
                Status.LOADING -> {
                    progressDialog.show()
                }
            }
        }
        lightningService.invoiceList().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    it.data?.invoiceArrayList?.let { data ->
                        setReceiveablesAdapter()
                        this.receivables = data
                    }
                }
                Status.ERROR -> {
                    if (it.message == "2fa") {
                        Auth2FaFragment().show(childFragmentManager, null)
                    } else {
                        showToast(it.message)
                    }
                    showToast(it.message)
                }
                Status.LOADING -> {
                    progressDialog.show()
                }
            }
        }
    }

    private fun setReceiveablesAdapter() {
        adminReceiveablesListAdapter =
            AdminReceiveablesListAdapter(requireContext(), this.receivables)
        receiveableslistview.adapter = adminReceiveablesListAdapter

    }

    private fun setSendablesableAdapter() {
        adminSendablesListAdapter = AdminSendablesListAdapter(requireContext(), this.sendables)
        sendeableslistview.adapter = adminSendablesListAdapter

    }

    @SuppressLint("SetTextI18n")
    private fun dialogBoxForGetPaidDistribute() {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        distributeGetPaidDialog = Dialog(requireContext())
        distributeGetPaidDialog?.setContentView(R.layout.dialoglayoutgetpaiddistribute)
        distributeGetPaidDialog?.apply {
            window?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
            window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
            setCancelable(false)
            val etMsatoshi = findViewById<EditText>(R.id.et_msatoshi)
            val etLabel = findViewById<EditText>(R.id.et_lable)
            etLabel.inputType = InputType.TYPE_NULL
            etLabel.setText("sale$unixTimeStamp")
            getPaidLABEL = etLabel.text.toString()
            val etDescription = findViewById<EditText>(R.id.et_description)
            val ivBack = findViewById<ImageView>(R.id.iv_back_invoice)
            qRCodeImage = findViewById(R.id.imgQR)
            val btnCreatInvoice = findViewById<Button>(R.id.btn_createinvoice)
            qRCodeImage.visibility = View.GONE
            ivBack.setOnClickListener { dismiss() }
            btnCreatInvoice.setOnClickListener {
                if (GlobalState.getInstance().channel_btcResponseData == null) {
                    showToast("Btc rate not available")
                    return@setOnClickListener
                }
                val msatoshi = etMsatoshi.text.toString()
                val label = etLabel.text.toString()
                val description = etDescription.text.toString()
                if (msatoshi.isEmpty()) {
                    showToast("Amount" + getString(R.string.empty))
                    return@setOnClickListener
                }
                if (label.isEmpty()) {
                    showToast("Label" + getString(R.string.empty))
                    return@setOnClickListener
                }
                if (description.isEmpty()) {
                    showToast("Description" + getString(R.string.empty))
                    return@setOnClickListener
                }
                currentTransactionLabel = label
                AMOUNT_USD = msatoshi.toDouble()
                var priceInBTC = 1 / GlobalState.getInstance().channel_btcResponseData.price
                priceInBTC *= msatoshi.toDouble()
                AMOUNT_BTC = priceInBTC
                var amountInMsatoshi = priceInBTC * AppConstants.btcToSathosi
                MSATOSHI = amountInMsatoshi
                amountInMsatoshi *= AppConstants.satoshiToMSathosi
                CONVERSION_RATE = AMOUNT_USD / AMOUNT_BTC
                val formatter: NumberFormat = DecimalFormat("#0")
                val rMSatoshi = formatter.format(amountInMsatoshi)
                distributeDescription = description

                lightningService.createInvoice(rMSatoshi, label, description)
                    .observe(viewLifecycleOwner) {
                        when (it.status) {
                            Status.SUCCESS -> {
                                invoiceLabel = label
                                progressDialog.dismiss()
                                showToast(it.data?.bolt11)
                                if (it.data?.bolt11 != null) {
                                    val temHax = it.data.bolt11
                                    val multiFormatWriter = MultiFormatWriter()
                                    try {
                                        val bitMatrix = multiFormatWriter.encode(
                                            temHax,
                                            BarcodeFormat.QR_CODE,
                                            600,
                                            600
                                        )
                                        val barcodeEncoder = BarcodeEncoder()
                                        val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                                        qRCodeImage.setImageBitmap(bitmap)
                                        qRCodeImage.visibility = View.VISIBLE
                                        listenToFcmBroadcast()
                                    } catch (e: WriterException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            Status.ERROR -> {
                                progressDialog.dismiss()
                                if (it.message == "2fa") {
                                    Auth2FaFragment().show(childFragmentManager, null)
                                } else {
                                    showToast(it.message)
                                }
                            }
                            Status.LOADING -> {
                                progressDialog.show()
                            }
                        }
                    }
            }
            show()
        }
    }

    private fun dialogBoxForConfirmPaymentInvoice(invoice: Invoice) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.customlayoutofconfirmpaymentdialogformerchantadmin)
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        dialog.setCancelable(false)
        //init dialog views
        val ivBack = dialog.findViewById<ImageView>(R.id.iv_back_invoice)
        val amount = dialog.findViewById<TextView>(R.id.et_amount)
        val paymentPreImage = dialog.findViewById<ImageView>(R.id.et_preimage)
        val paidAt = dialog.findViewById<TextView>(R.id.et_paidat)
        val purchasedItems =
            dialog.findViewById<TextView>(R.id.et_perchaseditems)
        val printInvoice = dialog.findViewById<Button>(R.id.btn_printinvoice)
        amount.visibility = View.GONE
        paymentPreImage.visibility = View.GONE
        paidAt.visibility = View.GONE
        purchasedItems.visibility = View.GONE
        printInvoice.visibility = View.GONE
        if (invoice.status == "paid") {
            val invoiceForPrint = InvoiceForPrint()
            invoiceForPrint.msatoshi = invoice.msatoshi
            invoiceForPrint.payment_preimage = invoice.payment_preimage
            invoiceForPrint.paid_at = invoice.paid_at
            invoiceForPrint.purchasedItems = distributeDescription
            invoiceForPrint.desscription = distributeDescription
            invoiceForPrint.mode = "distributeGetPaid"
            GlobalState.getInstance().invoiceForPrint = invoiceForPrint
            amount.visibility = View.VISIBLE
            paymentPreImage.visibility = View.VISIBLE
            paidAt.visibility = View.VISIBLE
            purchasedItems.visibility = View.VISIBLE
            printInvoice.visibility = View.VISIBLE
            val amounttempusd = round(getUsdFromBtc(mSatoshoToBtc(invoice.msatoshi)), 2)
            val precision = DecimalFormat("0.00")
            amount.text = StringBuilder()
                .append(excatFigure(round(mSatoshoToBtc(invoice.msatoshi), 9)))
                .append("BTC\n$").append(precision.format(round(amounttempusd, 2)))
                .append("USD").toString()
            paymentPreImage.setImageBitmap(getBitMapImg(invoice.payment_preimage, 300, 300))
            paidAt.text =
                getDateFromUTCTimestamp(invoice.paid_at, AppConstants.OUTPUT_DATE_FORMATE)
            purchasedItems.text = distributeDescription
        } else {
            val invoiceForPrint = InvoiceForPrint()
            invoiceForPrint.msatoshi = 0.0
            invoiceForPrint.payment_preimage = "N/A"
            invoiceForPrint.paid_at = 0
            invoiceForPrint.mode = "distributeGetPaid"
            GlobalState.getInstance().invoiceForPrint = invoiceForPrint
            amount.visibility = View.VISIBLE
            paymentPreImage.visibility = View.VISIBLE
            paidAt.visibility = View.VISIBLE
            purchasedItems.visibility = View.VISIBLE
            printInvoice.visibility = View.VISIBLE
            val precision = DecimalFormat("0.00")
            amount.text = StringBuilder()
                .append(excatFigure(round(mSatoshoToBtc(invoice.msatoshi), 9)))
                .append("BTC\n$").append(
                    precision.format(
                        round(
                            getUsdFromBtc(mSatoshoToBtc(invoice.msatoshi)),
                            2
                        )
                    )
                ).append("USD").toString()
            paidAt.text =
                getDateFromUTCTimestamp(invoice.paid_at, AppConstants.OUTPUT_DATE_FORMATE)
            paymentPreImage.setImageBitmap(getBitMapImg(invoice.payment_preimage, 300, 300))
            purchasedItems.text = "N/A"
        }
        printInvoice.setOnClickListener {
            loadObservers()
            if (invoice.status == "paid") {
                val invoiceForPrint = GlobalState.getInstance().getInvoiceForPrint()
                if (invoiceForPrint != null) {
                    PrintDialogFragment().show(childFragmentManager, null)
                }
            }
        }
        ivBack.setOnClickListener {
            loadObservers()
            dialog.dismiss()
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showPayCompleteDialog(payresponse: Pay) {
        Log.e("errorhe", "showCofirmationDialog me agya")
        val invoiceForPrint = InvoiceForPrint()
        invoiceForPrint.destination = payresponse.destination
        invoiceForPrint.msatoshi = payresponse.msatoshi
        invoiceForPrint.payment_preimage = payresponse.payment_preimage
        invoiceForPrint.created_at = payresponse.created_at
        GlobalState.getInstance().invoiceForPrint = invoiceForPrint
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialoglayoutrefundcommandeerlaststepconfirmedpay)
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        val ivBack: ImageView =
            dialog.findViewById(R.id.iv_back_invoice)
        val textView: TextView = dialog.findViewById(R.id.textView2)
        val ok: Button = dialog.findViewById(R.id.btn_ok)
        dialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        dialog.setCancelable(false)
        textView.text = "Payment Status:" + payresponse.status
        if (payresponse.status == "complete") {
            ok.text = "Print"
        }
        ok.setOnClickListener {
            loadObservers()
            val invoiceForPrint = GlobalState.getInstance().getInvoiceForPrint()
            if (payresponse.status == "complete") {
                if (invoiceForPrint != null) {
                    PrintDialogFragment().show(childFragmentManager, null)
                } else {
                    dialog.dismiss()
                }
            } else {
                dialog.dismiss()
            }
        }
        ivBack.setOnClickListener {
            dialog.dismiss()
            loadObservers()
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun dialogBoxForRefundCommandeer() {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        val commandeerRefundDialog = Dialog(requireContext())
        commandeerRefundDialog.setContentView(R.layout.dialoglayoutrefundcommandeer)
        commandeerRefundDialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        commandeerRefundDialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        commandeerRefundDialog.setCancelable(false)
        val bolt11 = commandeerRefundDialog.findViewById<EditText>(R.id.bolt11val)
        val ivBack = commandeerRefundDialog.findViewById<ImageView>(R.id.iv_back_invoice)
        val title = commandeerRefundDialog.findViewById<TextView>(R.id.tv_title)
        title.text = "COMMANDEER"
        val btnNext = commandeerRefundDialog.findViewById<Button>(R.id.btn_next)
        val btnscanQr = commandeerRefundDialog.findViewById<Button>(R.id.btn_scanQR)
        ivBack.setOnClickListener { v: View? -> commandeerRefundDialog.dismiss() }
        btnNext.setOnClickListener { v: View? ->
            val bolt11value = bolt11.text.toString()
            if (bolt11value.isEmpty()) {
                showToast("Bolt11 " + getString(R.string.empty))
            } else {
                commandeerRefundDialog.dismiss()
                bolt11fromqr = bolt11value
                decodeInvoice(bolt11value)
            }
        }
        btnscanQr.setOnClickListener {
            IntentIntegrator.forSupportFragment(this@AdminFragment1).initiateScan()
        }
        commandeerRefundDialog.show()
    }

    //Getting the scan results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1234 -> {
                super.onActivityResult(requestCode, resultCode, data)
                if (resultCode == Activity.RESULT_OK) {
                    dialogBoxForRefundCommandeer()
                }
            }
            49374 -> {
                val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                if (result != null) {
                    if (result.contents == null) {
                        showToast("Result Not Found")
                    } else {
                        bolt11fromqr = result.contents
                        decodeInvoice(bolt11fromqr)
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    private fun getDateInCorrectFormat(year: Int, monthOfYear: Int, dayOfMonth: Int): String {
        val date: String
        val formatedMonth: String = if (monthOfYear < 9) {
            "0" + (monthOfYear + 1)
        } else {
            (monthOfYear + 1).toString()
        }
        val formatedDay = if (dayOfMonth < 10) {
            "0$dayOfMonth"
        } else {
            dayOfMonth.toString()
        }
        date = "$formatedMonth-$formatedDay-$year"
        return date
    }

    private fun decodeInvoice(bolt11: String) {
        lightningService.decodeInvoice(bolt11).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    try {
                        showToast(it.data.toString())
                        showConfirmPayDialog(bolt11, it.data?.msatoshi ?: 0.0)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                Status.ERROR -> {
                    if (it.message == "2fa") {
                        Auth2FaFragment().show(childFragmentManager, null)
                    } else {
                        showToast(it.message)
                    }
                }
                Status.LOADING -> {
                    progressDialog.show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showConfirmPayDialog(bolt11value: String, msatoshi: Double) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialoglayoutrefundcommandeerstep2)
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        dialog.setCancelable(false)
        MSATOSHI = msatoshi
        val btc = mSatoshoToBtc(java.lang.Double.valueOf(msatoshi))
        val priceInBTC = GlobalState.getInstance().channel_btcResponseData.price
        var usd = priceInBTC * btc
        AMOUNT_USD = usd
        AMOUNT_BTC = btc
        CONVERSION_RATE = AMOUNT_USD / AMOUNT_BTC
        usd = MerchantBaseFragment.round(usd, 2)
        val mst = usd.toString()
        val bolt11: TextView = dialog.findViewById(R.id.bolt11valtxt)
        val label: TextView = dialog.findViewById(R.id.labelvaltxt)
        val amount: EditText = dialog.findViewById(R.id.amountval)
        amount.setText(mst)
        amount.inputType = InputType.TYPE_NULL
        val ivBack: ImageView =
            dialog.findViewById(R.id.iv_back_invoice)
        val excecute: Button = dialog.findViewById(R.id.btn_next)
        bolt11.text = bolt11value
        label.text = "outgoing$unixTimeStamp"
        if (msatoshi == 0.0) {
            excecute.visibility = View.INVISIBLE
        }
        ivBack.setOnClickListener { dialog.dismiss() }
        excecute.setOnClickListener {
            val bolt11val = bolt11.text.toString()
            val labelval = label.text.toString()
            val amountval = amount.text.toString()
            if (bolt11val.isEmpty()) {
                showToast("Bolt11 " + getString(R.string.empty))
                return@setOnClickListener
            }
            if (labelval.isEmpty()) {
                showToast("Label " + getString(R.string.empty))
                return@setOnClickListener
            }
            executeCommandeerRefundApi(bolt11val, labelval, amountval)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun executeCommandeerRefundApi(
        bolt11value: String,
        label: String,
        usd: String
    ) {
        var priceInBTC = 1 / GlobalState.getInstance().channel_btcResponseData.price
        priceInBTC *= usd.toDouble()
        var amountInMsatoshi = priceInBTC * AppConstants.btcToSathosi
        amountInMsatoshi *= AppConstants.satoshiToMSathosi
        val formatter: NumberFormat = DecimalFormat("#0")
        val rMSatoshi = formatter.format(amountInMsatoshi)
        lightningService.payRequestToOther(bolt11value, rMSatoshi, label)
            .observe(viewLifecycleOwner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        progressDialog.dismiss()
                        if (it.data?.status == "complete") {
                            saveGetRefundTransactionInLog(it.data, label)
                            showPayCompleteDialog(it.data)
                        } else {
                            val pay = Pay()
                            pay.status = "Not complete"
                            showPayCompleteDialog(pay)
                        }
                    }
                    Status.ERROR -> {
                        progressDialog.dismiss()
                        if (it.message == "2fa") {
                            Auth2FaFragment().show(childFragmentManager, null)
                        } else {
                            showToast(it.message)
                        }
                    }
                    Status.LOADING -> {
                        progressDialog.show()
                    }
                }
            }
    }

    private fun saveGetRefundTransactionInLog(pay: Pay, label: String) {
        val precision = DecimalFormat("0.00")
        val status = pay.status
        val transactionAmountbtc = excatFigure(round(AMOUNT_BTC, 9))
        val transactionAmountusd = precision.format(AMOUNT_USD)
        val conversionRate = precision.format(CONVERSION_RATE)
        val msatoshi = excatFigure(MSATOSHI)
        val paymentPreimage = pay.payment_preimage
        val paymentHash = pay.payment_hash
        val destination = pay.destination
        val merchantId = GlobalState.getInstance().merchant_id
        val transactionDescription1 = ""
        add_alpha_transaction(
            label,
            status,
            transactionAmountbtc,
            transactionAmountusd,
            conversionRate,
            msatoshi,
            paymentPreimage,
            paymentHash,
            destination,
            merchantId,
            transactionDescription1
        )
    }

    fun add_alpha_transaction(
        transaction_label: String?,
        status: String?,
        transaction_amountBTC: String?,
        transaction_amountUSD: String?,
        conversion_rate: String?,
        msatoshi: String?,
        payment_preimage: String?,
        payment_hash: String?,
        destination: String?,
        merchant_id: String?,
        transaction_description: String?
    ) {
        val call: Call<TransactionResp> = ApiClient.getRetrofit().create(Webservice::class.java)
            .add_alpha_transction(
                transaction_label,
                status,
                transaction_amountBTC,
                transaction_amountUSD,
                payment_preimage,
                payment_hash,
                conversion_rate,
                msatoshi,
                destination,
                merchant_id,
                transaction_description
            )
        call.enqueue(object : Callback<TransactionResp?> {
            override fun onResponse(
                call: Call<TransactionResp?>,
                response: Response<TransactionResp?>
            ) {
                if (response.body() != null) {
                    val resp = response.body()
                    if (resp?.message != "successfully done" && resp?.transactionInfo == null) {
                        showToast("Not Done!!")
                    }
                    Log.e("Test", "Test")
                }
                Log.e("AddTransactionLog", response.message())
            }

            override fun onFailure(call: Call<TransactionResp?>, t: Throwable) {
                Log.e("AddTransactionLog", t.message.toString())
            }
        })
    }

    private fun listenToFcmBroadcast() {
        fcmBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    if (intent.extras != null) {
                        val notificationModel = Gson().fromJson(
                            intent.getStringExtra(AppConstants.PAYMENT_INVOICE),
                            FirebaseNotificationModel::class.java
                        )
                        Log.d(TAG, notificationModel.invoice_label)
                        fcmReceived()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        requireContext().registerReceiver(
            fcmBroadcastReceiver,
            IntentFilter(AppConstants.PAYMENT_RECEIVED_NOTIFICATION)
        )
    }

    private fun fcmReceived() {
        distributeGetPaidDialog?.dismiss()
        confirmPayment()
        if (fcmBroadcastReceiver != null) {
            requireContext().unregisterReceiver(fcmBroadcastReceiver)
        }

    }

    private fun confirmPayment() {
        invoiceLabel?.let { it ->
            lightningService.confirmPayment(it).observe(viewLifecycleOwner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        confirmingProgressDialog.dismiss()
                        if (it.data != null) {
                            if (it.data.status == "paid") {
                                dialogBoxForConfirmPaymentInvoice(it.data)
                                confirmingProgressDialog.dismiss()
                            } else {
                                AlertDialog.Builder(requireContext())
                                    .setMessage("Payment Not Received")
                                    .setPositiveButton("Retry", null)
                                    .show()
                            }
                        } else {
                            confirmingProgressDialog.dismiss()
                            AlertDialog.Builder(requireContext())
                                .setMessage("Payment Not Received")
                                .setPositiveButton("Retry", null)
                                .show()
                        }
                    }
                    Status.ERROR -> {
                        if (it.message == "2fa") {
                            Auth2FaFragment().show(childFragmentManager, null)
                        } else {
                            showToast(it.message)
                        }
                        showToast(it.message.toString())
                    }
                    Status.LOADING -> {
                        confirmingProgressDialog.show()
                    }
                }
            }
        }

    }


    override fun onStop() {
        super.onStop()
        if (fcmBroadcastReceiver != null) {
            requireContext().unregisterReceiver(fcmBroadcastReceiver)
        }
    }

}