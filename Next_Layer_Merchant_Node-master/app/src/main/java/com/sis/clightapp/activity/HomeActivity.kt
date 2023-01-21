package com.sis.clightapp.activity

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.sis.clightapp.Interface.ApiClientStartStop
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.R
import com.sis.clightapp.fragments.shared.ExitDialogFragment
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData
import com.sis.clightapp.model.REST.ServerStartStop.Node.NodeResp
import com.sis.clightapp.util.AppConstants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class HomeActivity : BaseActivity() {
    private lateinit var opAdmin: Button
    private lateinit var opMerchant: Button
    private lateinit var opCheckOut: Button
    private lateinit var setTextWithSpan: TextView
    var TAG = "CLighting App"
    var recallTime = 0
    var code = 0

    var isThorConfirmed = false
    var thorNodeStatusImg: ImageView? = null

    var checkStatusPD: ProgressDialog? = null
    var isConfirmMerchant = false
    var currentMerchantData: MerchantData? = null
    var startServerPD: ProgressDialog? = null
    var stopServerPD: ProgressDialog? = null
    var wait20SecPD: ProgressDialog? = null
    var bundle: Bundle? = null
    var isFromLogin = false
    override fun onBackPressed() {
        //   super.onBackPressed();
        ExitDialogFragment() {
            startActivity(Intent(this@HomeActivity, MainEntryActivityNew::class.java))
        }.show(supportFragmentManager, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recallTime = 0
        checkStatusPD = ProgressDialog(this)
        checkStatusPD!!.setMessage("Loading...")
        checkStatusPD!!.setCancelable(false)
        startServerPD = ProgressDialog(this)
        startServerPD!!.setMessage("Loading...")
        startServerPD!!.setCancelable(false)
        stopServerPD = ProgressDialog(this)
        stopServerPD!!.setMessage("Loading...")
        stopServerPD!!.setCancelable(false)
        wait20SecPD = ProgressDialog(this)
        wait20SecPD!!.setMessage("Loading...")
        wait20SecPD!!.setCancelable(false)
        currentMerchantData = sessionService.getMerchantData()
        if (currentMerchantData != null) {
            isConfirmMerchant = true
            isThorConfirmed = true
        } else {
            isThorConfirmed = false
            isConfirmMerchant = false
        }
        dialog = ProgressDialog(this)
        dialog.setMessage("Connecting...")
        setTextWithSpan = findViewById(R.id.imageView3)
        val boldStyle = StyleSpan(Typeface.BOLD)
        setTextWithSpan(
            setTextWithSpan,
            getString(R.string.welcome_text),
            getString(R.string.welcome_text_bold),
            boldStyle
        )
        thorNodeStatusImg = findViewById(R.id.thor_status_main)
        updateStatusBox(0, true)

        opAdmin = findViewById(R.id.optionAdmin)
        opMerchant = findViewById(R.id.optionMerchant)
        opCheckOut = findViewById(R.id.optionCheckout)
        val iin = intent
        bundle = iin.extras
        if (bundle != null) {
            isFromLogin = bundle!!.getBoolean("isFromLogin")
        }
        opAdmin.setOnClickListener {
            Log.e(TAG, "Admin Mode Selected")
            val i = Intent(applicationContext, LoginActivity::class.java)
            i.putExtra("role", "admin")
            startActivity(i)
            finish()
        }
        opMerchant.setOnClickListener {
            Log.e(TAG, "Merchant Mode Selected")
            val i = Intent(applicationContext, LoginActivity::class.java)
            i.putExtra("role", "merchant")
            startActivity(i)
            finish()
        }
        opCheckOut.setOnClickListener {
            Log.e(TAG, "Selecting CheckOut Mode")
            val i = Intent(applicationContext, LoginActivity::class.java)
            i.putExtra("role", "checkout")
            startActivity(i)
            finish()
        }
        if (!isFromLogin) {
            val date = Date(System.currentTimeMillis())
            val lastDatTime = sharedPreferences.getsession(LASTDATE, applicationContext)
            val lastdatecalendar = Calendar.getInstance()
            lastdatecalendar.timeInMillis = lastDatTime
            val lastdateD = lastdatecalendar.time
            Log.e("Before:", lastdateD.toString())
            val currentDateTime = date.time
            Log.e("Current:", date.toString())
            val diff = currentDateTime - lastDatTime
            // Calculate difference in minutes
            val diffMinutes = diff / (60 * 1000)
            if (diffMinutes < 240) {
                if (sharedPreferences.getBoolean(ISALLSERVERUP, this)) {
                    updateStatusBox(1, sharedPreferences.getThorStatus(THORSTATUS, this))
                    updateStatusBox(2, sharedPreferences.getLightningStatus(LIGHTNINGSTATUS, this))
                    updateStatusBox(3, sharedPreferences.getBitcoinStatus(BITCOINSTATUS, this))
                } else {
                    //  checkAppFlow();
                    updateStatusBox(1, sharedPreferences.getThorStatus(THORSTATUS, this))
                    updateStatusBox(2, sharedPreferences.getLightningStatus(LIGHTNINGSTATUS, this))
                    updateStatusBox(3, sharedPreferences.getBitcoinStatus(BITCOINSTATUS, this))
                }
            } else {
                // checkAppFlow();
                updateStatusBox(1, sharedPreferences.getThorStatus(THORSTATUS, this))
                updateStatusBox(2, sharedPreferences.getLightningStatus(LIGHTNINGSTATUS, this))
                updateStatusBox(3, sharedPreferences.getBitcoinStatus(BITCOINSTATUS, this))
            }
            // showToast("User Not Login");
        } else {
            updateStatusBox(1, sharedPreferences.getThorStatus(THORSTATUS, this))
            updateStatusBox(2, sharedPreferences.getLightningStatus(LIGHTNINGSTATUS, this))
            updateStatusBox(3, sharedPreferences.getBitcoinStatus(BITCOINSTATUS, this))
        }
    }

    //TODO:Point No 6
    private fun checkAppFlow() {
        //TODO:Set thor button display green colour
//        thorNodeStatusImg.setImageDrawable(getDrawable(R.drawable.greenstatus));
        updateStatusBox(1, true)
        updateStatusBox(2, false)
        updateStatusBox(3, false)
        //TODO:check status of bitcoin node
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData!!.ssh_ip_port != null && currentMerchantData!!.ssh_password != null && currentMerchantData!!.ssh_username != null && currentMerchantData!!.rpc_username != null && currentMerchantData!!.rpc_password != null) {
                    val type = "status"
                    val ssh = currentMerchantData!!.ssh_ip_port
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                val sh = ssh.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val host = sh[0]
                                val port = sh[1]
                                if (currentMerchantData!!.isIs_own_bitcoin) {
                                    //TODO:When  Own BTC
                                    val sshPass = currentMerchantData!!.ssh_password
                                    val sshUsername = currentMerchantData!!.ssh_username
                                    val rpcUserName = currentMerchantData!!.rpc_username
                                    val rpcPassword = currentMerchantData!!.rpc_password
                                    callBitcoinNodeStatusCheck(
                                        type,
                                        host,
                                        port,
                                        sshUsername,
                                        sshPass,
                                        rpcUserName,
                                        rpcPassword
                                    )
                                } else {
                                    //TODO:When Not Own BTC
                                    val sshPass = currentMerchantData!!.ssh_password
                                    val sshUsername = currentMerchantData!!.ssh_username
                                    val rpcUserName = currentMerchantData!!.rpc_username
                                    val rpcPassword = currentMerchantData!!.rpc_password
                                    goTOtheNotOwnBTC(host, port, sshUsername, sshPass, rpcUserName, rpcPassword)
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "")
                            }
                        } else {
                            //TODO
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "")
                        }
                    } else {
                        //TODO
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "")
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "")
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
        }
    }

    //TODO:    :Main Case :   Case to Not Own Bitcoin
    private fun goTOtheNotOwnBTC(
        host: String,
        port: String,
        sshUsername: String,
        sshPass: String,
        rpcUserName: String,
        rpcPassword: String
    ) {
        //Step 1) App assumes proper remote bitcoin node is up and display green color (DGC).
        updateStatusBox(3, true)
        //Step 2) A restart is performed on the lightning node and then a status check.
        val type = "start"
        reStartLightningStep(type, host, port, sshUsername, sshPass, rpcUserName, rpcPassword)
    }

    private fun reStartLightningStep(
        type: String,
        host: String,
        port: String,
        sshUsername: String,
        sshPass: String,
        rpcUserName: String,
        rpcPassword: String
    ) {
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File? = null
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
        }
        if (yourFile!!.exists()) {
            startServerPD!!.show()
            var sshkeypasval = sharedPreferences.getString("sshkeypass", this)
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = sshkeypasval.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val type2: RequestBody = type.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val host2: RequestBody = host.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val port2: RequestBody = port.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshUsername2: RequestBody = sshUsername.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshPass2: RequestBody = sshPass.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val rpcUserName2: RequestBody = rpcUserName.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val rpcPassword2: RequestBody = rpcPassword.toRequestBody(("text/plain".toMediaTypeOrNull()))
            var itemImageFileMPBody: MultipartBody.Part? = null
            val photo_id: RequestBody = yourFile.asRequestBody(("".toMediaTypeOrNull()))
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> = ApiClientStartStop.getRetrofit().create(Webservice::class.java)
                .startLightningServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody)
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200) {
                                val handler = Handler()
                                handler.postDelayed({ // yourMethod();
                                    startServerPD!!.dismiss()
                                    reCheckLightningSatus(
                                        type,
                                        host,
                                        port,
                                        sshUsername,
                                        sshPass,
                                        rpcUserName,
                                        rpcPassword
                                    )
                                }, AppConstants.TIMEFORWAITLN2)
                            } else {
                                updateStatusBox(3, false)
                                updateStatusBox(2, false)
                                startServerPD!!.dismiss()
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "")
                            startServerPD!!.dismiss()
                        }
                    } else {
                        val resp = response.body()
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "")
                        startServerPD!!.dismiss()
                    }
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "")
                    startServerPD!!.dismiss()
                }
            })
        } else {
            showToast("SSH is Missing")
        }
    }

    private fun reCheckLightningSatus(
        type: String,
        host: String,
        port: String,
        sshUsername: String,
        sshPass: String,
        rpcUserName: String,
        rpcPassword: String
    ) {
        checkStatusPD!!.show()
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File? = null
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
        }
        if (yourFile!!.exists()) {
            var sshkeypasval = sharedPreferences.getString("sshkeypass", this)
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = sshkeypasval.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val type2: RequestBody = type.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val host2: RequestBody = host.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val port2: RequestBody = port.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshUsername2: RequestBody = sshUsername.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshPass2: RequestBody = sshPass.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val rpcUserName2: RequestBody = rpcUserName.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val rpcPassword2: RequestBody = rpcPassword.toRequestBody(("text/plain".toMediaTypeOrNull()))
            var itemImageFileMPBody: MultipartBody.Part? = null
            val photo_id: RequestBody = yourFile.asRequestBody(("".toMediaTypeOrNull()))
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> = ApiClientStartStop.getRetrofit().create(Webservice::class.java)
                .checkLightningNodeServerStatus2(
                    sshkeypass,
                    host2,
                    port2,
                    sshUsername2,
                    itemImageFileMPBody,
                    rpcUserName2,
                    rpcPassword2
                )
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200 && resp.message == "up") {
                                updateStatusBox(2, true)
                                updateStatusBox(3, true)
                                checkStatusPD!!.dismiss()
                            } else if (resp.code == 200 && resp.message == "down") {
                                updateStatusBox(2, false)
                                updateStatusBox(3, false)
                                checkStatusPD!!.dismiss()
                                goAlertDialogwithOneBTn(
                                    1,
                                    "",
                                    "Please check the status of your remote BTC node and manually restart your Lightning Node.",
                                    "OK",
                                    ""
                                )
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "")
                            checkStatusPD!!.dismiss()
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "")
                        checkStatusPD!!.dismiss()
                    }
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "")
                    checkStatusPD!!.dismiss()
                }
            })
        } else {
            showToast("SSH is Missing")
            checkStatusPD!!.dismiss()
        }
    }

    //TODO:    :Main Case :   Case To OWn Bitcoin
    private fun callBitcoinNodeStatusCheck(
        type: String,
        host: String,
        port: String,
        sshUsername: String,
        sshPass: String,
        rpcUserName: String,
        rpcPassword: String
    ) {
        checkStatusPD!!.show()
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File? = null
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
        }
        if (yourFile!!.exists()) {
            var sshkeypasval = sharedPreferences.getString("sshkeypass", this)
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), sshkeypasval)
            val type2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), type)
            val host2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), host)
            val port2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), port)
            val sshUsername2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), sshUsername)
            val sshPass2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), sshPass)
            val rpcUserName2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), rpcUserName)
            val rpcPassword2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), rpcPassword)
            var itemImageFileMPBody: MultipartBody.Part? = null
            val photo_id: RequestBody = RequestBody.create(("".toMediaTypeOrNull()), yourFile)
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> = ApiClientStartStop.getRetrofit().create(Webservice::class.java)
                .checkBitcoinNodeServerStatus2(
                    sshkeypass,
                    host2,
                    port2,
                    sshUsername2,
                    itemImageFileMPBody,
                    rpcUserName2,
                    rpcPassword2
                )
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200 && resp.message == "up") {
                                goTOBitcoinUpCase()
                            } else if (resp.code == 200 && resp.message == "down") {
                                goTOBitcoinDownCase()
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "")
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "")
                    }
                    checkStatusPD!!.dismiss()
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "")
                    checkStatusPD!!.dismiss()
                }
            })
        } else {
            showToast("SSH Is Misiing")
        }
    }

    //TODO: GO TO THE BITCOIN DOWN CASE
    private fun goTOBitcoinDownCase() {
        gotoRestartBitcoinApi()
    }

    private fun gotoRestartBitcoinApi() {
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData!!.ssh_ip_port != null && currentMerchantData!!.ssh_password != null && currentMerchantData!!.ssh_username != null) {
                    val type = "start"
                    val ssh = currentMerchantData!!.ssh_ip_port
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                val sh = ssh.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val host = sh[0]
                                val port = sh[1]
                                if (currentMerchantData!!.isIs_own_bitcoin) {
                                    val sshPass = currentMerchantData!!.ssh_password
                                    val sshUsername = currentMerchantData!!.ssh_username
                                    startBitcoinServer(type, host, port, sshUsername, sshPass)
                                } else {
                                    goAlertDialogwithOneBTn(1, "", "No Own Bitcoin Node!!", "OK", "")
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "")
                            }
                        } else {
                            //TODO
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "")
                        }
                    } else {
                        //TODO
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "")
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "")
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
        }
    }

    //TODO:START AND STOP Bitcoin SERVER APIs
    private fun startBitcoinServer(type: String, host: String, port: String, sshUsername: String, sshPass: String) {
        startServerPD!!.show()
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File? = null
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
        }
        if (yourFile!!.exists()) {
            var sshkeypasval = sharedPreferences.getString("sshkeypass", this)
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), sshkeypasval)
            val type2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), type)
            val host2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), host)
            val port2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), port)
            val sshUsername2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), sshUsername)
            val sshPass2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), sshPass)
            var itemImageFileMPBody: MultipartBody.Part? = null
            val photo_id: RequestBody = RequestBody.create(("".toMediaTypeOrNull()), yourFile)
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> = ApiClientStartStop.getRetrofit().create(Webservice::class.java)
                .startBitcoinServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody)
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200) {
                                // updateResultBitcoinStatus("Bitcoin: "+resp.getMessage());
                                val handler = Handler()
                                handler.postDelayed({ // yourMethod();
                                    startServerPD!!.dismiss()
                                    goToForStartBitcoinDone()
                                }, AppConstants.TIMEFORWAITLN2)
                            } else {
                                goToBitcoinNodeNotDone()
                                startServerPD!!.dismiss()
                                //  updateResultBitcoinStatus(resp.getMessage());
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "")
                            startServerPD!!.dismiss()
                        }
                    } else {
                        val resp = response.body()
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "")
                        startServerPD!!.dismiss()
                    }
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "")
                    startServerPD!!.dismiss()
                }
            })
        } else {
            showToast("SSH Is Misiing")
        }
    }

    private fun goToForStartBitcoinDone() {
        //TODO: AFter @0 seconds wait!!
        wait20SecPD!!.show()
        Handler().postDelayed({
            wait20SecPD!!.dismiss()
            goToStartLightningNode()
        }, AppConstants.TIMEFORWAITLN)
    }

    private fun goToStartLightningNode() {
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData!!.ssh_ip_port != null && currentMerchantData!!.ssh_password != null && currentMerchantData!!.ssh_username != null && currentMerchantData!!.rpc_username != null && currentMerchantData!!.rpc_password != null) {
                    val type = "start"
                    val ssh = currentMerchantData!!.ssh_ip_port
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                val sh = ssh.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val host = sh[0]
                                val port = sh[1]
                                val sshPass = currentMerchantData!!.ssh_password
                                val sshUsername = currentMerchantData!!.ssh_username
                                startLightningServer2(type, host, port, sshUsername, sshPass)
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "")
                            }
                        } else {
                            //TODO
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "")
                        }
                    } else {
                        //TODO
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "")
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "")
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
        }
    }

    //TODO:START  Lightnning SERVER API
    private fun startLightningServer2(type: String, host: String, port: String, sshUsername: String, sshPass: String) {
        startServerPD!!.show()
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File? = null
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
        }
        if (yourFile!!.exists()) {
            var sshkeypasval = sharedPreferences.getString("sshkeypass", this)
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), sshkeypasval)
            val type2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), type)
            val host2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), host)
            val port2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), port)
            val sshUsername2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), sshUsername)
            val sshPass2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), sshPass)
            var itemImageFileMPBody: MultipartBody.Part? = null
            val photo_id: RequestBody = RequestBody.create(("".toMediaTypeOrNull()), yourFile)
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> = ApiClientStartStop.getRetrofit().create(Webservice::class.java)
                .startLightningServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody)
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200) {
                                updateStatusBox(2, true)
                            } else {
                                updateStatusBox(2, false)
                                goToRestartLighntningNodefor9Cycles()
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "")
                        }
                    } else {
                        val resp = response.body()
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "")
                    }
                    startServerPD!!.dismiss()
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "")
                    startServerPD!!.dismiss()
                }
            })
        } else {
            showToast("SSH Is Misiing")
        }
    }

    private fun goToRestartLighntningNodefor9Cycles() {
        //TODO: AFter @0 seconds wait!!
        wait20SecPD!!.show()
        Handler().postDelayed({
            wait20SecPD!!.dismiss()
            goRestartLightningApi9()
        }, AppConstants.TIMEFORWAITLN)
    }

    private fun goRestartLightningApi9() {
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData!!.ssh_ip_port != null && currentMerchantData!!.ssh_password != null && currentMerchantData!!.ssh_username != null && currentMerchantData!!.rpc_username != null && currentMerchantData!!.rpc_password != null) {
                    val type = "status"
                    val ssh = currentMerchantData!!.ssh_ip_port
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                val sh = ssh.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val host = sh[0]
                                val port = sh[1]
                                if (currentMerchantData!!.isIs_own_bitcoin) {
                                    val sshPass = currentMerchantData!!.ssh_password
                                    val sshUsername = currentMerchantData!!.ssh_username
                                    val rpcUserName = currentMerchantData!!.rpc_username
                                    val rpcPassword = currentMerchantData!!.rpc_password
                                    startLightningNode9(type, host, port, sshUsername, sshPass)
                                } else {
                                    goAlertDialogwithOneBTn(1, "", "No Own Bitcoin Node!!", "OK", "")
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "")
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "")
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "")
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "")
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
        }
    }

    private fun startLightningNode9(type: String, host: String, port: String, sshUsername: String, sshPass: String) {
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File? = null
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
        }
        if (yourFile!!.exists()) {
            startServerPD!!.show()
            var sshkeypasval = sharedPreferences.getString("sshkeypass", this)
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = sshkeypasval.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val type2: RequestBody = type.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val host2: RequestBody = host.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val port2: RequestBody = port.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshUsername2: RequestBody = sshUsername.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshPass2: RequestBody = sshPass.toRequestBody(("text/plain".toMediaTypeOrNull()))
            var itemImageFileMPBody: MultipartBody.Part? = null
            val photo_id: RequestBody = yourFile.asRequestBody(("".toMediaTypeOrNull()))
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> = ApiClientStartStop.getRetrofit().create(Webservice::class.java)
                .startLightningServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody)
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200) {
                                val handler = Handler()
                                handler.postDelayed({ // yourMethod();
                                    updateStatusBox(2, true)
                                    startServerPD!!.dismiss()
                                    checkTheLightningNodeStatus9()
                                }, AppConstants.TIMEFORWAITLN2) //40 seconds seconds

//                            updateResultLightningStatus("Lightnning: "+resp.getMessage());
                            } else {
                                updateStatusBox(2, false)
                                //                            updateResultLightningStatus(resp.getMessage());
                                startServerPD!!.dismiss()
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "")
                            startServerPD!!.dismiss()
                        }
                    } else {
                        val resp = response.body()
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "")
                        startServerPD!!.dismiss()
                    }
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "")
                    startServerPD!!.dismiss()
                }
            })
        } else {
            showToast("SSH Is Misiing")
        }
    }

    private fun checkTheLightningNodeStatus9() {
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData!!.ssh_ip_port != null && currentMerchantData!!.ssh_password != null && currentMerchantData!!.ssh_username != null && currentMerchantData!!.rpc_username != null && currentMerchantData!!.rpc_password != null) {
                    val type = "status"
                    val ssh = currentMerchantData!!.ssh_ip_port
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                val sh = ssh.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val host = sh[0]
                                val port = sh[1]
                                if (currentMerchantData!!.isIs_own_bitcoin) {
                                    val sshPass = currentMerchantData!!.ssh_password
                                    val sshUsername = currentMerchantData!!.ssh_username
                                    val rpcUserName = currentMerchantData!!.rpc_username
                                    val rpcPassword = currentMerchantData!!.rpc_password
                                    callLightningNodeStatusCheck9(
                                        type,
                                        host,
                                        port,
                                        sshUsername,
                                        sshPass,
                                        rpcUserName,
                                        rpcPassword
                                    )
                                } else {
                                    goAlertDialogwithOneBTn(1, "", "No Own Bitcoin Node!!", "OK", "")
                                }
                            } else {
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "")
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "")
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "")
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "")
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
        }
    }

    private fun callLightningNodeStatusCheck9(
        type: String,
        host: String,
        port: String,
        sshUsername: String,
        sshPass: String,
        rpcUserName: String,
        rpcPassword: String
    ) {
        checkStatusPD!!.show()
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File? = null
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
        }
        if (yourFile!!.exists()) {
            var sshkeypasval = sharedPreferences.getString("sshkeypass", this)
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = sshkeypasval.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val type2: RequestBody = type.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val host2: RequestBody = host.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val port2: RequestBody = port.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshUsername2: RequestBody = sshUsername.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshPass2: RequestBody = sshPass.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val rpcUserName2: RequestBody = rpcUserName.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val rpcPassword2: RequestBody = rpcPassword.toRequestBody(("text/plain".toMediaTypeOrNull()))
            var itemImageFileMPBody: MultipartBody.Part? = null
            val photo_id: RequestBody = RequestBody.create(("".toMediaTypeOrNull()), yourFile)
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> = ApiClientStartStop.getRetrofit().create(Webservice::class.java)
                .checkLightningNodeServerStatus2(
                    sshkeypass,
                    host2,
                    port2,
                    sshUsername2,
                    itemImageFileMPBody,
                    rpcUserName2,
                    rpcPassword2
                )
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200 && resp.message == "up") {
                                updateStatusBox(2, true)
                            } else if (resp.code == 200 && resp.message == "down") {
                                updateStatusBox(2, false)
                                goTowaitandRecall()
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "")
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "")
                    }
                    checkStatusPD!!.dismiss()
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "")
                    checkStatusPD!!.dismiss()
                }
            })
        } else {
            showToast("SSH Is Misiing")
        }
    }

    private fun goTowaitandRecall() {
        if (recallTime > 8) {
            goAlertDialogwithOneBTn(1, "", "Please Restart Lightning Node Manually!", "OK", "")
        } else {
            recallTime = recallTime + 1
            wait20SecPD!!.show()
            Handler().postDelayed({
                wait20SecPD!!.dismiss()
                goRestartLightningApi9()
            }, AppConstants.TIMEFORWAITLN)
        }
    }

    private fun goToBitcoinNodeNotDone() {
        goAlertDialogwithOneBTn(1, "", "Bitcoin Is Not Start Succefully!!!", "OK", "")
    }

    //TODO: GO TO THE BITCOIN UP CASE
    private fun goTOBitcoinUpCase() {
        updateStatusBox(3, true)
        callRestartLightningNodeAPI()
    }

    private fun callRestartLightningNodeAPI() {
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData!!.ssh_ip_port != null && currentMerchantData!!.ssh_password != null && currentMerchantData!!.ssh_username != null && currentMerchantData!!.rpc_username != null && currentMerchantData!!.rpc_password != null) {
                    val type = "start"
                    val ssh = currentMerchantData!!.ssh_ip_port
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                val sh = ssh.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val host = sh[0]
                                val port = sh[1]
                                val sshPass = currentMerchantData!!.ssh_password
                                val sshUsername = currentMerchantData!!.ssh_username
                                startLightningServer(type, host, port, sshUsername, sshPass)
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "")
                            }
                        } else {
                            //TODO
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "")
                        }
                    } else {
                        //TODO
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "")
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "")
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
        }
    }

    //TODO:START  Lightnning SERVER APIs
    private fun startLightningServer(type: String, host: String, port: String, sshUsername: String, sshPass: String) {
        startServerPD!!.show()
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File? = null
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
        }
        if (yourFile!!.exists()) {
            var sshkeypasval = sharedPreferences.getString("sshkeypass", this)
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = sshkeypasval.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val type2: RequestBody = type.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val host2: RequestBody = host.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val port2: RequestBody = port.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshUsername2: RequestBody = sshUsername.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshPass2: RequestBody = sshPass.toRequestBody(("text/plain".toMediaTypeOrNull()))
            var itemImageFileMPBody: MultipartBody.Part? = null
            val photo_id: RequestBody = yourFile.asRequestBody(("".toMediaTypeOrNull()))
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> = ApiClientStartStop.getRetrofit().create(Webservice::class.java)
                .startLightningServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody)
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200) {
                                val handler = Handler()
                                handler.postDelayed({ // yourMethod();
                                    updateStatusBox(2, true)
                                    startServerPD!!.dismiss()
                                    checkTheLightningNodeStatus()
                                }, AppConstants.TIMEFORWAITLN2) //40 seconds seconds

//                            updateResultLightningStatus("Lightnning: "+resp.getMessage());
                            } else {
                                startServerPD!!.dismiss()
                                updateStatusBox(2, false)
                                goAlertDialogwithOneBTn(1, "", "Please Restart Lightning Node Manually!", "OK", "")
                            }
                        } else {
                            startServerPD!!.dismiss()
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "")
                        }
                    } else {
                        startServerPD!!.dismiss()
                        val resp = response.body()
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "")
                    }
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "")
                    startServerPD!!.dismiss()
                }
            })
        } else {
            showToast("SSH Is Misiing")
        }
    }

    private fun checkTheLightningNodeStatus() {
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData!!.ssh_ip_port != null && currentMerchantData!!.ssh_password != null && currentMerchantData!!.ssh_username != null && currentMerchantData!!.rpc_username != null && currentMerchantData!!.rpc_password != null) {
                    val type = "status"
                    val ssh = currentMerchantData!!.ssh_ip_port
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                val sh = ssh.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val host = sh[0]
                                val port = sh[1]
                                if (currentMerchantData!!.isIs_own_bitcoin) {
                                    val sshPass = currentMerchantData!!.ssh_password
                                    val sshUsername = currentMerchantData!!.ssh_username
                                    val rpcUserName = currentMerchantData!!.rpc_username
                                    val rpcPassword = currentMerchantData!!.rpc_password
                                    callLightningNodeStatusCheck(
                                        type,
                                        host,
                                        port,
                                        sshUsername,
                                        sshPass,
                                        rpcUserName,
                                        rpcPassword
                                    )
                                } else {
                                    goAlertDialogwithOneBTn(1, "", "No Own Bitcoin Node!!", "OK", "")
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "")
                            }
                        } else {
                            //TODO
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "")
                        }
                    } else {
                        //TODO
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "")
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "")
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
        }
    }

    //TODO: Lightning Status Check API
    private fun callLightningNodeStatusCheck(
        type: String,
        host: String,
        port: String,
        sshUsername: String,
        sshPass: String,
        rpcUserName: String,
        rpcPassword: String
    ) {
        checkStatusPD!!.show()
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File? = null
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
        }
        if (yourFile!!.exists()) {
            var sshkeypasval = sharedPreferences.getString("sshkeypass", this)
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), sshkeypasval)
            val type2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), type)
            val host2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), host)
            val port2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), port)
            val sshUsername2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), sshUsername)
            val sshPass2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), sshPass)
            val rpcUserName2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), rpcUserName)
            val rpcPassword2: RequestBody = RequestBody.create(("text/plain".toMediaTypeOrNull()), rpcPassword)
            var itemImageFileMPBody: MultipartBody.Part? = null
            val photo_id: RequestBody = RequestBody.create(("".toMediaTypeOrNull()), yourFile)
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> = ApiClientStartStop.getRetrofit().create(Webservice::class.java)
                .checkLightningNodeServerStatus2(
                    sshkeypass,
                    host2,
                    port2,
                    sshUsername2,
                    itemImageFileMPBody,
                    rpcUserName2,
                    rpcPassword2
                )
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200 && resp.message == "up") {
                                goToLightningUpCase()
                            } else if (resp.code == 200 && resp.message == "down") {
                                goToLightningDownCase()
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "")
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "")
                    }
                    checkStatusPD!!.dismiss()
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "")
                    checkStatusPD!!.dismiss()
                }
            })
        } else {
            showToast("SSH Is Misiing")
        }
    }

    private fun goToLightningUpCase() {
        updateStatusBox(2, true)
    }

    private fun goToLightningDownCase() {
        updateStatusBox(2, false)
        goAlertDialogwithOneBTn(1, "", "Please Restart Lightning Node Manually!", "OK", "")
    }

    //TODO: Update the Status Box
    private fun updateStatusBox(i: Int, b: Boolean) {
//        switch (i) {
//            case 1:
//                if (b) {
//                    sharedPreferences.setBoolean(true, ISALLSERVERUP, this);
//                    isThorConfirmed = true;
//                    sharedPreferences.setThorStatus(true, THORSTATUS, this);
//                    thorNodeStatusImg.setImageDrawable(getDrawable(R.drawable.greenstatus));
//                } else {
//                    sharedPreferences.setBoolean(false, ISALLSERVERUP, this);
//                    isThorConfirmed = false;
//                    sharedPreferences.setThorStatus(false, THORSTATUS, this);
//                    thorNodeStatusImg.setImageDrawable(getDrawable(R.drawable.redstatus));
//                }
//                break;
//            case 2:
//                if (b) {
//                    sharedPreferences.setBoolean(true, ISALLSERVERUP, this);
//                    isLightningConfirmed = true;
//                    sharedPreferences.setLightningStatus(true, LIGHTNINGSTATUS, this);
////                    lightningNodeStatusImg.setImageDrawable(getDrawable(R.drawable.greenstatus));
//                } else {
//                    sharedPreferences.setBoolean(false, ISALLSERVERUP, this);
//                    isLightningConfirmed = false;
//                    sharedPreferences.setLightningStatus(false, LIGHTNINGSTATUS, this);
////                    lightningNodeStatusImg.setImageDrawable(getDrawable(R.drawable.redstatus));
//                }
//                break;
//            case 3:
//                if (b) {
//                    sharedPreferences.setBoolean(true, ISALLSERVERUP, this);
//                    isBitcoinConfirmed = true;
//                    sharedPreferences.setBitcoinStatus(true, BITCOINSTATUS, this);
////                    bitcoinNodeStatusImg.setImageDrawable(getDrawable(R.drawable.greenstatus));
//                } else {
//                    sharedPreferences.setBoolean(false, ISALLSERVERUP, this);
//                    isBitcoinConfirmed = false;
//                    sharedPreferences.setBitcoinStatus(false, BITCOINSTATUS, this);
////                    bitcoinNodeStatusImg.setImageDrawable(getDrawable(R.drawable.redstatus));
//                }
//                break;
//        }
        if (sharedPreferences.getvalueofconnectedSocket("socketconnected", this) == "") {
            thorNodeStatusImg!!.setImageDrawable(getDrawable(R.drawable.redstatus))
        } else {
            thorNodeStatusImg!!.setImageDrawable(getDrawable(R.drawable.greenstatus))
        }
    }

    private fun goAlertDialogwithOneBTn(
        i: Int,
        alertTitleMessage: String,
        alertMessage: String,
        alertBtn1Message: String,
        alertBtn2Message: String
    ) {
        val goAlertDialogwithOneBTnDialog: Dialog
        goAlertDialogwithOneBTnDialog = Dialog(this)
        goAlertDialogwithOneBTnDialog.setContentView(R.layout.alert_dialog_layout)
        goAlertDialogwithOneBTnDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        goAlertDialogwithOneBTnDialog.setCancelable(false)
        val alerttitleTv = goAlertDialogwithOneBTnDialog.findViewById<TextView>(R.id.alertTitle)
        val alertmessageTv = goAlertDialogwithOneBTnDialog.findViewById<TextView>(R.id.alertMessage)
        val yesbtn = goAlertDialogwithOneBTnDialog.findViewById<Button>(R.id.yesbtn)
        val nobtn = goAlertDialogwithOneBTnDialog.findViewById<Button>(R.id.nobtn)
        yesbtn.text = alertBtn1Message
        nobtn.text = alertBtn2Message
        alerttitleTv.text = alertTitleMessage
        alertmessageTv.text = alertMessage
        if (i == 1) {
            nobtn.visibility = View.GONE
            alerttitleTv.visibility = View.GONE
        } else {
        }
        yesbtn.setOnClickListener { goAlertDialogwithOneBTnDialog.dismiss() }
        nobtn.setOnClickListener { goAlertDialogwithOneBTnDialog.dismiss() }
        goAlertDialogwithOneBTnDialog.show()
    }
}