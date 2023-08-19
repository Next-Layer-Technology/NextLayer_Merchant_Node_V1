package com.sis.clightapp.activity

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.google.zxing.integration.android.IntentIntegrator
import com.sis.clightapp.Interface.ApiFCM
import com.sis.clightapp.Interface.ApiPaths2
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.R
import com.sis.clightapp.model.FCMResponse
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData
import com.sis.clightapp.model.GsonModel.Merchant.MerchantLoginResp
import com.sis.clightapp.model.REST.get_session_response
import com.sis.clightapp.model.WebsocketResponse.WebSocketOTPresponse
import com.sis.clightapp.model.WebsocketResponse.WebSocketResponse
import com.sis.clightapp.util.CustomSharedPreferences
import com.sis.clightapp.util.GlobalState
import org.json.JSONException
import org.json.JSONObject
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tech.gusavila92.websocketclient.WebSocketClient
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.*

class MainEntryActivityNew : BaseActivity() {
    private val apiClient: ApiPaths2 by inject()
    private val webservice: Webservice by inject()

    private lateinit var registerBtn: TextView
    private lateinit var cancelAction: TextView
    private lateinit var registerAction: TextView
    lateinit var confirmingProgressDialog: ProgressDialog
    var currentMerchantData: MerchantData? = null
    var isConfirmMerchant = false
    var isLoginMerchant = false
    var code = 0
    var code1 = ""
    private lateinit var webSocketClient: WebSocketClient
    var keyguardManager: KeyguardManager? = null
    private var qrScan: IntentIntegrator? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_entry_new)
        confirmingProgressDialog = ProgressDialog(this@MainEntryActivityNew)
        confirmingProgressDialog!!.setMessage("Confirming...")
        confirmingProgressDialog!!.setCancelable(false)
        confirmingProgressDialog!!.setCanceledOnTouchOutside(false)
        registerBtn = findViewById(R.id.register_btn)
        //        Button button = findViewById(R.id.print);
