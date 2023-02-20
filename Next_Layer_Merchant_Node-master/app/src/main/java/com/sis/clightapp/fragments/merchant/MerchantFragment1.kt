package com.sis.clightapp.fragments.merchant

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.opencsv.CSVWriter
import com.sis.clightapp.EmailSdk.GMailSender
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.R
import com.sis.clightapp.activity.MerchantMainActivity
import com.sis.clightapp.adapter.MerchantRefundsListAdapter
import com.sis.clightapp.adapter.MerchantSalesListAdapter
import com.sis.clightapp.fragments.admin.AdminBaseFragment
import com.sis.clightapp.fragments.printing.PrintDialogFragment
import com.sis.clightapp.model.GsonModel.FirebaseNotificationModel
import com.sis.clightapp.model.GsonModel.Invoice
import com.sis.clightapp.model.GsonModel.Merchant.MerchantLoginResp
import com.sis.clightapp.model.GsonModel.Pay
import com.sis.clightapp.model.GsonModel.Refund
import com.sis.clightapp.model.REST.TransactionResp
import com.sis.clightapp.model.Tax
import com.sis.clightapp.services.BTCService
import com.sis.clightapp.services.LightningService
import com.sis.clightapp.services.SessionService
import com.sis.clightapp.session.MyLogOutService
import com.sis.clightapp.util.*
import org.json.JSONException
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MerchantFragment1 : MerchantBaseFragment() {
    private val lightningService: LightningService by inject()
    private val btcService: BTCService by inject()
    private val sessionService: SessionService by inject()
    private val webservice: Webservice by inject()
    private lateinit var preferences: SharedPreferences
    private var edit: SharedPreferences.Editor? = null
    private var INTENT_AUTHENTICATE = 1234

    private lateinit var progressDialog: ProgressDialog
    private lateinit var getpaidbutton: Button
    private lateinit var refundbutton: Button
    private lateinit var saleslistview: ListView
    private lateinit var refundslistview: ListView
    private lateinit var merchantSalesListAdapter: MerchantSalesListAdapter
    private lateinit var merchantRefundsListAdapter: MerchantRefundsListAdapter
    private val TAG = "CLightinApp"
    private lateinit var confirmPaymentDialog: Dialog
    private lateinit var commandeerRefundDialog: Dialog
    private lateinit var distributeGetPaidDialog: Dialog

    //    private lateinit var confirpaymentbtn: Button
    private lateinit var qRCodeImage: ImageView
    private var currentTransactionLabel: String? = null
    private var bolt11fromqr = ""
    private var getPaidDescrition = ""
    private var fcmBroadcastReceiver: BroadcastReceiver? = null


    var isInAppMerchant1 = true

    private var invoices = arrayListOf<Invoice>()
    private var refunds = arrayListOf<Refund>()

    //Date Filter
    private lateinit var fromDateSale: EditText
    private lateinit var toDateSale: EditText
    private lateinit var fromDateRefund: EditText
    private lateinit var toDateRefund: EditText
    private lateinit var picker: DatePickerDialog

    //Email Sale
    private var gMailSender: GMailSender? = null
    private var mSaleDataSource: ArrayList<Invoice> = arrayListOf()
    private var mRefundDataSource: ArrayList<Refund> = arrayListOf()
    private lateinit var setTextWithSpan: TextView
    private var paidLabel = ""
    private var refundLabel = ""
    private var amountBtc = 0.0
    private var amountUsd = 0.0
    private var conversionRate = 0.0
    private var msatoshi = 0.0


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

    override fun onStart() {
        super.onStart()
        if (isInAppMerchant1) {
            loadObservers()
        }
    }

    private fun loadObservers() {
        lightningService.refundList().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    it.data?.refundArrayList?.let { data ->
                        setRefundsAdapter(data)
                        this.refunds = data
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
                        this.invoices = data
                        setSalesAdapter(data)
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_merchant1, container, false)
        setTextWithSpan = view.findViewById(R.id.poweredbyimage)
        val boldStyle = StyleSpan(Typeface.BOLD)
        setTextWithSpan(
            setTextWithSpan,
            getString(R.string.welcome_text),
            getString(R.string.welcome_text_bold),
            boldStyle
        )
        fromDateSale = view.findViewById(R.id.et_from_date_sale)
        toDateSale = view.findViewById(R.id.et_to_date_sale)
        fromDateRefund = view.findViewById(R.id.et_from_date_refund)
        toDateRefund = view.findViewById(R.id.et_to_date_refund)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        edit = preferences.edit()
        progressDialog = ProgressDialog(context)
        sharedPreferences = CustomSharedPreferences()
        findMerchant(
            CustomSharedPreferences().getvalueofMerchantname("merchant_name", context),
            CustomSharedPreferences().getvalueofMerchantpassword("merchant_pass", context)
        )
        getpaidbutton = view.findViewById(R.id.getpaidbutton)
        getpaidbutton.setOnClickListener { //TODO:what ever on getPAid
            dialogBoxForGetPaidDistribute()
        }
        refundbutton = view.findViewById(R.id.refundbutton)
        refundbutton.setOnClickListener { view1: View? ->
            //TODO:what ever on Refunds
            isInAppMerchant1 = false
            val km =
                requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardSecure) {
                val authIntent = km.createConfirmDeviceCredentialIntent("Authorize Payment", "")
                startActivityForResult(authIntent, INTENT_AUTHENTICATE)
            } else {
                dialogBoxForRefundCommandeer()
            }
        }
        saleslistview = view.findViewById(R.id.salesListview)
        refundslistview = view.findViewById(R.id.refendListview)

        fromDateSale.inputType = InputType.TYPE_NULL
        toDateSale.inputType = InputType.TYPE_NULL
        fromDateRefund.inputType = InputType.TYPE_NULL
        toDateRefund.inputType = InputType.TYPE_NULL
        fromDateSale.setOnClickListener {
            val cldr = Calendar.getInstance()
            val day = cldr[Calendar.DAY_OF_MONTH]
            val month = cldr[Calendar.MONTH]
            val year = cldr[Calendar.YEAR]
            // date picker dialog
            picker = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val date = getDateInCorrectFormat(year, monthOfYear, dayOfMonth)
                    fromDateSale.setText(date)
                    fromDateRefund.setText("")
                    toDateRefund.setText("")
                    toDateSale.setText("")
                    setAdapterFromDateSale(date)
                }, year, month, day
            )
            picker.datePicker.maxDate =
                System.currentTimeMillis() // TODO: used to hide future date,month and year
            picker.show()
        }
        toDateSale.setOnClickListener {
            val cldr = Calendar.getInstance()
            val day = cldr[Calendar.DAY_OF_MONTH]
            val month = cldr[Calendar.MONTH]
            val year = cldr[Calendar.YEAR]
            // date picker dialog
            picker = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val date = getDateInCorrectFormat(year, monthOfYear, dayOfMonth)
                    toDateSale.setText(date)
                    fromDateSale.setText("")
                    fromDateRefund.setText("")
                    toDateRefund.setText("")
                    setAdapterToDateSale(date)
                }, year, month, day
            )
            picker.datePicker.maxDate =
                System.currentTimeMillis() // TODO: used to hide future date,month and year
            picker.show()
        }
        fromDateRefund.setOnClickListener {
            val cldr = Calendar.getInstance()
            val day = cldr[Calendar.DAY_OF_MONTH]
            val month = cldr[Calendar.MONTH]
            val year = cldr[Calendar.YEAR]
            // date picker dialog
            picker = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val date = getDateInCorrectFormat(year, monthOfYear, dayOfMonth)
                    fromDateRefund.setText(date)
                    toDateRefund.setText("")
                    fromDateSale.setText("")
                    toDateSale.setText("")
                    setAdapterFromDateRefund(date)
                }, year, month, day
            )
            picker.datePicker.maxDate =
                System.currentTimeMillis() // TODO: used to hide future date,month and year
            picker.show()
        }
        toDateRefund.setOnClickListener {
            val cldr = Calendar.getInstance()
            val day = cldr[Calendar.DAY_OF_MONTH]
            val month = cldr[Calendar.MONTH]
            val year = cldr[Calendar.YEAR]
            // date picker dialog
            picker = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val date = getDateInCorrectFormat(year, monthOfYear, dayOfMonth)
                    toDateRefund.setText(date)
                    fromDateRefund.setText("")
                    fromDateSale.setText("")
                    toDateSale.setText("")
                    setAdapterToDateRefund(date)
                }, year, month, day
            )
            picker.datePicker.maxDate =
                System.currentTimeMillis() // TODO: used to hide future date,month and year
            picker.show()
        }
        return view
    }

    private fun setAdapterToDateRefund(date: String) {
        val fromDateRefundList = ArrayList<Refund>()
        for (refund in refunds) {
            if (refund.status == "complete") {
                val sourceSplit =
                    date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val month = sourceSplit[0].toInt()
                val day = sourceSplit[1].toInt()
                val year = sourceSplit[2].toInt()
                val cal = Calendar.getInstance(TimeZone.getTimeZone("CST"))
                cal[year, month - 1] = day
                val date = cal.time
                val paidTime = refund.created_at * 1000
                val date2 = Date(paidTime)
                if (date2.before(date)) {
                    fromDateRefundList.add(refund)
                }
            }
        }
        merchantRefundsListAdapter =
            MerchantRefundsListAdapter(requireContext(), fromDateRefundList)
        refundslistview.adapter = merchantRefundsListAdapter
    }

    private fun setAdapterFromDateRefund(datex: String) {
        val fromDateRefundList = ArrayList<Refund>()
        for (refund in refunds) {
            if (refund.status == "complete") {
                val sourceSplit =
                    datex.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val month = sourceSplit[0].toInt()
                val day = sourceSplit[1].toInt()
                val year = sourceSplit[2].toInt()
                //  GregorianCalendar calendar = new GregorianCalendar();
                val cal = Calendar.getInstance(TimeZone.getTimeZone("CST"))
                cal[year, month - 1] = day
                val date = cal.time
                val paidTime = refund.created_at * 1000
                val date2 = Date(paidTime)
                if (date2.after(date) || date.day == date2.day) {
                    fromDateRefundList.add(refund)
                }
            }
        }
        merchantRefundsListAdapter =
            MerchantRefundsListAdapter(requireContext(), fromDateRefundList)
        refundslistview.adapter = merchantRefundsListAdapter
    }

    private fun setAdapterToDateSale(toString: String) {
        val fromDateSaleList = arrayListOf<Invoice>()
        for (sale in invoices) {
            if (sale.payment_preimage != null) {
                val sourceSplit =
                    toString.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val month = sourceSplit[0].toInt()
                val day = sourceSplit[1].toInt()
                val year = sourceSplit[2].toInt()
                //GregorianCalendar calendar = new GregorianCalendar();
                val cal = Calendar.getInstance(TimeZone.getTimeZone("CST"))
                cal[year, month - 1] = day
                val date = cal.time
                date.time
                val paidTime = sale.paid_at * 1000
                val date2 = Date(paidTime)
                if (date2.before(date)) {
                    fromDateSaleList.add(sale)
                }
            }
        }
        merchantSalesListAdapter = MerchantSalesListAdapter(requireContext(), fromDateSaleList)
        saleslistview.adapter = merchantSalesListAdapter
    }

    private fun setAdapterFromDateSale(date: String) {
        val fromDateSaleList = arrayListOf<Invoice>()
        for (sale in invoices) {
            if (sale.payment_preimage != null) {
                val sourceSplit =
                    date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val month = sourceSplit[0].toInt()
                val day = sourceSplit[1].toInt()
                val year = sourceSplit[2].toInt()
                val cal = Calendar.getInstance(TimeZone.getTimeZone("CST"))
                cal[year, month - 1] = day
                val date = cal.time
                val paidTime = sale.paid_at * 1000
                val date2 = Date(paidTime)
                if (date2.after(date) || date.day == date2.day) {
                    fromDateSaleList.add(sale)
                }
            }
        }
        merchantSalesListAdapter = MerchantSalesListAdapter(requireContext(), fromDateSaleList)
        saleslistview.adapter = merchantSalesListAdapter
    }

    private fun setSalesAdapter(data: ArrayList<Invoice>) {
        val todaySaleList = ArrayList<Invoice>()
        val totalPaidSaleList = ArrayList<Invoice>()
        val totalUnPaidSaleList = ArrayList<Invoice>()
        for (sale in data) {
            if (sale.label != null) {
                totalPaidSaleList.add(sale)
                val curentTime = Date().time
                val paidTime = sale.paid_at * 1000
                val currentDate = Date(curentTime)
                val paidDate = Date(paidTime)
                val cal1 = Calendar.getInstance()
                val cal2 = Calendar.getInstance()
                cal1.time = currentDate
                cal2.time = paidDate
                val sameDay = cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                        cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
                if (sameDay) {
                    todaySaleList.add(sale)
                }
            } else {
                totalUnPaidSaleList.add(sale)
            }
        }
        mSaleDataSource = totalPaidSaleList
        merchantSalesListAdapter = MerchantSalesListAdapter(requireContext(), todaySaleList)
        saleslistview.adapter = merchantSalesListAdapter
    }

    private fun setRefundsAdapter(data: ArrayList<Refund>) {
        val mTodayRefundList = ArrayList<Refund>()
        val mTotalCompleteRefundList = ArrayList<Refund>()
        val mTotalUnCompleteList = ArrayList<Refund>()
        for (refund in data) {
            if (refund.status != null) {
                if (refund.status == "complete") {
                    mTotalCompleteRefundList.add(refund)
                    val currentTime = Date().time
                    val refundtime = refund.created_at * 1000
                    val currentDate = Date(currentTime)
                    val refundDate = Date(refundtime)
                    val cal1 = Calendar.getInstance()
                    val cal2 = Calendar.getInstance()
                    cal1.time = currentDate
                    cal2.time = refundDate
                    val sameDay = cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                            cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
                    if (sameDay) {
                        mTodayRefundList.add(refund)
                    }
                } else {
                    mTotalUnCompleteList.add(refund)
                }
            } else {
                mTotalUnCompleteList.add(refund)
            }
        }
        mRefundDataSource = mTotalCompleteRefundList
        merchantRefundsListAdapter = MerchantRefundsListAdapter(requireContext(), mTodayRefundList)
        refundslistview.adapter = merchantRefundsListAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun dialogBoxForGetPaidDistribute() {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        distributeGetPaidDialog = Dialog(requireContext())
        distributeGetPaidDialog.setContentView(R.layout.dialoglayoutgetpaiddistribute)
        distributeGetPaidDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        distributeGetPaidDialog.window?.setLayout((width * .9).toInt(), (height * .9).toInt())
        distributeGetPaidDialog.setCancelable(false)
        val title = distributeGetPaidDialog.findViewById<View>(R.id.tv_title) as TextView
        title.text = "Get Paid"
//        confirpaymentbtn = distributeGetPaidDialog.findViewById(R.id.confirpaymentbtn)
        val etMsatoshi = distributeGetPaidDialog.findViewById<EditText>(R.id.et_msatoshi)
        val etLabel = distributeGetPaidDialog.findViewById<EditText>(R.id.et_lable)
        etLabel.inputType = InputType.TYPE_NULL
        etLabel.setText("sale$unixTimeStamp")
        paidLabel = etLabel.text.toString()
        val etDescription = distributeGetPaidDialog.findViewById<EditText>(R.id.et_description)
        val ivBack = distributeGetPaidDialog.findViewById<ImageView>(R.id.iv_back_invoice)
        qRCodeImage = distributeGetPaidDialog.findViewById(R.id.imgQR)
        val btnCreatInvoice = distributeGetPaidDialog.findViewById<Button>(R.id.btn_createinvoice)
        qRCodeImage.visibility = View.GONE
        ivBack.setOnClickListener { distributeGetPaidDialog.dismiss() }
        btnCreatInvoice.setOnClickListener(View.OnClickListener {
            val mSatoshi = etMsatoshi.text.toString()
            val label = etLabel.text.toString()
            val description = etDescription.text.toString()
            if (mSatoshi.isEmpty()) {
                showToast("Amount" + getString(R.string.empty))
                return@OnClickListener
            }
            if (label.isEmpty()) {
                showToast("Label" + getString(R.string.empty))
                return@OnClickListener
            }
            if (description.isEmpty()) {
                showToast("Description" + getString(R.string.empty))
                return@OnClickListener
            }
            if (btcService.btcPrice == 0.0) {
                showToast("Btc rate not available" + getString(R.string.empty))
                return@OnClickListener
            }
            //      progressBar.setVisibility(View.VISIBLE);
            currentTransactionLabel = label
            amountUsd = mSatoshi.toDouble()
            var priceInBTC = 1 / btcService.btcPrice
            priceInBTC *= mSatoshi.toDouble()
            amountBtc = priceInBTC
            var amountInMsatoshi = priceInBTC * AppConstants.btcToSathosi
            msatoshi = amountInMsatoshi
            amountInMsatoshi *= AppConstants.satoshiToMSathosi
            conversionRate = amountUsd / amountBtc
            val formatter: NumberFormat = DecimalFormat("#0")
            val rMSatoshi = formatter.format(amountInMsatoshi)
            getPaidDescrition = description
            lightningService.createInvoice(rMSatoshi, label, description)
                .observe(viewLifecycleOwner) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            progressDialog.dismiss()
                            showToast(it.data?.bolt11)
                            if (it.data?.bolt11 != null) {
                                currentTransactionLabel = label
                                val temHax = it.data.bolt11
                                val multiFormatWriter = MultiFormatWriter()
                                try {
                                    Log.d(QR_CODE, temHax)
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
//                                    confirpaymentbtn.visibility = View.VISIBLE
                                    etDescription.hideKeyboard()
                                    listenToFcmBroadcast()
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

        })
//        confirpaymentbtn.setOnClickListener {
//            confirmPayment()
//        }
        distributeGetPaidDialog.show()
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
        distributeGetPaidDialog.dismiss()
        confirmPayment()
//        if (fcmBroadcastReceiver != null) {
//            requireContext().unregisterReceiver(fcmBroadcastReceiver)
//        }
    }

    private fun confirmPayment() {
        currentTransactionLabel?.let { it ->
            lightningService.confirmPayment(it).observe(viewLifecycleOwner) { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        progressDialog.dismiss()
                        if (response.data != null) {
                            if (response.data.status == "paid") {
                                dialogBoxForConfirmPaymentInvoice(response.data)
                                response.data.let {
                                    addMerchantTransaction(
                                        currentTransactionLabel,
                                        it.status,
                                        String.format("%.9f", satoshiToBtc(it.msatoshi)),
                                        String.format("%.9f", amountUsd),
                                        conversionRate.toString(),
                                        excatFigure(msatoshi),
                                        it.payment_preimage,
                                        it.payment_hash,
                                        it.description,
                                        sessionService.getMerchantData()?.merchant_id,
                                        it.description
                                    )
                                }
                                progressDialog.dismiss()
                            } else {
                                AlertDialog.Builder(requireContext())
                                    .setMessage("Payment Not Received")
                                    .setPositiveButton("Retry", null)
                                    .show()
                            }
                        } else {
                            progressDialog.dismiss()
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
                        progressDialog.show()
                    }
                }
            }
        }

    }


    @SuppressLint("SetTextI18n")
    private fun dialogBoxForConfirmPaymentInvoice(invoice: Invoice?) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        confirmPaymentDialog = Dialog(requireContext())
        confirmPaymentDialog.setContentView(R.layout.customlayoutofconfirmpaymentdialogformerchantadmin)
        confirmPaymentDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        confirmPaymentDialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        confirmPaymentDialog.setCancelable(false)
        val ivBack = confirmPaymentDialog.findViewById<ImageView>(R.id.iv_back_invoice)
        val amount = confirmPaymentDialog.findViewById<TextView>(R.id.et_amount)
        val paymentPreimage = confirmPaymentDialog.findViewById<ImageView>(R.id.et_preimage)
        val paidAt = confirmPaymentDialog.findViewById<TextView>(R.id.et_paidat)
        val purchasedItems = confirmPaymentDialog.findViewById<TextView>(R.id.et_perchaseditems)
        val printInvoice = confirmPaymentDialog.findViewById<Button>(R.id.btn_printinvoice)
        amount.visibility = View.GONE
        paymentPreimage.visibility = View.GONE
        paidAt.visibility = View.GONE
        purchasedItems.visibility = View.GONE
        printInvoice.visibility = View.GONE
        if (invoice != null) {
            if (invoice.status == "paid") {
                amount.visibility = View.VISIBLE
                paymentPreimage.visibility = View.VISIBLE
                paidAt.visibility = View.VISIBLE
                purchasedItems.visibility = View.VISIBLE
                //  tax.setVisibility(View.VISIBLE);
                printInvoice.visibility = View.VISIBLE
                val usd = round(btcService.btcToUsd(mSatoshoToBtc(invoice.msatoshi)), 2)
                val precision = DecimalFormat("0.00")
                amount.text = """
                    ${excatFigure(round(mSatoshoToBtc(invoice.msatoshi), 9))}BTC
                    ${"$"}${precision.format(round(usd, 2))}USD
                    """.trimIndent()
                paymentPreimage.setImageBitmap(getBitMapImg(invoice.payment_preimage, 300, 300))
                paidAt.text =
                    getDateFromUTCTimestamp(invoice.paid_at, AppConstants.OUTPUT_DATE_FORMATE)
                purchasedItems.text = getPaidDescrition
            } else {
                amount.visibility = View.VISIBLE
                paymentPreimage.visibility = View.VISIBLE
                paidAt.visibility = View.VISIBLE
                purchasedItems.visibility = View.VISIBLE
                printInvoice.visibility = View.VISIBLE
                amount.text = "0.0"
                paidAt.text = "N/A"
                purchasedItems.text = getPaidDescrition
                paymentPreimage.setImageBitmap(getBitMapImg(invoice.payment_preimage, 300, 300))
            }
        }
        printInvoice.setOnClickListener { view: View? ->
            confirmPaymentDialog.dismiss()
            if (invoice?.status == "paid") {
                loadObservers()
                PrintDialogFragment(invoice, null, arrayListOf()) {
                    (requireActivity() as MerchantMainActivity).clearAndGoBack()
                }.show(childFragmentManager, null)
            } else {
                loadObservers()
            }
        }
        ivBack.setOnClickListener {
            loadObservers()
            confirmPaymentDialog.dismiss()
        }
        confirmPaymentDialog.show()
    }

    private fun dialogBoxForRefundCommandeer() {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        commandeerRefundDialog = Dialog(requireContext())
        commandeerRefundDialog.setContentView(R.layout.dialoglayoutrefundcommandeer)
        commandeerRefundDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        commandeerRefundDialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        commandeerRefundDialog.setCancelable(false)
        val bolt11 = commandeerRefundDialog.findViewById<EditText>(R.id.bolt11val)
        val ivBack = commandeerRefundDialog.findViewById<ImageView>(R.id.iv_back_invoice)
        val btnNext = commandeerRefundDialog.findViewById<Button>(R.id.btn_next)
        val btnscanQr = commandeerRefundDialog.findViewById<Button>(R.id.btn_scanQR)
        // progressBar = dialog.findViewById(R.id.progress_bar);
        ivBack.setOnClickListener { commandeerRefundDialog.dismiss() }
        btnNext.setOnClickListener(View.OnClickListener {
            val bolt11value = bolt11.text.toString()
            if (bolt11value.isEmpty()) {
                showToast("Bolt11 " + getString(R.string.empty))
                return@OnClickListener
            } else {
                commandeerRefundDialog.dismiss()
                bolt11fromqr = bolt11value
                decodeInvoice(bolt11value)
            }
        })
        btnscanQr.setOnClickListener {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt(resources.getString(R.string.scanqrforbolt11))
            options.setCameraId(0) // Use a specific camera of the device
            options.setBeepEnabled(false)
            options.setBarcodeImageEnabled(true)
            barcodeLauncher.launch(options)
            commandeerRefundDialog.dismiss()
        }
        commandeerRefundDialog.show()
    }

    private fun decodeInvoice(bolt11: String) {
        lightningService.decodeInvoice(bolt11).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    try {
                        dialogBoxForRefundCommandeerStep2(bolt11, it.data?.msatoshi ?: 0.0)
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
    private fun dialogBoxForRefundCommandeerStep2(bolt11value: String, msatoshi: Double) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialoglayoutrefundcommandeerstep2)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        dialog.setCancelable(false)
        this.msatoshi = msatoshi
        val btc = mSatoshoToBtc(msatoshi)
        val priceInBTC = btcService.btcPrice
        var usd = priceInBTC * btc
        amountUsd = usd
        amountBtc = btc
        conversionRate = amountUsd / amountBtc
        usd = round(usd, 2)
        val mst = usd.toString()
        val bolt11 = dialog.findViewById<TextView>(R.id.bolt11valtxt)
        val tvLabel = dialog.findViewById<TextView>(R.id.labelvaltxt)
        val tvAmount = dialog.findViewById<EditText>(R.id.amountval)
        tvAmount.setText(mst)
        tvAmount.inputType = InputType.TYPE_NULL
        val ivBack = dialog.findViewById<ImageView>(R.id.iv_back_invoice)
        val execute = dialog.findViewById<Button>(R.id.btn_next)
        bolt11.text = bolt11value
        tvLabel.text = "outgoing$unixTimeStamp"
        refundLabel = tvLabel.text.toString()
        if (msatoshi == 0.0) {
            execute.visibility = View.INVISIBLE
        }
        ivBack.setOnClickListener { dialog.dismiss() }
        execute.setOnClickListener(View.OnClickListener {
            val bolt11val = bolt11.text.toString()
            val label = tvLabel.text.toString()
            val amount = tvAmount.text.toString()
            if (bolt11val.isEmpty()) {
                showToast("Bolt11 " + getString(R.string.empty))
                return@OnClickListener
            }
            if (label.isEmpty()) {
                showToast("Label " + getString(R.string.empty))
                return@OnClickListener
            }
            executeCommandeerRefundApi(bolt11val, label, amount)
            dialog.dismiss()
        })
        dialog.show()
    }

    private fun executeCommandeerRefundApi(bolt11: String, label: String, amountusd: String) {
        var priceInBTC = 1 / btcService.btcPrice
        priceInBTC *= amountusd.toDouble()
        var amountInMsatoshi = priceInBTC * AppConstants.btcToSathosi
        amountInMsatoshi *= AppConstants.satoshiToMSathosi
        val formatter: NumberFormat = DecimalFormat("#0")
        val rMSatoshi = formatter.format(amountInMsatoshi)
        lightningService.payRequestToOther(bolt11, rMSatoshi, label)
            .observe(viewLifecycleOwner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        progressDialog.dismiss()
                        if (it.data?.status == "complete") {
                            saveGetRefundTransactionInLog(it.data, label)
                            it.data.bolt11 = bolt11
                            showConfirmDialog(it.data)
                        } else {
                            val pay = Pay()
                            pay.bolt11 = bolt11
                            pay.status = "Not complete"
                            showConfirmDialog(pay)
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

    private fun showConfirmDialog(pay: Pay) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialoglayoutrefundcommandeerlaststepconfirmedpay)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val ivBack = dialog.findViewById<ImageView>(R.id.iv_back_invoice)
        val textView = dialog.findViewById<TextView>(R.id.textView2)
        val ok = dialog.findViewById<Button>(R.id.btn_ok)
        dialog.window?.setLayout((width * .9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)
        textView.text = "Payment Status:" + pay.status
        if (pay.status == "complete") {
            ok.text = "Print"
        }
        val etDesc: TextView = dialog.findViewById(R.id.etDesc)
        ok.setOnClickListener { view: View? ->
            pay.desc = etDesc.text.toString()
            if (pay.status == "complete") {
                loadObservers()
                PrintDialogFragment(null, pay, arrayListOf()) {
                    (requireActivity() as MerchantMainActivity).clearAndGoBack()
                }.show(childFragmentManager, null)
                dialog.dismiss()
            } else {
                loadObservers()
                dialog.dismiss()
            }
        }
        ivBack.setOnClickListener { v: View? ->
            dialog.dismiss()
            loadObservers()
        }
        dialog.show()
    }

    //Getting the scan results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.isNotEmpty() && permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cSV
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1234 -> {
                // HANDLE LockIng
                super.onActivityResult(requestCode, resultCode, data)
                if (requestCode == 1234) {
                    if (resultCode == Activity.RESULT_OK) {
                        //do something you want when pass the security
                        // Toast.makeText(getApplicationContext(),"done",Toast.LENGTH_SHORT).show();
                        dialogBoxForRefundCommandeer()
                    }
                }
            }
        }
    }

    private fun saveGetRefundTransactionInLog(pay: Pay, label: String) {
        val precision = DecimalFormat("0.00")
        val status = pay.status
        val transactionAmountbtc = excatFigure(AdminBaseFragment.round(amountBtc, 9))
        val transactionAmountusd = precision.format(amountUsd)
        val conversionRate = precision.format(conversionRate)
        val msatoshi = excatFigure(msatoshi)
        val paymentPreimage = pay.payment_preimage
        val paymentHash = pay.payment_hash
        val destination = pay.destination
        val merchantId = GlobalState.getInstance().merchant_id
        val transactionDescription1 = ""
        addMerchantTransaction(
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

    private fun addMerchantTransaction(
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
            .add_merchant_transction(
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

    @get:Throws(FileNotFoundException::class)
    val cSV: Unit
        get() {
            if (mSaleDataSource.size > 0) {
                dailyReportSendingEmailSale(mSaleDataSource)
                weeklyReportSendingEmailSale(mSaleDataSource)
                monthlyReportSendingEmailSale(mSaleDataSource)
            }
            if (mRefundDataSource.size > 0) {
                dailyReportSendingEmailRefund(mRefundDataSource)
                weeklyReportSendingEmailRefund(mRefundDataSource)
                monthlyReportSendingEmailRefund(mRefundDataSource)
            }
        }

    //TODO:Refunds Emailing
    private fun monthlyReportSendingEmailRefund(mRefundDataSource: ArrayList<Refund>) {
        val todayList = ArrayList<Refund>()
        for (refund in mRefundDataSource) {
            val curentTime = Date().time
            val paidTime = refund.created_at * 1000
            val currentDate = Date(curentTime)
            val paidDate = Date(paidTime)
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = currentDate
            cal2.time = paidDate
            todayList.add(refund)
        }
        if (todayList.size > 0) {
            var date: Date? = null
            try {
                val sdf = SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US)
                date = sdf.parse(preferences.getString("myMonthlyDate2", "01-01-2000"))
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val curentTime = Date().time
            val currentDate = Date(curentTime)
            if (date != null) {
                val cal1 = Calendar.getInstance()
                val cal2 = Calendar.getInstance()
                cal1.time = date
                cal2.time = currentDate
                val sameDay = cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                        cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
                if (sameDay) {
                    //showToast("Already Refund Email Report Month ");
                } else {
                    //showToast("New  Refund Report Email Month");
                    sendEmailMonthlyRefund(todayList)
                }
            } else {
                //showToast("First Refund Time New Report Email Month");
                sendEmailMonthlyRefund(todayList)
            }
        }
    }

    private fun sendEmailMonthlyRefund(todayList: ArrayList<Refund>) {
        var merchantId = "merchant"
        val userInfo = GlobalState.getInstance().userInfo
        if (userInfo != null) {
            if (userInfo.userID != null) {
                merchantId = userInfo.userID
            }
        }
        val curentTime = Date().time
        val currentDate = Date(curentTime)
        val sdf = SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US)
        preferences.edit().putString("myMonthlyDate2", sdf.format(currentDate)).apply()

        //Create Folder
        var file: File? = null
        val t = createDirIfNotExists("/CLightData/Refund/Monthly")
        if (t) {
            val extStorageDirectory = file.toString()
            val fileName = merchantId + "_Monthly" + unixTimeStamp + ".csv"
            file = File(extStorageDirectory, fileName)
            try {
                // create FileWriter object with file as parameter
                val outputfile = FileWriter(file)

                // create CSVWriter object filewriter object as parameter
                val writer = CSVWriter(outputfile)
                val data2 = toStringArrayFromRefund(todayList)
                // create a List which contains String array
                writer.writeAll(data2)
                // closing writer connection
                writer.close()
                GlobalState.setRefundFile(file)
                sendRefundEmail("Monthly")
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        } else {
            showToast("Error Making Folder")
        }
    }

    private fun weeklyReportSendingEmailRefund(mRefundDataSource: ArrayList<Refund>) {
        val todayList = ArrayList<Refund>()
        for (refund in mRefundDataSource) {
            val curentTime = Date().time
            val paidTime = refund.created_at * 1000
            val currentDate = Date(curentTime)
            val paidDate = Date(paidTime)
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = currentDate
            cal2.time = paidDate
            val isSameWeek = isCurrentWeekDateSelect(cal2)
            if (isSameWeek) {
                todayList.add(refund)
            }
        }
        if (todayList.size > 0) {
            var date: Date? = null
            try {
                val sdf = SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US)
                date = sdf.parse(preferences.getString("myWeeklyDate2", "01-01-2000"))
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val curentTime = Date().time
            val currentDate = Date(curentTime)
            if (date != null) {
                val cal1 = Calendar.getInstance()
                val cal2 = Calendar.getInstance()
                cal1.time = date
                cal2.time = currentDate
                val sameWeek = isCurrentWeekDateSelect(cal1)
                if (!sameWeek) {
                    sendEmailWeeklyRefund(todayList)
                }
            } else {
                sendEmailWeeklyRefund(todayList)
            }
        }
    }

    private fun sendEmailWeeklyRefund(todayList: ArrayList<Refund>) {
        var merchantId = "merchant"
        val userInfo = GlobalState.getInstance().userInfo
        if (userInfo != null) {
            if (userInfo.userID != null) {
                merchantId = userInfo.userID
            }
        }
        val curentTime = Date().time
        val currentDate = Date(curentTime)
        val sdf = SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US)
        preferences.edit().putString("myWeeklyDate2", sdf.format(currentDate)).apply()

        //Create Folder
        var file: File? = null
        val t = createDirIfNotExists("/CLightData/Refund/Weekly")
        if (t) {
            val extStorageDirectory = file.toString()
            val fileName = merchantId + "_Weekly" + unixTimeStamp + ".csv"
            file = File(extStorageDirectory, fileName)
            try {
                // create FileWriter object with file as parameter
                val outputfile = FileWriter(file)

                // create CSVWriter object filewriter object as parameter
                val writer = CSVWriter(outputfile)
                val data2 = toStringArrayFromRefund(todayList)
                // create a List which contains String array
                writer.writeAll(data2)
                // closing writer connection
                writer.close()
                GlobalState.setSaleFile(file)
                sendRefundEmail("Weekly")
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        } else {
            showToast("Error Making Folder")
        }
    }

    private fun dailyReportSendingEmailRefund(mRefundDataSource: ArrayList<Refund>) {
        val todayList = ArrayList<Refund>()
        for (refund in mRefundDataSource) {
            val curentTime = Date().time
            val paidTime = refund.created_at * 1000
            val currentDate = Date(curentTime)
            val paidDate = Date(paidTime)
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = currentDate
            cal2.time = paidDate
            val sameDay = cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                    cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
            if (sameDay) {
                todayList.add(refund)
            }
        }
        if (todayList.size > 0) {
            var date: Date? = null
            try {
                val sdf = SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US)
                date = sdf.parse(preferences.getString("myDailyDate2", "01-01-2000"))
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val curentTime = Date().time
            val currentDate = Date(curentTime)
            if (date != null) {
                val cal1 = Calendar.getInstance()
                val cal2 = Calendar.getInstance()
                cal1.time = date
                cal2.time = currentDate
                val sameDay = cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                        cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
                if (!sameDay) {
                    sendEmailDailyRefund(todayList)
                }
            } else {
                // showToast("First Time New Report Email Today ");
                sendEmailDailyRefund(todayList)
            }
        }
    }

    private fun sendEmailDailyRefund(todayList: ArrayList<Refund>) {
        val curentTime = Date().time
        val currentDate = Date(curentTime)
        val sdf = SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US)
        preferences.edit().putString("myDailyDate2", sdf.format(currentDate)).apply()
        var merchantId = "merchant"
        val userInfo = GlobalState.getInstance().userInfo
        if (userInfo != null) {
            if (userInfo.userID != null) {
                merchantId = userInfo.userID
            }
        }
        //Create Folder
        var file: File? = null
        val t = createDirIfNotExists("/CLightData/Refund/Daily")
        if (t) {
            val extStorageDirectory = file.toString()
            val fileName = merchantId + "_daily" + unixTimeStamp + ".csv"
            file = File(extStorageDirectory, fileName)
            try {
                // create FileWriter object with file as parameter
                val outputfile = FileWriter(file)

                // create CSVWriter object filewriter object as parameter
                val writer = CSVWriter(outputfile)
                val data2 = toStringArrayFromRefund(todayList)
                // create a List which contains String array
                writer.writeAll(data2)
                // closing writer connection
                writer.close()
                GlobalState.setSaleFile(file)
                sendRefundEmail("Daily")
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        } else {
            showToast("Error Making Folder")
        }
    }

    //TODO:Sales Emailing
    private fun monthlyReportSendingEmailSale(saleList: ArrayList<Invoice>) {
        val todayList = ArrayList<Invoice>()
        for (sale in saleList) {
            val curentTime = Date().time
            val paidTime = sale.paid_at * 1000
            val currentDate = Date(curentTime)
            val paidDate = Date(paidTime)
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = currentDate
            cal2.time = paidDate
            todayList.add(sale)
        }
        if (todayList.size > 0) {
            var date: Date? = null
            try {
                val sdf = SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US)
                date = sdf.parse(preferences.getString("myMonthlyDate", "01-01-2000"))
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val curentTime = Date().time
            val currentDate = Date(curentTime)
            if (date != null) {
                val cal1 = Calendar.getInstance()
                val cal2 = Calendar.getInstance()
                cal1.time = date
                cal2.time = currentDate
                val sameDay = cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                        cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
                if (!sameDay) {
                    sendEmailMonthly(todayList)
                }
            } else {
                // showToast("First Time New Report Email Month");
                sendEmailMonthly(todayList)
            }
        }
    }

    private fun sendEmailMonthly(todayList: ArrayList<Invoice>) {
        var merchantId = "merchant"
        val userInfo = GlobalState.getInstance().userInfo
        if (userInfo != null) {
            if (userInfo.userID != null) {
                merchantId = userInfo.userID
            }
        }
        val curentTime = Date().time
        val currentDate = Date(curentTime)
        val sdf = SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US)
        preferences.edit().putString("myMonthlyDate", sdf.format(currentDate)).apply()

        //Create Folder
        var file: File? = null
        val t = createDirIfNotExists("/CLightData/Sales/Monthly")
        if (t) {
            val extStorageDirectory = file.toString()
            val fileName = merchantId + "_Monthly" + unixTimeStamp + ".csv"
            file = File(extStorageDirectory, fileName)
            try {
                // create FileWriter object with file as parameter
                val outputfile = FileWriter(file)

                // create CSVWriter object filewriter object as parameter
                val writer = CSVWriter(outputfile)
                val data2 = toStringArrayFromSale(todayList)
                // create a List which contains String array
                writer.writeAll(data2)
                // closing writer connection
                writer.close()
                GlobalState.setSaleFile(file)
                sendSaleEmail("Monthly")
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        } else {
            showToast("Error Making Folder")
        }
    }

    private fun weeklyReportSendingEmailSale(saleList: ArrayList<Invoice>) {
        val todayList = ArrayList<Invoice>()
        for (sale in saleList) {
            val curentTime = Date().time
            val paidTime = sale.paid_at * 1000
            val currentDate = Date(curentTime)
            val paidDate = Date(paidTime)
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = currentDate
            cal2.time = paidDate
            val isSameWeek = isCurrentWeekDateSelect(cal2)
            if (isSameWeek) {
                todayList.add(sale)
            }
        }
        if (todayList.size > 0) {
            var date: Date? = null
            try {
                val sdf = SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US)
                date = sdf.parse(preferences.getString("myWeeklyDate", "01-01-2000"))
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val curentTime = Date().time
            val currentDate = Date(curentTime)
            if (date != null) {
                val cal1 = Calendar.getInstance()
                val cal2 = Calendar.getInstance()
                cal1.time = date
                cal2.time = currentDate
                val sameWeek = isCurrentWeekDateSelect(cal1)
                if (!sameWeek) {
                    sendEmailWeekly(todayList)
                }
            } else {
                //showToast("First Time New Report Email in Week");
                sendEmailWeekly(todayList)
            }
        }
    }

    private fun sendEmailWeekly(todayList: ArrayList<Invoice>) {
        var merchantId = "merchant"
        val userInfo = GlobalState.getInstance().userInfo
        if (userInfo != null) {
            if (userInfo.userID != null) {
                merchantId = userInfo.userID
            }
        }
        val curentTime = Date().time
        val currentDate = Date(curentTime)
        val sdf = SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US)
        preferences.edit().putString("myWeeklyDate", sdf.format(currentDate)).apply()

        //Create Folder
        var file: File? = null
        val t = createDirIfNotExists("/CLightData/Sales/Weekly")
        if (t) {
            val extStorageDirectory = file.toString()
            val fileName = merchantId + "_Weekly" + unixTimeStamp + ".csv"
            file = File(extStorageDirectory, fileName)
            try {
                // create FileWriter object with file as parameter
                val outputfile = FileWriter(file)

                // create CSVWriter object filewriter object as parameter
                val writer = CSVWriter(outputfile)
                val data2 = toStringArrayFromSale(todayList)
                // create a List which contains String array
                writer.writeAll(data2)
                // closing writer connection
                writer.close()
                GlobalState.setSaleFile(file)
                sendSaleEmail("Weekly")
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        } else {
            showToast("Error Making Folder")
        }
    }

    private fun dailyReportSendingEmailSale(saleList: ArrayList<Invoice>) {
        val todayList = ArrayList<Invoice>()
        for (sale in saleList) {
            val curentTime = Date().time
            val paidTime = sale.paid_at * 1000
            val currentDate = Date(curentTime)
            val paidDate = Date(paidTime)
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = currentDate
            cal2.time = paidDate
            val sameDay = cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                    cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
            if (sameDay) {
                todayList.add(sale)
            }
        }
        if (todayList.size > 0) {
            var date: Date? = null
            try {
                val sdf = SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US)
                date = sdf.parse(preferences.getString("myDailyDate", "01-01-2000"))
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val curentTime = Date().time
            val currentDate = Date(curentTime)
            if (date != null) {
                val cal1 = Calendar.getInstance()
                val cal2 = Calendar.getInstance()
                cal1.time = date
                cal2.time = currentDate
                val sameDay = cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                        cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
                if (sameDay) {
                    //showToast("Already Email Report Today ");
                } else {
                    //showToast("New  Report Email Today ");
                    sendEmailDaily(todayList)
                }
            } else {
                //showToast("First Time New Report Email Today ");
                sendEmailDaily(todayList)
            }
        }
    }

    private fun sendEmailDaily(todayList: ArrayList<Invoice>) {
        val curentTime = Date().time
        val currentDate = Date(curentTime)
        val sdf = SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US)
        preferences.edit().putString("myDailyDate", sdf.format(currentDate)).apply()
        var merchantId = "merchant"
        val userInfo = GlobalState.getInstance().userInfo
        if (userInfo != null) {
            if (userInfo.userID != null) {
                merchantId = userInfo.userID
            }
        }

        //Create Folder
        var file: File? = null
        val t = createDirIfNotExists("/CLightData/Sales/Daily")
        if (t) {
            val extStorageDirectory = file.toString()
            val fileName = merchantId + "_daily" + unixTimeStamp + ".csv"
            file = File(extStorageDirectory, fileName)
            try {
                // create FileWriter object with file as parameter
                val outputfile = FileWriter(file)

                // create CSVWriter object filewriter object as parameter
                val writer = CSVWriter(outputfile)
                val data2 = toStringArrayFromSale(todayList)
                // create a List which contains String array
                writer.writeAll(data2)
                // closing writer connection
                writer.close()
                GlobalState.setSaleFile(file)
                sendSaleEmail("Daily")
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        } else {
            showToast("Error Making Folder")
        }
    }

    private fun isCurrentWeekDateSelect(yourSelectedDate: Calendar): Boolean {
        val ddd = yourSelectedDate.time
        val c = Calendar.getInstance(TimeZone.getTimeZone("CST"))
        c.firstDayOfWeek = Calendar.MONDAY
        c[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        c[Calendar.HOUR_OF_DAY] = 0
        c[Calendar.MINUTE] = 0
        c[Calendar.SECOND] = 0
        c[Calendar.MILLISECOND] = 0
        val monday = c.time
        val nextMonday = Date(monday.time + 7 * 24 * 60 * 60 * 1000)
        return ddd.after(monday) && ddd.before(nextMonday)
    }

    private fun getSenderInfo(mail: String, pas: String, emailType: String) {
        var merchantId = "Merchant " + emailType + " Sales Sheet" + getDateFromUTCTimestamp(
            unixTimeStampInLong,
            AppConstants.OUTPUT_DATE_FORMATE
        )
        val userInfo = GlobalState.getInstance().userInfo
        if (userInfo != null) {
            if (userInfo.userID != null) {
                merchantId = userInfo.userID + " Sales Sheet " + getDateFromUTCTimestamp(
                    unixTimeStampInLong,
                    AppConstants.OUTPUT_DATE_FORMATE
                )
            }
        }
        val finalMerchantId = merchantId
        val sender = Thread {
            try {
                gMailSender = GMailSender(mail, pas)
                senderEmail = mail
                gMailSender?.sendMail(
                    "CLight App",
                    "Sales ",
                    senderEmail,
                    "decentralizedworldinc@protonmail.com", finalMerchantId, 1
                )
            } catch (e: Exception) {
                Log.e("mylog", "Error: " + e.message)
                //Toast.makeText(getApplicationContext(),"Error:"+e.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        }
        sender.start()
    }

    private fun getSenderInfo2(mail: String, pas: String, emailType: String) {
        var merchantId = "Merchant " + emailType + " Sales Sheet" + getDateFromUTCTimestamp(
            unixTimeStampInLong,
            AppConstants.OUTPUT_DATE_FORMATE
        )
        val userInfo = GlobalState.getInstance().userInfo
        if (userInfo != null) {
            if (userInfo.userID != null) {
                merchantId = userInfo.userID + " Sales Sheet " + getDateFromUTCTimestamp(
                    unixTimeStampInLong,
                    AppConstants.OUTPUT_DATE_FORMATE
                )
            }
        }
        //d4amenace@yahoo.com
        val finalMerchantId = merchantId
        val sender = Thread {
            try {
                gMailSender = GMailSender(mail, pas)
                senderEmail = mail
                gMailSender?.sendMail(
                    "CLight App",
                    "Sales ",
                    senderEmail,
                    "d4amenace@yahoo.com", finalMerchantId, 1
                )
            } catch (e: Exception) {
                Log.e("mylog", "Error: " + e.message)
                //Toast.makeText(getApplicationContext(),"Error:"+e.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        }
        sender.start()
    }

    private fun getSenderInfo3(mail: String, pas: String, emailType: String) {
        var merchantId = "Merchant " + emailType + " Refunds Sheet" + getDateFromUTCTimestamp(
            unixTimeStampInLong,
            AppConstants.OUTPUT_DATE_FORMATE
        )
        val userInfo = GlobalState.getInstance().userInfo
        if (userInfo != null) {
            if (userInfo.userID != null) {
                merchantId = userInfo.userID + " Refunds Sheet " + getDateFromUTCTimestamp(
                    unixTimeStampInLong,
                    AppConstants.OUTPUT_DATE_FORMATE
                )
            }
        }
        //d4amenace@yahoo.com
        val finalMerchantId = merchantId
        val sender = Thread {
            try {
                gMailSender = GMailSender(mail, pas)
                senderEmail = mail
                gMailSender?.sendMail(
                    "CLight App",
                    "Refunds ",
                    senderEmail,
                    "decentralizedworldinc@protonmail.com", finalMerchantId, 0
                )
            } catch (e: Exception) {
                Log.e("mylog", "Error: " + e.message)
                //Toast.makeText(getApplicationContext(),"Error:"+e.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        }
        sender.start()
    }

    private fun getSenderInfo4(mail: String, pas: String, emailType: String) {
        var merchantId = "Merchant $emailType Refunds Sheet" + getDateFromUTCTimestamp(
            unixTimeStampInLong,
            AppConstants.OUTPUT_DATE_FORMATE
        )
        val userInfo = GlobalState.getInstance().userInfo
        if (userInfo != null) {
            if (userInfo.userID != null) {
                merchantId = userInfo.userID + " Refunds Sheet " + getDateFromUTCTimestamp(
                    unixTimeStampInLong,
                    AppConstants.OUTPUT_DATE_FORMATE
                )
            }
        }
        //d4amenace@yahoo.com
        val finalMerchantId = merchantId
        val sender = Thread {
            try {
                gMailSender = GMailSender(mail, pas)
                senderEmail = mail
                gMailSender?.sendMail(
                    "CLight App",
                    "Refunds ",
                    senderEmail,
                    "d4amenace@yahoo.com", finalMerchantId, 0
                )
            } catch (e: Exception) {
                Log.e("mylog", "Error: " + e.message)
                //Toast.makeText(getApplicationContext(),"Error:"+e.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        }
        sender.start()
    }

    private fun sendSaleEmail(emailType: String) {
        getSenderInfo("nextlayertechnology@gmail.com", "bitcoin2020", emailType)
        getSenderInfo2("nextlayertechnology@gmail.com", "bitcoin2020", emailType)
    }

    private fun sendRefundEmail(emailType: String) {
        getSenderInfo3("nextlayertechnology@gmail.com", "bitcoin2020", emailType)
        getSenderInfo4("nextlayertechnology@gmail.com", "bitcoin2020", emailType)
    }

    private fun findMerchant(id: String, pass: String) {
        progressDialog.show()
        val paramObject = JsonObject()
        paramObject.addProperty("user_id", id)
        paramObject.addProperty("password", pass)
        val call = webservice.merchant_Loging(paramObject)
        call.enqueue(object : Callback<MerchantLoginResp?> {
            override fun onResponse(
                call: Call<MerchantLoginResp?>,
                response: Response<MerchantLoginResp?>
            ) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()?.message == "successfully login") {
                            val merchantData = response.body()?.merchantData
                            merchantData?.let {
                                sessionService.setMerchantData(it)
                                GlobalState.getInstance().merchant_id = id
                                //                            GlobalState.getInstance().setMerchantData(merchantData);
                                val tax = Tax()
                                tax.taxInUSD = 1.0
                                tax.taxInBTC = 0.00001
                                tax.taxpercent = java.lang.Double.valueOf(merchantData.tax_rate)
                                GlobalState.getInstance().tax = tax
                                CustomSharedPreferences().setString(
                                    merchantData?.ssh_password,
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
            }

            override fun onFailure(call: Call<MerchantLoginResp?>, t: Throwable) {
                progressDialog.dismiss()
            }
        })
    }

    companion object {
        var senderEmail: String? = null
        const val REQUEST_WRITE_PERMISSION = 786

        fun createDirIfNotExists(path: String?): Boolean {
            var ret = true
            val file = File(Environment.getExternalStorageDirectory(), path)
            if (!file.exists() && !file.mkdirs()) {
                Log.e("TravellerLog :: ", "Problem creating Image folder")
                ret = false
            }
            return ret
        }

        private fun toStringArrayFromSale(emps: List<Invoice>): List<Array<String?>> {
            val records: MutableList<Array<String?>> = ArrayList()
            records.add(
                arrayOf(
                    "label",
                    "msatoshi",
                    "status",
                    "paid_at",
                    "payment_preimage",
                    "description"
                )
            )
            val it = emps.iterator()
            while (it.hasNext()) {
                val emp = it.next()
                records.add(
                    arrayOf(
                        emp.label,
                        excatFigure2(emp.msatoshi),
                        emp.status,
                        getDateFromUTCTimestamp2(emp.paid_at, AppConstants.OUTPUT_DATE_FORMATE),
                        emp.payment_preimage,
                        emp.description
                    )
                )
            }
            return records
        }

        private fun toStringArrayFromRefund(emps: List<Refund>): List<Array<String?>> {
            val records: MutableList<Array<String?>> = ArrayList()

            // adding header record
            records.add(
                arrayOf(
                    "bolt11",
                    "msatoshi",
                    "status",
                    "created_at",
                    "destination",
                    "payment_hash",
                    "payment_preimage"
                )
            )
            val it = emps.iterator()
            while (it.hasNext()) {
                val emp = it.next()
                records.add(
                    arrayOf(
                        emp.bolt11,
                        excatFigure2(emp.msatoshi),
                        emp.status,
                        getDateFromUTCTimestamp2(emp.created_at, AppConstants.OUTPUT_DATE_FORMATE),
                        emp.destination,
                        emp.payment_hash,
                        emp.payment_preimage
                    )
                )
            }
            return records
        }
    }

    override fun onStop() {
        super.onStop()
        if (fcmBroadcastReceiver != null) {
            //crashes here
            requireContext().unregisterReceiver(fcmBroadcastReceiver)
        }
    }
}