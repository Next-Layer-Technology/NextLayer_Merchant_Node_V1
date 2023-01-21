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
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.R
import com.sis.clightapp.adapter.AdminReceivableListAdapter
import com.sis.clightapp.adapter.AdminSendablesListAdapter
import com.sis.clightapp.fragments.merchant.MerchantBaseFragment
import com.sis.clightapp.fragments.printing.PrintDialogFragment
import com.sis.clightapp.model.GsonModel.*
import com.sis.clightapp.model.REST.TransactionResp
import com.sis.clightapp.services.BTCService
import com.sis.clightapp.services.LightningService
import com.sis.clightapp.session.MyLogOutService
import com.sis.clightapp.util.*
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
    private val webservice: Webservice by inject()

    private var INTENT_AUTHENTICATE = 1234
    private var setwidht = 0
    var setheight = 0
    var rateInBtc = 0.0
    private lateinit var distributebutton: Button
    private lateinit var commandeerbutton: Button
    private lateinit var qRCodeImage: ImageView
    private lateinit var receiveableslistview: ListView
    private lateinit var sendeableslistview: ListView
    private var adminReceiveablesListAdapter: AdminReceivableListAdapter? = null
    private var adminSendablesListAdapter: AdminSendablesListAdapter? = null
    private var fcmBroadcastReceiver: BroadcastReceiver? = null

    private lateinit var progressDialog: ProgressDialog

    private var currentTransactionLabel = ""
    private var bolt11fromqr = ""
    private var distributeDescription = ""
    private var gdaxUrl = "ws://73.36.65.41:8095/SendCommands"
    private lateinit var fromDaterReceivables: EditText
    private lateinit var toDateReceivables: EditText
    private lateinit var fromDateSendables: EditText
    private lateinit var toDateSendables: EditText
    private lateinit var picker: DatePickerDialog
    var isInApp = true
    lateinit var setTextWithSpan: TextView
    private var AMOUNT_BTC = 0.0
    private var AMOUNT_USD = 0.0
    private var CONVERSION_RATE = 0.0
    private var MSATOSHI = 0.0
    var getPaidLABEL = ""
    private var distributeGetPaidDialog: Dialog? = null

    private var receivables = arrayListOf<Invoice>()
    private var sendables = arrayListOf<Refund>()

    private var invoiceLabel: String? = null
    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            val originalIntent = result.originalIntent
            if (originalIntent == null) {
                Toast.makeText(
                    requireContext(),
                    "Cancelled",
                    Toast.LENGTH_LONG
                ).show()
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Log.d(
                    "MainActivity",
                    "Cancelled scan due to missing camera permission"
                )
                Toast.makeText(
                    requireContext(),
                    "Cancelled due to missing camera permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            if (result.contents != null) {
                if (result.contents == null) {
                    showToast("Result Not Found")
                } else {
                    bolt11fromqr = result.contents
                    decodeInvoice(bolt11fromqr)
                }
            }
        }
    }

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
        btcService.currentBtc.observe(viewLifecycleOwner) {
            it.data?.let {
                this.rateInBtc = it.price
            }
        }
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
                    progressDialog.dismiss()
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
                    progressDialog.dismiss()
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
            AdminReceivableListAdapter(requireActivity(), this.receivables)
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
            window?.setLayout((width *.9).toInt(), (height *.9).toInt())
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
                if (rateInBtc == 0.0) {
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
                var priceInBTC = 1 / rateInBtc
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
                                        etDescription.hideKeyboard()
                                    } catch (e: WriterException) {
                                        e.printStackTrace()
                                    }
                                }
                            }

                            Status.ERROR -> {
                                progressDialog.dismiss()
                                showToast(it.message)
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
        dialog.window?.setLayout((width *.9).toInt(), (height *.9).toInt())
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
            amount.visibility = View.VISIBLE
            paymentPreImage.visibility = View.VISIBLE
            paidAt.visibility = View.VISIBLE
            purchasedItems.visibility = View.VISIBLE
            printInvoice.visibility = View.VISIBLE
            val amounttempusd = round(btcService.btcToUsd(mSatoshoToBtc(invoice.msatoshi)), 2)
            val precision = DecimalFormat("0.00")
            amount.text = StringBuilder()
                .append(excatFigure(round(mSatoshoToBtc(invoice.msatoshi), 9)))
                .append("BTC\n$").append(precision.format(round(amounttempusd, 2)))
                .append("USD").toString()
            paymentPreImage.setImageBitmap(getBitMapFromHex(invoice.payment_preimage, 300, 300))
            paidAt.text =
                getDateFromUTCTimestamp(invoice.paid_at, AppConstants.OUTPUT_DATE_FORMATE)
            purchasedItems.text = distributeDescription
        } else {
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
                            btcService.btcToUsd(mSatoshoToBtc(invoice.msatoshi)),
                            2
                        )
                    )
                ).append("USD").toString()
            paidAt.text =
                getDateFromUTCTimestamp(invoice.paid_at, AppConstants.OUTPUT_DATE_FORMATE)
            paymentPreImage.setImageBitmap(getBitMapFromHex(invoice.payment_preimage, 300, 300))
            purchasedItems.text = "N/A"
        }
        printInvoice.setOnClickListener {
            loadObservers()
            if (invoice.status == "paid") {
                PrintDialogFragment(
                    invoice,
                    items = GlobalState.getInstance().selectedItems.toList()
                ).show(childFragmentManager, null)
            }
        }
        ivBack.setOnClickListener {
            loadObservers()
            dialog.dismiss()
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showPayCompleteDialog(pay: Pay, bolt11value: String) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialoglayoutrefundcommandeerlaststepconfirmedpay)
        val ivBack: ImageView =
            dialog.findViewById(R.id.iv_back_invoice)
        val textView: TextView = dialog.findViewById(R.id.textView2)
        val ok: Button = dialog.findViewById(R.id.btn_ok)
        dialog.window?.setLayout((width  *.9).toInt(), (height  *.9).toInt())
        dialog.setCancelable(false)
        textView.text = "Payment Status:" + pay.status
        if (pay.status == "complete") {
            ok.text = "Print"
        }
        pay.bolt11 = bolt11value
        ok.setOnClickListener {
            loadObservers()
            if (pay.status == "complete") {
                PrintDialogFragment(
                    payment = pay,
                    items = GlobalState.getInstance().selectedItems.toList()
                ).show(childFragmentManager, null)
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
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialoglayoutrefundcommandeer)
        dialog.window?.setLayout((width  *.9).toInt(), (height *.5).toInt())
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog.setCancelable(false)
        val etBolt11 = dialog.findViewById<EditText>(R.id.bolt11val)
        val ivBack = dialog.findViewById<ImageView>(R.id.iv_back_invoice)
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_title)
        tvTitle.text = "COMMANDEER"
        val btnNext = dialog.findViewById<Button>(R.id.btn_next)
        val btnscanQr = dialog.findViewById<Button>(R.id.btn_scanQR)
        ivBack.setOnClickListener { dialog.dismiss() }
        btnNext.setOnClickListener {
            val bolt11value = etBolt11.text.toString()
            if (bolt11value.isEmpty()) {
                showToast("Bolt11 " + getString(R.string.empty))
            } else {
                dialog.dismiss()
                bolt11fromqr = bolt11value
                decodeInvoice(bolt11value)
            }
        }
        btnscanQr.setOnClickListener {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt(resources.getString(R.string.scanqrforbolt11))
            options.setCameraId(0) // Use a specific camera of the device
            options.setBeepEnabled(false)
            options.setBarcodeImageEnabled(true)
            barcodeLauncher.launch(options)
        }
        dialog.show()
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
        }
    }

    private fun getDateInCorrectFormat(year: Int, monthOfYear: Int, dayOfMonth: Int): String {
        val date: String
        val formattedMonth: String = if (monthOfYear < 9) {
            "0" + (monthOfYear + 1)
        } else {
            (monthOfYear + 1).toString()
        }
        val formatedDay = if (dayOfMonth < 10) {
            "0$dayOfMonth"
        } else {
            dayOfMonth.toString()
        }
        date = "$formattedMonth-$formatedDay-$year"
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
                    progressDialog.dismiss()
                    showToast(it.message)
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
        dialog.window?.setLayout((width * .9).toInt(), (height * .9).toInt())
        dialog.setCancelable(false)
        MSATOSHI = msatoshi
        val btc = mSatoshoToBtc(java.lang.Double.valueOf(msatoshi))
        val priceInBTC = rateInBtc
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
            dialog.dismiss()
            executeCommandeerRefundApi(bolt11val, labelval, amountval)
        }
        dialog.show()
    }

    private fun executeCommandeerRefundApi(
        bolt11value: String,
        label: String,
        usd: String
    ) {
        var priceInBTC = 1 / rateInBtc
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
                            showPayCompleteDialog(it.data, bolt11value)
                        } else {
                            val pay = Pay()
                            pay.status = "Not complete"
                            showPayCompleteDialog(pay, bolt11value)
                        }
                    }

                    Status.ERROR -> {
                        progressDialog.dismiss()
                        showToast(it.message)
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
        addAlphaTransaction(
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

    private fun addAlphaTransaction(
        transactionLabel: String?,
        status: String?,
        transactionAmountbtc: String?,
        transactionAmountusd: String?,
        conversionRate: String?,
        msatoshi: String?,
        paymentPreimage: String?,
        paymentHash: String?,
        destination: String?,
        merchantId: String?,
        transactionDescription: String?
    ) {
        val call: Call<TransactionResp> = webservice
            .add_alpha_transction(
                transactionLabel,
                status,
                transactionAmountbtc,
                transactionAmountusd,
                paymentPreimage,
                paymentHash,
                conversionRate,
                msatoshi,
                destination,
                merchantId,
                transactionDescription
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
            lightningService.confirmPayment(it).observe(viewLifecycleOwner) { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        confirmingProgressDialog.dismiss()
                        if (response.data != null) {
                            if (response.data.status == "paid") {
                                dialogBoxForConfirmPaymentInvoice(response.data)
                                response.data.let {
                                    addAlphaTransaction(
                                        invoiceLabel,
                                        it.status,
                                        String.format("%.9f", satoshiToBtc(it.msatoshi)),
                                        String.format("%.9f", btcService.btcToUsd(satoshiToBtc(it.msatoshi))),
                                        CONVERSION_RATE.toString(),
                                        excatFigure(MSATOSHI),
                                        it.payment_preimage,
                                        it.payment_hash,
                                        it.description,
                                        GlobalState.getInstance().merchant_id,
                                        it.description
                                    )
                                }
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
                        progressDialog.dismiss()
                        showToast(response.message)
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