//        button.setVisibility(View.VISIBLE);
//        button.setOnClickListener(v -> {
//            Invoice invoice = new Invoice();
//            invoice.setLabel("Label");
//            invoice.setAmount_msat("60000");
//            invoice.setAmount_received_msat("0.0000001");
//            invoice.setDescription("description");
//            invoice.setBolt11("lnbc662n1p3cd6m2sp5r6ulc2tgry7c9smm9ndrmxptg0mwmkyhk3sa6em2c8yqq4s8ntlqpp5juxqr05v30l4vuqf6g067xwl4q8xw69nwsttrncwx49lp8f9ve2sdq8v3jhxccxqzfvcqpjrzjqflfuth6uaxmx7pvaj304s4p9qzkm2gj0qhhg34k2h8w882fdsupgzadygqqvxqqqyqqqqqqqqqqqqqqyg9qyysgq8cpttudvl6z7zgclccfl36kqdsjcjsz60qg5zrhsdlz0y5w3l7xj0pystnuyu6s927da0hwqq5vycsuwys400qe3dungn9q5pn0gxyqqsz45sd");
//            invoice.setPayment_hash("970c01be8c8bff567009d21faf19dfa80e6768b37416b1cf0e354bf09d256655");
//            invoice.setExpires_at(System.currentTimeMillis());
//            invoice.setMsatoshi(60000);
//            invoice.setPaid_at(System.currentTimeMillis());
//            invoice.setPay_index(1);
//            invoice.setStatus("complete");
//            new PrintDialogFragment(invoice, null, new ArrayList()).show(getSupportFragmentManager(), null);
//        });
        keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        expireToken
        if (CustomSharedPreferences().getvalueofMerchantId("merchant_id", this) != 0) {
            findMerchant(
                CustomSharedPreferences().getvalueofMerchantname("merchant_name", this),
                CustomSharedPreferences().getvalueofMerchantpassword("merchant_pass", this)
            )
        }
        registerBtn.setOnClickListener(View.OnClickListener { v: View? ->
            if (sharedPreferences.getislogin("registered", this)) {
                showToast("You are registered already")
            } else {
                if (sharedPreferences.getissavecredential("credential", this)) {
                    dialogB()
                } else {
                    dialogA()
                }
                isLoginMerchant = false
                GlobalState.getInstance().login = isLoginMerchant
            }
        })
        findViewById<View>(R.id.signin_btn).setOnClickListener { v: View? ->
            if (sharedPreferences.getislogin("registered", this)) {
                isLoginMerchant = true
                GlobalState.getInstance().login = isLoginMerchant
                val i = keyguardManager!!.createConfirmDeviceCredentialIntent("Authentication required", "password")
                startActivityForResult(i, 241)
            } else {
                loginPressed()
            }
        }
        qrScan = IntentIntegrator(this)
        qrScan!!.setOrientationLocked(false)
        val prompt = resources.getString(R.string.scanqrfornewmembertoken)
        qrScan!!.setPrompt(prompt)
    }

    override fun onResume() {
        super.onResume()
        checkSecureLockEnabledElseShowPopup()
    }

    private fun checkSecureLockEnabledElseShowPopup() {
        if (!keyguardManager!!.isKeyguardSecure) {
            dialog_LockCheck()
        }
    }

    fun setToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
            if (!task.isSuccessful) {
                Log.w("tFCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            sendRegistrationToServer(token)
            Log.d("tes2Fcm", token!!)
        }
    }

    private fun sendRegistrationToServer(token: String?) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://nextlayer.live/testfcm/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiInterface = retrofit.create(ApiFCM::class.java)
        try {
            val paramObject = JsonObject()
            paramObject.addProperty("fcmRegToken", token)
            val paramObject1 = JsonObject()
            paramObject1.addProperty("pwsUpdate", "New Token")
            paramObject.add("payload", paramObject1)
            val call = apiInterface.FcmHitForToken(paramObject)
            call.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    Log.e("TAG", "onResponse: " + response.body().toString())
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Log.e("TAG", "onResponse: " + t.message.toString())
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 241) {
            if (resultCode == RESULT_OK) {
                if (code == 724) {
                    dialogC()
                } else {
                    createWebSocketClient()
                }
                Toast.makeText(this, "Success: Verified user's identity", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failure: Unable to verify user's identity", Toast.LENGTH_SHORT).show()
            }
        } else {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    showToast("Result Not Found")
                } else {
                    val memberToken = result.contents
                    if (etEmail != null) {
                        etEmail!!.setText(memberToken)
                    }
                    val ip_Address = etIpaddress!!.text.toString()
                    if (!ip_Address.isEmpty()) {
                        sharedPreferences.setvalueofipaddress(ip_Address, "ip", this)
                    }
                    if (sharedPreferences.getvalueofRefresh("refreshToken", this) == "") {
                        if (memberToken.isEmpty()) {
                            showToast("Enter refresh Token")
                        } else if (sharedPreferences.getvalueofipaddress("ip", this) == "") {
                            showToast("Enter Ip Adress")
                        } else {
                            try {
                                getOTP(memberToken)
                                dialogBBuilder!!.dismiss()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        if (sharedPreferences.getvalueofipaddress("ip", this) == "") {
                            showToast("Enter Ip Adress")
                        } else {
                            try {
                                getOTP(sharedPreferences.getvalueofRefresh("refreshToken", this))
                                dialogBBuilder!!.dismiss()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    @Throws(JSONException::class)
    private fun getOTP(refresh: String) {
        val jsonObject1 = JsonObject()
        jsonObject1.addProperty("refresh", refresh)
        val call: Call<WebSocketResponse> = apiClient.getotp(jsonObject1)
        call.enqueue(object : Callback<WebSocketResponse?> {
            override fun onResponse(call: Call<WebSocketResponse?>, response: Response<WebSocketResponse?>) {
                if (response.body() != null) {
                    val webSocketResponse = response.body()
                    if (webSocketResponse!!.code == 700) {
                        sharedPreferences.setvalueofOtpSecret(
                            webSocketResponse.token,
                            "otpsecret",
                            this@MainEntryActivityNew
                        )
                        sharedPreferences.setvalueofRefresh(refresh, "refreshToken", this@MainEntryActivityNew)
                        if (!webSocketResponse.token.isEmpty()) {
                            sharedPreferences.setvalueofRefresh(refresh, "refreshToken", this@MainEntryActivityNew)
                            dialog_Otp_Code(webSocketResponse.token)
                        }
                    } else if (webSocketResponse.code == 701) {
                        sharedPreferences.setvalueofRefresh(refresh, "refreshToken", this@MainEntryActivityNew)
                        dialogC()
                        showToast(webSocketResponse.message)
                    } else if (webSocketResponse.code == 702) {
                        showToast(webSocketResponse.message)
                    } else if (webSocketResponse.code == 703) {
                        showToast(webSocketResponse.message)
                    } else if (webSocketResponse.code == 704) {
                        showToast(webSocketResponse.message)
                    } else if (webSocketResponse.code == 711) {
                        showToast(webSocketResponse.message)
                    } else if (webSocketResponse.code == 716) {
                        showToast(webSocketResponse.message)
                    } else if (webSocketResponse.code == 721) {
                        showToast(webSocketResponse.message)
                    } else if (webSocketResponse.code == 722) {
                        showToast(webSocketResponse.message)
                    } else if (webSocketResponse.code == 723) {
                        showToast(webSocketResponse.message)
                    } else if (webSocketResponse.code == 724) {
                        goTo2FaPasswordDialog(refresh)
                        showToast(webSocketResponse.message)
                    } else if (webSocketResponse.code == 725) {
                        showToast(webSocketResponse.message)
                    }
                }
            }

            override fun onFailure(call: Call<WebSocketResponse?>, t: Throwable) {
                dialog_GetInfo(1, t.message)
                Log.e("get-funding-nodes:", t.message!!)
                showToast("get-funding-nodes:" + t.message)
            }
        })
    }

    private fun getToken(refresh: String, twofactor_key: String) {
        val time = CustomSharedPreferences().getvalueofExpierTime(this)
        val jsonObject1 = JsonObject()
        jsonObject1.addProperty("refresh", refresh)
        jsonObject1.addProperty("twoFactor", twofactor_key)
        jsonObject1.addProperty("time", time)
        val call: Call<WebSocketOTPresponse> =
            apiClient.gettoken(jsonObject1)
        call.enqueue(object : Callback<WebSocketOTPresponse?> {
            override fun onResponse(call: Call<WebSocketOTPresponse?>, response: Response<WebSocketOTPresponse?>) {
                confirmingProgressDialog!!.dismiss()
                if (response.body() != null) {
                    val webSocketOTPresponse = response.body()
                    if (webSocketOTPresponse!!.code == 700) {
                        code = 0
                        sharedPreferences.setislogin(true, "registered", this@MainEntryActivityNew)
                        if (webSocketOTPresponse.token == "") {
                        } else {
                            sharedPreferences.setvalueofaccestoken(
                                webSocketOTPresponse.token,
                                "accessToken",
                                this@MainEntryActivityNew
                            )
                            createWebSocketClient()
                            val isTokenSet = CustomSharedPreferences().getvalue("IsTokenSet", this@MainEntryActivityNew)
                            if (isTokenSet == "1") {
                                val token = CustomSharedPreferences().getString("FcmToken", this@MainEntryActivityNew)
                                token?.let { setFCMToken(it, refresh) }
                            }
                        }
                    } else if (webSocketOTPresponse.code == 701) {
                        dialogC()
                        showToast("Missing 2FA code when requesting an access token")
                    } else if (webSocketOTPresponse.code == 702) {
                        dialogC()
                        showToast("2FA code is incorrect / has timed out (30s window)")
                    } else if (webSocketOTPresponse.code == 703) {
                        showToast("refresh token missing when requesting access code")
                    } else if (webSocketOTPresponse.code == 704) {
                        showToast("refresh token missing when requesting access code")
                    } else if (webSocketOTPresponse.code == 711) {
                        showToast("error -> attempting to initialize 2FA with the admin refresh code in a client system")
                    } else if (webSocketOTPresponse.code == 716) {
                        showToast("Refresh token has expired (6 months), a new one is being mailed to the user")
                    } else if (webSocketOTPresponse.code == 721) {
                        showToast("SendCommands is missing a \"commands\" field")
                    } else if (webSocketOTPresponse.code == 722) {
                        showToast("SendCommands is missing a \"token\" with the access token")
                    } else if (webSocketOTPresponse.code == 723) {
                        showToast("SendCommands received a refresh token instead of an access token")
                    } else if (webSocketOTPresponse.code == 724) {
                        showToast("Access token has expired (at this point request 2FA code and get a new access token from /Refresh")
                        goTo2FaPasswordDialog(refresh)
                    } else if (webSocketOTPresponse.code == 725) {
                        showToast("Misc websocket error, \"message\" field will include more data")
                    }
                }
            }

            override fun onFailure(call: Call<WebSocketOTPresponse?>, t: Throwable) {
                confirmingProgressDialog!!.dismiss()
                Log.e("get-funding-nodes:", t.message!!)
            }
        })
    }

    private fun setFCMToken(tokenFCM: String, refreshToken: String) {
        val jsonObject1 = JsonObject()
        jsonObject1.addProperty("refresh", refreshToken)
        jsonObject1.addProperty("fcmRegToken", tokenFCM)
        val call: Call<FCMResponse> = apiClient.setFcmToken(jsonObject1)
        call.enqueue(object : Callback<FCMResponse?> {
            override fun onResponse(call: Call<FCMResponse?>, response: Response<FCMResponse?>) {
                assert(response.body() != null)
                Log.d(Companion.TAG, "onResponse: " + response.body())
                if (response.body() != null) {
                    val fcmResponse = response.body()
                    if (fcmResponse!!.code == 700) {
                        CustomSharedPreferences().setvalue("0", "IsTokenSet", applicationContext)
                    }
                }
            }

            override fun onFailure(call: Call<FCMResponse?>, t: Throwable) {
                Log.e("get-funding-nodes:", t.message!!)
            }
        })
    }

    private fun loginPressed() {
        val dialogBuilder = android.app.AlertDialog.Builder(this, R.style.AlertDialog).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialoglayoutpresslogin, null)
        dialogBuilder.setView(dialogView)
        cancelAction = dialogView.findViewById(R.id.cancel_action)
        registerAction = dialogView.findViewById(R.id.register_action)
        cancelAction.setOnClickListener { dialogBuilder.dismiss() }
        registerAction.setOnClickListener {
            if (sharedPreferences.getissavecredential("credential", this)) {
                dialogB()
            } else {
                dialogA()
            }
            dialogBuilder.dismiss()
        }
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    private fun dialogA() {
        val dialogBuilder = android.app.AlertDialog.Builder(this, R.style.AlertDialog).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.registerpopup_a, null)
        dialogBuilder.setView(dialogView)
        val merchantIdEt = dialogView.findViewById<EditText>(R.id.merchantid_et_register)
        val merchantPassEt = dialogView.findViewById<EditText>(R.id.merchantpass_et_register)
        val submit = dialogView.findViewById<Button>(R.id.confirm)
        submit.setOnClickListener { v: View? ->
            val merchantId = merchantIdEt.text.toString()
            val merchantPass = merchantPassEt.text.toString()
            if (merchantId == "") {
                showToast("please add user Id first!")
            } else if (merchantPass == "") {
                showToast("please add user Password first!")
            } else {
                findMerchant(merchantId, merchantPass)
            }
            dialogBuilder.dismiss()
        }
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    private lateinit var etEmail: EditText
    private lateinit var etIpaddress: EditText
    private lateinit var dialogBBuilder: android.app.AlertDialog
    fun dialogB() {
        dialogBBuilder = android.app.AlertDialog.Builder(this, R.style.AlertDialog).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.registerpopup_b, null)
        etEmail = dialogView.findViewById(R.id.et_email2)
        etIpaddress = dialogView.findViewById(R.id.ip_address)
        dialogBBuilder.setCanceledOnTouchOutside(false)
        etEmail.visibility = View.VISIBLE
        if (sharedPreferences.getvalueofipaddress("ip", this) == "") {
            etIpaddress.visibility = View.VISIBLE
        } else {
            etIpaddress.visibility = View.GONE
        }
        dialogBBuilder.setView(dialogView)
        val confirm = dialogView.findViewById<Button>(R.id.confirmlink)
        val scanQRCode = dialogView.findViewById<Button>(R.id.btn_scanQR)
        scanQRCode.setOnClickListener {
            qrScan!!.initiateScan()
        }
        confirm.setOnClickListener {
            val refresh = etEmail.text.toString()
            val ipAddress = etIpaddress.text.toString()
            if (ipAddress.isNotEmpty()) {
                sharedPreferences.setvalueofipaddress(ipAddress, "ip", this)
            }
            if (sharedPreferences.getvalueofRefresh("refreshToken", this) == "") {
                if (refresh.isEmpty()) {
                    showToast("Enter refresh Token")
                } else if (sharedPreferences.getvalueofipaddress("ip", this) == "") {
                    showToast("Enter Ip Address")
                } else {
                    try {
                        getOTP(refresh)
                        dialogBBuilder.dismiss()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            } else {
                if (sharedPreferences.getvalueofipaddress("ip", this) == "") {
                    showToast("Enter Ip Adress")
                } else {
                    try {
                        getOTP(sharedPreferences.getvalueofRefresh("refreshToken", this))
                        dialogBBuilder.dismiss()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        dialogBBuilder.setView(dialogView)
        dialogBBuilder.show()
    }

    fun dialogC() {
        val dialogBuilder = android.app.AlertDialog.Builder(this, R.style.AlertDialog).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.registerpopup_c, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setCanceledOnTouchOutside(false)
        val codef2 = dialogView.findViewById<EditText>(R.id.code2fa)
        val confirm = dialogView.findViewById<Button>(R.id.confirm2fa)
        confirm.setOnClickListener { v: View? ->
            val code2faConfirm = codef2.text.toString()
            getToken(sharedPreferences.getvalueofRefresh("refreshToken", this), code2faConfirm)
            dialogBuilder.dismiss()
        }
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    fun dialog_Otp_Code(otp: String?) {
        val dialogBuilder = android.app.AlertDialog.Builder(this, R.style.AlertDialog).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.registerpopup_otp_code, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCanceledOnTouchOutside(false)
        val otpcode = dialogView.findViewById<TextView>(R.id.otpcode)
        otpcode.text = otp
        val next = dialogView.findViewById<TextView>(R.id.register_action_next)
        next.setOnClickListener { v: View? ->
            dialogC()
            dialogBuilder.dismiss()
        }
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    fun dialog_LockCheck() {
        val dialogBuilder = android.app.AlertDialog.Builder(this, R.style.AlertDialog).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.registerpopup_lockcheck, null)
        dialogBuilder.setView(dialogView)
        val action_ok = dialogView.findViewById<TextView>(R.id.action_ok)
        action_ok.setOnClickListener { v: View? ->

            dialogBuilder.dismiss()
            startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        }
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(true)
        dialogBuilder.setOnCancelListener { showOnBackAlert() }
        dialogBuilder.show()
    }

    @SuppressLint("SetTextI18n")
    fun dialog_GetInfo(`val`: Int, message: String?) {
        val dialogBuilder = AlertDialog.Builder(this, R.style.AlertDialog).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.getinfo_popup, null)
        dialogBuilder.setView(dialogView)
        val next = dialogView.findViewById<TextView>(R.id.getinfo_action)
        val viewText = dialogView.findViewById<TextView>(R.id.visual_text)
        if (`val` == 1) {
            next.text = "Close"
        }
        if (`val` == 3) {
            next.text = "Reconnect"
        }
        if (`val` == 2) {
            next.text = "Close"
        } else {
            next.text = "Next"
        }
        viewText.text = message
        next.setOnClickListener { v: View? ->
            if (`val` == 1) {
                dialogBuilder.dismiss()
            } else if (`val` == 2) {
                dialogBuilder.dismiss()
            } else if (`val` == 3) {
                createWebSocketClient()
                dialogBuilder.dismiss()
            } else {
                createWebSocketClient1()
                dialogBuilder.dismiss()
            }
        }
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    private fun findMerchant(id: String, pass: String) {
        confirmingProgressDialog!!.show()
        confirmingProgressDialog!!.setCancelable(false)
        confirmingProgressDialog!!.setCanceledOnTouchOutside(false)
        val paramObject = JsonObject()
        paramObject.addProperty("user_id", id)
        paramObject.addProperty("password", pass)
        val call: Call<MerchantLoginResp> =
            webservice.merchant_Loging(paramObject)
        call.enqueue(object : Callback<MerchantLoginResp?> {
            override fun onResponse(call: Call<MerchantLoginResp?>, response: Response<MerchantLoginResp?>) {
                confirmingProgressDialog.dismiss()
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.message == "successfully login") {
                            val merchantData = response.body()!!.merchantData
                            sessionService.setMerchantData(merchantData)
                            GlobalState.getInstance().setLattitude(merchantData.latitude)
                            GlobalState.getInstance().longitude = merchantData.longitude
                            currentMerchantData = merchantData
                            GlobalState.getInstance().merchant_id = id
                            sharedPreferences.setString(
                                currentMerchantData!!.ssh_password,
                                "sshkeypass",
                                this@MainEntryActivityNew
                            )
                            CustomSharedPreferences().setvalueofMerchantname(
                                id,
                                "merchant_name",
                                this@MainEntryActivityNew
                            )
                            CustomSharedPreferences().setvalueofMerchantpassword(
                                pass,
                                "merchant_pass",
                                this@MainEntryActivityNew
                            )
                            CustomSharedPreferences().setvalueofMerchantId(
                                merchantData.id,
                                "merchant_id",
                                this@MainEntryActivityNew
                            )
                            CustomSharedPreferences().setvalueofContainerAddress(
                                merchantData.container_address,
                                "container_address",
                                this@MainEntryActivityNew
                            )
                            CustomSharedPreferences().setvalueofLightningPort(
                                merchantData.lightning_port,
                                "lightning_port",
                                this@MainEntryActivityNew
                            )
                            CustomSharedPreferences().setvalueofPWSPort(
                                merchantData.pws_port,
                                "pws_port",
                                this@MainEntryActivityNew
                            )
                            CustomSharedPreferences().setvalueofMWSPort(
                                merchantData.mws_port,
                                "mws_port",
                                this@MainEntryActivityNew
                            )
                            CustomSharedPreferences().setvalue(
                                merchantData.accessToken,
                                "accessTokenLogin",
                                this@MainEntryActivityNew
                            )
                            CustomSharedPreferences().setvalue(
                                merchantData.refreshToken,
                                "refreshTokenLogin",
                                this@MainEntryActivityNew
                            )
                            val mwsCommad =
                                "ws://" + merchantData.container_address + ":" + merchantData.mws_port + "/SendCommands"
                            CustomSharedPreferences().setvalueofMWSCommand(
                                mwsCommad,
                                "mws_command",
                                this@MainEntryActivityNew
                            )
                            sharedPreferences.setvalueofipaddress(
                                merchantData.container_address + ":" + merchantData.mws_port,
                                "ip",
                                this@MainEntryActivityNew
                            )

                            //private final String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";

                            //gotoTestCase(merchantData);
                            if (sharedPreferences.getislogin("registered", this@MainEntryActivityNew)) {
                            } else {
                                if (isLoginMerchant) {
                                    if (sharedPreferences.getvalueofSocketCode(
                                            "socketcode",
                                            this@MainEntryActivityNew
                                        ) == 724
                                    ) {
                                        dialogC()
                                    } else if (sharedPreferences.getvalueofSocketCode(
                                            "socketcode",
                                            this@MainEntryActivityNew
                                        ) == 722
                                    ) {
                                        dialogC()
                                    } else {
                                        createWebSocketClient()
                                    }
                                } else {
                                    dialogB()
                                }
                            }
                        } else {
                            isConfirmMerchant = false
                            goAlertDialogwithOneBTn("Invalid Merchant ID!")
                        }
                    } else {
                        isConfirmMerchant = false
                        Log.e("Error:", response.toString())
                        goAlertDialogwithOneBTn("Server Error")
                    }
                } else {
                    isConfirmMerchant = false
                    Log.e("Error:", response.toString())
                    goAlertDialogwithOneBTn("Server Error")
                }
            }

            override fun onFailure(call: Call<MerchantLoginResp?>, t: Throwable) {
                isConfirmMerchant = false
                confirmingProgressDialog!!.dismiss()
                goAlertDialogwithOneBTn("Network Error")
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun goAlertDialogwithOneBTn(alertMessage: String) {
        val goAlertDialogwithOneBTnDialog: Dialog
        goAlertDialogwithOneBTnDialog = Dialog(this)
        goAlertDialogwithOneBTnDialog.setContentView(R.layout.alert_dialog_layout)
        goAlertDialogwithOneBTnDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        goAlertDialogwithOneBTnDialog.setCancelable(false)
        val alerttitleTv = goAlertDialogwithOneBTnDialog.findViewById<TextView>(R.id.alertTitle)
        val alertmessageTv = goAlertDialogwithOneBTnDialog.findViewById<TextView>(R.id.alertMessage)
        val yesbtn = goAlertDialogwithOneBTnDialog.findViewById<Button>(R.id.yesbtn)
        val nobtn = goAlertDialogwithOneBTnDialog.findViewById<Button>(R.id.nobtn)
        yesbtn.text = "OK"
        nobtn.text = ""
        alerttitleTv.text = ""
        alertmessageTv.text = alertMessage
        nobtn.visibility = View.GONE
        alerttitleTv.visibility = View.GONE
        yesbtn.setOnClickListener { v: View? ->
            goAlertDialogwithOneBTnDialog.dismiss()
            if (alertMessage == "Invalid Merchant ID!") {
                if (!sharedPreferences.getissavecredential("credential", this)) {
                    dialogA()
                }
            }
        }
        nobtn.setOnClickListener { v: View? -> goAlertDialogwithOneBTnDialog.dismiss() }
        goAlertDialogwithOneBTnDialog.show()
    }

    private fun createWebSocketClient() {
        Log.v(Companion.TAG, "createWebSocketClient: ")
        val uri: URI
        uri = try {
            // Connect to local host
            URI("ws://" + sharedPreferences.getvalueofipaddress("ip", this) + "/SendCommands")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }
        Log.v(Companion.TAG, "createWebSocketClient: $uri")
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen() {
                val token = sharedPreferences.getvalueofaccestoken("accessToken", this@MainEntryActivityNew)
                val json = "{\"token\" : \"$token\", \"commands\" : [\"ls\", \"ls -l\"] }"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocketClient!!.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                }
                Log.i("WebSocket", "Session is starting")
            }

            override fun onTextReceived(s: String) {
                Log.i("WebSocket", "Message received")
                sharedPreferences.setvalueofSocketCode(0, "socketcode", this@MainEntryActivityNew)
                if (s == "{\"code\":724,\"message\":\"Access token has expired, please request a new token\"}") {
                    try {
                        Log.v(Companion.TAG, "onTextReceived: $s")
                        val jsonObject = JSONObject(s)
                        code = jsonObject.getInt("code")
                        sharedPreferences.setvalueofSocketCode(code, "socketcode", this@MainEntryActivityNew)
                        if (code == 724) {
                            runOnUiThread { dialogC() }
                            webSocketClient!!.close()
                        } else if (code == 700) {
                            webSocketClient!!.close()
                        }
                    } catch (err: JSONException) {
                        Log.e(Companion.TAG, err.toString())
                    }
                } else if (s == "{\"code\":723,\"message\":\"Access token is invalid\"}") {
                    Log.v(Companion.TAG, "onTextReceived: $s")
                    runOnUiThread {
                        goTo2FaPasswordDialog(
                            sharedPreferences.getvalueofRefresh(
                                "refreshToken",
                                this@MainEntryActivityNew
                            )
                        )
                    }
                } else {
                    if (GlobalState.getInstance().login == true) {
                        runOnUiThread { createWebSocketClient1() }
                    } else {
                        runOnUiThread {
                            dialog_GetInfo(
                                0,
                                "Your node is now registered. The next time you log in you may do so using device based two-factor authentication."
                            )
                        }
                    }
                }
            }

            override fun onBinaryReceived(data: ByteArray) {}
            override fun onPingReceived(data: ByteArray) {}
            override fun onPongReceived(data: ByteArray) {}
            override fun onException(e: Exception) {
                println(e.message)
                runOnUiThread {
                    if (e.message == "Attempt to invoke virtual method 'boolean java.lang.Boolean.booleanValue()' on a null object reference") {
                        dialog_GetInfo(
                            0,
                            "Your node is now registered. The next time you log in you may do so using device based two-factor authentication."
                        )
                    } else {
                        dialog_GetInfo(2, e.message)
                    }
                }
            }

            override fun onCloseReceived() {
                Log.i("WebSocket", "Closed ")
                println("onCloseReceived")
            }
        }
        webSocketClient.setConnectTimeout(100000)
        webSocketClient.setReadTimeout(600000)
        webSocketClient.enableAutomaticReconnection(5000)
        webSocketClient.connect()
    }

    private fun createWebSocketClient1() {
        val uri: URI
        uri = try {
            URI("ws://" + sharedPreferences.getvalueofipaddress("ip", this) + "/SendCommands")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen() {
                val token = sharedPreferences.getvalueofaccestoken("accessToken", this@MainEntryActivityNew)
                val json = "{\"token\" : \"$token\", \"commands\" : [\"lightning-cli getinfo\"] }"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocketClient!!.send(obj.toString())
                } catch (t: Throwable) {
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                }
                Log.i("WebSocket", "Session is starting")
            }

            override fun onTextReceived(s: String) {
                Log.i("WebSocket", "Message received")
                try {
                    val jsonObject = JSONObject(s)
                    code1 = jsonObject.getString("id")
                    if (code1 == "") {
                        sharedPreferences.setvalueofconnectedSocket("", "socketconnected", this@MainEntryActivityNew)
                    } else {
                        sharedPreferences.setvalueofconnectedSocket(code1, "socketconnected", this@MainEntryActivityNew)
                        runOnUiThread {
                            val i = Intent(this@MainEntryActivityNew, HomeActivity::class.java)
                            startActivity(i)
                        }
                    }
                    if (code == 724) {
                        sharedPreferences.setvalueofSocketCode(code, "socketcode", this@MainEntryActivityNew)
                        webSocketClient!!.close()
                    }
                } catch (err: JSONException) {
                    Log.d("Error", err.toString())
                }
            }

            override fun onBinaryReceived(data: ByteArray) {}
            override fun onPingReceived(data: ByteArray) {}
            override fun onPongReceived(data: ByteArray) {}
            override fun onException(e: Exception) {
                println(e.message)
                runOnUiThread { dialog_GetInfo(2, e.message) }
            }

            override fun onCloseReceived() {
                Log.i("WebSocket", "Closed ")
                println("onCloseReceived")
            }
        }
        webSocketClient.setConnectTimeout(100000)
        webSocketClient.setReadTimeout(600000)
        webSocketClient.enableAutomaticReconnection(5000)
        webSocketClient.connect()
    }

    private val expireToken: Unit
        private get() {
            val call: Call<get_session_response> = webservice.get_session("merchant", "haiww82uuw92iiwu292isk")
            call.enqueue(object : Callback<get_session_response?> {
                override fun onResponse(call: Call<get_session_response?>, response: Response<get_session_response?>) {
                    if (response.body() != null) {
                        val loginresponse = response.body()
                        if (loginresponse!!.session_token != null) {
                            CustomSharedPreferences().setvalueofExpierTime(
                                loginresponse.session_token.toInt(),
                                this@MainEntryActivityNew
                            )
                        } else {
                            showToast("Response empty")
                            CustomSharedPreferences().setvalueofExpierTime(300, this@MainEntryActivityNew)
                        }
                    } else {
                        CustomSharedPreferences().setvalueofExpierTime(300, this@MainEntryActivityNew)
                    }
                }

                override fun onFailure(call: Call<get_session_response?>, t: Throwable) {
                    Log.e("get-funding-nodes:", t.message!!)
                    CustomSharedPreferences().setvalueofExpierTime(300, this@MainEntryActivityNew)
                }
            })
        }

    private fun goTo2FaPasswordDialog(accessToken: String) {
        val enter2FaPassDialog = Dialog(this)
        enter2FaPassDialog.setContentView(R.layout.merchat_twofa_pass_lay)
        enter2FaPassDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        enter2FaPassDialog.setCancelable(false)
        val et2faPass = enter2FaPassDialog.findViewById<EditText>(R.id.taskEditText)
        val btnConfirm = enter2FaPassDialog.findViewById<Button>(R.id.btn_confirm)
        val btnCancel = enter2FaPassDialog.findViewById<Button>(R.id.btn_cancel)
        val ivBack = enter2FaPassDialog.findViewById<ImageView>(R.id.iv_back_invoice)
        ivBack.setOnClickListener { enter2FaPassDialog.dismiss() }
        btnConfirm.setOnClickListener {
            val task = et2faPass.text.toString()
            if (task.isEmpty()) {
                showToast("Enter 2FA Password")
            } else {
                enter2FaPassDialog.dismiss()
                confirmingProgressDialog!!.show()
                confirmingProgressDialog!!.setCancelable(false)
                confirmingProgressDialog!!.setCanceledOnTouchOutside(false)
                getSessionToken(accessToken, task)
            }
        }
        btnCancel.setOnClickListener { v: View? -> enter2FaPassDialog.dismiss() }
        enter2FaPassDialog.show()
    }

    private fun getSessionToken(accessToken: String, twoFaCode: String) {
        val call: Call<get_session_response> = webservice.get_session("merchant", "haiww82uuw92iiwu292isk")
        call.enqueue(object : Callback<get_session_response?> {
            override fun onResponse(call: Call<get_session_response?>, response: Response<get_session_response?>) {
                if (response.body() != null) {
                    val loginresponse = response.body()
                    if (loginresponse!!.session_token.toInt() != -1) {
                        CustomSharedPreferences().setvalueofExpierTime(
                            loginresponse.session_token.toInt(),
                            this@MainEntryActivityNew
                        )
                        val RefToken =
                            CustomSharedPreferences().getvalueofRefresh("refreshToken", this@MainEntryActivityNew)
                        getToken(RefToken, twoFaCode)
                    } else {
                        confirmingProgressDialog.dismiss()
                        showToast("Response empty")
                    }
                } else {
                    confirmingProgressDialog!!.dismiss()
                    try {
                        if (response.errorBody() != null) {
                            showToast(response.errorBody()!!.string())
                        }
                    } catch (e: IOException) {
                        showToast(e.message)
                    }
                }
            }

            override fun onFailure(call: Call<get_session_response?>, t: Throwable) {
                Log.e("get-funding-nodes:", t.message!!)
                confirmingProgressDialog!!.dismiss()
                showToast(t.message)
            }
        })
    }

    override fun onBackPressed() {
        showOnBackAlert()
    }

    private fun showOnBackAlert() {
        Dialog(this).apply {
            setContentView(R.layout.alert_dialog_layout)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            findViewById<TextView>(R.id.alertTitle).apply {
                text = ""
                visibility = View.GONE
            }
            findViewById<TextView>(R.id.alertMessage).text = "Are you sure you want to exit?"
            findViewById<Button>(R.id.yesbtn).apply {
                text = "Yes"
                setOnClickListener {
                    dismiss()
                    finishAffinity()
                    finish()
                }
            }
            findViewById<Button>(R.id.nobtn).apply {
                text = "No"
                setOnClickListener {
                    dismiss()
                }
            }
            setCancelable(true)
        }.show()
    }

    companion object {
        private val TAG = HomeActivity::class.java.name
    }
}