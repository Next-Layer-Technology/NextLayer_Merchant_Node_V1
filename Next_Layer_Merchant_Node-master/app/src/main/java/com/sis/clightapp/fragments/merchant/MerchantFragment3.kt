package com.sis.clightapp.fragments.merchant

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.sis.clightapp.Interface.ApiClientStartStop
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.R
import com.sis.clightapp.activity.MainEntryActivityNew
import com.sis.clightapp.fragments.shared.Auth2FaFragment
import com.sis.clightapp.model.GsonModel.Getinfoerror
import com.sis.clightapp.model.REST.ServerStartStop.Node.NodeResp
import com.sis.clightapp.model.ScreenInfo
import com.sis.clightapp.services.SessionService
import com.sis.clightapp.session.MyLogOutService
import com.sis.clightapp.util.AppConstants
import com.sis.clightapp.util.CustomSharedPreferences
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tech.gusavila92.websocketclient.WebSocketClient
import java.io.File
import java.net.URI
import java.net.URISyntaxException
import java.util.*

class MerchantFragment3 : MerchantBaseFragment() {
    private val sessionService: SessionService by inject()
    private val webservice by lazy {
        ApiClientStartStop.getRetrofit(requireContext()).create(Webservice::class.java)
    }
    var TAG = "CLighting App"
    var countAddScreen = 1
    private var gdaxUrl = "ws://73.36.65.41:8095/SendCommands"
    var code1 = ""
    var code = 0
    var countDetach = 0
    var countquit = 0
    private lateinit var webSocketClient: WebSocketClient
    var list = ArrayList<ScreenInfo>()
    var listafterAdd = ArrayList<ScreenInfo>()
    var listafterDetach = ArrayList<ScreenInfo>()
    var newlist = ArrayList<ScreenInfo>()
    private lateinit var exitingdialog: ProgressDialog
    private lateinit var startServerPD: ProgressDialog
    private lateinit var stopServerPD: ProgressDialog
    private lateinit var stopcall: ProgressDialog
    private lateinit var startcall: ProgressDialog
    private lateinit var screenCall: ProgressDialog
    private lateinit var quitCall: ProgressDialog
    private lateinit var addscreenCall: ProgressDialog
    private lateinit var detachscreencall: ProgressDialog
    private lateinit var getinfocall: ProgressDialog
    private lateinit var simpleLoader: ProgressDialog
    private lateinit var startBitcoinBtn: Button
    private lateinit var stopBitcoinBtn: Button
    private lateinit var startLightningBtn: Button
    private lateinit var stopLightningBtn: Button
    private lateinit var removeSshKeyBtn: Button
    private lateinit var rebootUpdateUpgradeBtn: Button
    private lateinit var removeCredentials: Button
    private lateinit var updateReboodnodehost: Button
    private lateinit var setTextWithSpan: TextView
    private lateinit var resultLightning: TextView
    private lateinit var resultBitcoin: TextView
    private lateinit var resultRebootupdateupgrade: TextView
    private lateinit var checkStatusPD: ProgressDialog
    private lateinit var thorNodeStatusImg: ImageView
    var isThorConfirmed = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_merchant3, container, false)
        sharedPreferences = CustomSharedPreferences()
        thorNodeStatusImg = view.findViewById(R.id.thor_status)
        setTextWithSpan = view.findViewById(R.id.imageView3)
        resultLightning = view.findViewById(R.id.result_Lightninng)
        resultBitcoin = view.findViewById(R.id.result_Bitcoin)
        resultRebootupdateupgrade = view.findViewById(R.id.result_RebootUpdateUpgrade)
        val boldStyle = StyleSpan(Typeface.BOLD)
        setTextWithSpan(
            setTextWithSpan,
            getString(R.string.welcome_text),
            getString(R.string.welcome_text_bold),
            boldStyle
        )
        exitingdialog = ProgressDialog(requireContext())
        exitingdialog.setMessage("Loading...")
        exitingdialog.setCancelable(false)
        stopcall = ProgressDialog(requireContext())
        stopcall.setMessage("Loading...")
        stopcall.setCancelable(false)
        quitCall = ProgressDialog(requireContext())
        quitCall.setMessage("Loading...")
        quitCall.setCancelable(false)
        addscreenCall = ProgressDialog(requireContext())
        addscreenCall.setMessage("Loading...")
        addscreenCall.setCancelable(false)
        detachscreencall = ProgressDialog(requireContext())
        detachscreencall.setMessage("Loading...")
        detachscreencall.setCancelable(false)
        screenCall = ProgressDialog(requireContext())
        screenCall.setMessage("Loading...")
        screenCall.setCancelable(false)
        startcall = ProgressDialog(requireContext())
        startcall.setMessage("Loading...")
        startcall.setCancelable(false)
        getinfocall = ProgressDialog(requireContext())
        getinfocall.setMessage("Loading...")
        getinfocall.setCancelable(false)
        simpleLoader = ProgressDialog(requireContext())
        simpleLoader.setMessage("In progress...")
        simpleLoader.setCancelable(false)
        startServerPD = ProgressDialog(requireContext())
        startServerPD.setMessage("Connecting...")
        startServerPD.setCancelable(false)
        stopServerPD = ProgressDialog(requireContext())
        stopServerPD.setMessage("Connecting...")
        stopServerPD.setCancelable(false)
        checkStatusPD = ProgressDialog(requireContext())
        checkStatusPD.setMessage("Loading...")
        checkStatusPD.setCancelable(false)
        rebootUpdateUpgradeBtn = view.findViewById(R.id.rebootUpdateUpgradeBtn)
        removeSshKeyBtn = view.findViewById(R.id.removeSshKeyBtn)
        startBitcoinBtn = view.findViewById(R.id.startBitcoinBtn)
        stopBitcoinBtn = view.findViewById(R.id.stopBitcoinBtn)
        startLightningBtn = view.findViewById(R.id.startLightningBtn)
        stopLightningBtn = view.findViewById(R.id.stopLightningBtn)
        removeCredentials = view.findViewById(R.id.removecredentials)
        updateReboodnodehost = view.findViewById(R.id.reboot_restartnodehost)
        gdaxUrl = CustomSharedPreferences().getvalueofMWSCommand("mws_command", requireContext())
        sharedPreferences = CustomSharedPreferences()
        removeCredentials.setOnClickListener(View.OnClickListener {
            sharedPreferences.clearAllPreferences(
                requireContext()
            )
        })
        updateReboodnodehost.setOnClickListener { getinfo() }
        startBitcoinBtn.setOnClickListener {
            val merchantData = sessionService.getMerchantData()
            if (merchantData != null) {
                if (merchantData.ssh_ip_port != null && merchantData.ssh_password != null && merchantData.ssh_username != null) {
                    val type = "start"
                    val ssh = merchantData.ssh_ip_port
                    if (ssh != null) {
                        if (ssh.isNotEmpty()) {
                            if (ssh.contains(":")) {
                                val sh = ssh.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val host = sh[0]
                                val port = sh[1]
                                if (merchantData.isIs_own_bitcoin) {
                                    val sshPass = merchantData.ssh_password
                                    val sshUsername = merchantData.ssh_username
                                    startBitcoinServer(type, host, port, sshUsername)
                                } else {
                                    val builder = AlertDialog.Builder(requireContext())
                                    builder.setMessage("No Own Bitcoin Node")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                                }
                            } else {
                                //TODO
                                val builder = AlertDialog.Builder(requireContext())
                                builder.setMessage("Invalid SSH IP!")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                            }
                        } else {
                            //TODO
                            val builder = AlertDialog.Builder(requireContext())
                            builder.setMessage("Empty SSH IP!")
                                .setCancelable(false)
                                .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                        }
                    } else {
                        //TODO
                        val builder = AlertDialog.Builder(
                            requireContext()
                        )
                        builder.setMessage("Unavaiable SSH IP!")
                            .setCancelable(false)
                            .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                    }
                } else {
                    val builder = AlertDialog.Builder(
                        requireContext()
                    )
                    builder.setMessage("Merchant Info Missing")
                        .setCancelable(false)
                        .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                }
            } else {
                val builder = AlertDialog.Builder(
                    requireContext()
                )
                builder.setMessage("Enter Merchant ID")
                    .setCancelable(false)
                    .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
            }
        }
        stopBitcoinBtn.setOnClickListener {
            val merchantData = sessionService.getMerchantData()
            if (merchantData != null) {
                if (merchantData.ssh_ip_port != null && merchantData.ssh_password != null && merchantData.ssh_username != null) {
                    val type = "stop"
                    val ssh = merchantData.ssh_ip_port
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                val sh = ssh.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val host = sh[0]
                                val port = sh[1]
                                if (merchantData.isIs_own_bitcoin) {
                                    val sshPass = merchantData.ssh_password
                                    val sshUsername = merchantData.ssh_username
                                    stopBitcoinServer(type, host, port, sshUsername, sshPass)
                                } else {
                                    val builder = AlertDialog.Builder(
                                        requireContext()
                                    )
                                    builder.setMessage("No Own Bitcoin Node")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                                }
                            } else {
                                //TODO
                                val builder = AlertDialog.Builder(
                                    requireContext()
                                )
                                builder.setMessage("Invalid SSH IP!")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                            }
                        } else {
                            //TODO
                            val builder = AlertDialog.Builder(
                                requireContext()
                            )
                            builder.setMessage("Empty SSH IP!")
                                .setCancelable(false)
                                .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                        }
                    } else {
                        val builder = AlertDialog.Builder(
                            requireContext()
                        )
                        builder.setMessage("Unavailable SSH IP!")
                            .setCancelable(false)
                            .setPositiveButton("Ok") { dialog, _ -> dialog.cancel() }.show()
                    }
                } else {
                    val builder = AlertDialog.Builder(
                        requireContext()
                    )
                    builder.setMessage("Merchant Info Missing")
                        .setCancelable(false)
                        .setPositiveButton("Ok") { dialog, _ -> dialog.cancel() }.show()
                }
            } else {
                val builder = AlertDialog.Builder(
                    requireContext()
                )
                builder.setMessage("Enter Merchant ID")
                    .setCancelable(false)
                    .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
            }

        }
        startLightningBtn.setOnClickListener {
            countAddScreen = 0
            screen_ls()
        }
        stopLightningBtn.setOnClickListener {
            stoplightning()
        }
        removeSshKeyBtn.setOnClickListener {
            val merchantData = sessionService.getMerchantData()
            if (merchantData != null) {
                Auth2FaFragment().show(childFragmentManager, null)
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "")
            }
        }
        rebootUpdateUpgradeBtn.setOnClickListener {
            val merchantData = sessionService.getMerchantData()
            if (merchantData != null) {
                if (merchantData.ssh_ip_port != null && merchantData.ssh_password != null && merchantData.ssh_username != null) {
                    val type = "update"
                    val ssh = merchantData.ssh_ip_port
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                val sh = ssh.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val host = sh[0]
                                val port = sh[1]
                                val sshPass = merchantData.ssh_password
                                val sshUsername = merchantData.ssh_username
                                updateServer(type, host, port, sshUsername, sshPass)
                            } else {
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "Ok", "")
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "Ok", "")
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "Ok", "")
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing!", "Ok", "")
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "Ok", "")
            }

        }
        return view
    }

    private fun upgradeServer(type: String, host: String, port: String, sshUsername: String, sshPass: String) {
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File?
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
            return
        }
        if (yourFile.exists()) {
            startServerPD.show()
            var sshkeypasval = sharedPreferences.getString("sshkeypass", requireContext())
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = sshkeypasval.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val type2: RequestBody = type.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val host2: RequestBody = host.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val port2: RequestBody = port.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshUsername2: RequestBody = sshUsername.toRequestBody(("text/plain".toMediaTypeOrNull()))
            var itemImageFileMPBody: MultipartBody.Part? = null
            val photo_id: RequestBody = yourFile.asRequestBody(("".toMediaTypeOrNull()))
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> = webservice
                .upgradeServer(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody)
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200) {
                                startServerPD.dismiss()
                                resultRebootupdateupgrade.text = resp.message
                                rebootServer("reboot", host, port, sshUsername, sshPass)
                            } else {
                                startServerPD.dismiss()
                                resultRebootupdateupgrade.text = resp.message
                            }
                        } else {
                            startServerPD.dismiss()
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "Retry", "")
                        }
                    } else {
                        startServerPD.dismiss()
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "Retry", "")
                    }
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    goAlertDialogwithOneBTn(1, "", t.message, "Retry", "")
                    startServerPD.dismiss()
                }
            })
        } else {
            goAlertDialogwithOneBTn(1, "", "SSH is Missing", "Retry", "")
        }
    }

    private fun updateServer(type: String, host: String, port: String, sshUsername: String, sshPass: String) {
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File?
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
            return
        }
        if (yourFile.exists()) {
            startServerPD.show()
            var sshkeypasval = sharedPreferences.getString("sshkeypass", requireContext())
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = sshkeypasval.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val type2: RequestBody = type.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val host2: RequestBody = host.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val port2: RequestBody = port.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshUsername2: RequestBody = sshUsername.toRequestBody(("text/plain".toMediaTypeOrNull()))
            var itemImageFileMPBody: MultipartBody.Part? = null
            val photoId: RequestBody = RequestBody.create(("".toMediaTypeOrNull()), yourFile)
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photoId)
            val call: Call<NodeResp> =
                webservice.updateServer(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody)
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200) {
                                startServerPD.dismiss()
                                resultRebootupdateupgrade.text = resp.message
                                upgradeServer("upgrade", host, port, sshUsername, sshPass)
                            } else {
                                startServerPD.dismiss()
                                resultRebootupdateupgrade.text = resp.message
                            }
                        } else {
                            startServerPD.dismiss()
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "Retry", "")
                        }
                    } else {
                        startServerPD.dismiss()
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "Retry", "")
                    }
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    goAlertDialogwithOneBTn(1, "", t.message, "Retry", "")
                    startServerPD.dismiss()
                }
            })
        } else {
            goAlertDialogwithOneBTn(1, "", "SSH is Missing", "Retry", "")
        }
    }

    private fun rebootServer(type: String, host: String, port: String, sshUsername: String, sshPass: String) {
        val yourFilePath = Environment.getExternalStorageDirectory().toString() + "/merhantapp"
        var yourFile: File?
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
            return
        }
        if (yourFile.exists()) {
            startServerPD.show()
            var sshkeypasval = sharedPreferences.getString("sshkeypass", requireContext())
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = sshkeypasval.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val type2: RequestBody = type.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val host2: RequestBody = host.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val port2: RequestBody = port.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshUsername2: RequestBody = sshUsername.toRequestBody(("text/plain".toMediaTypeOrNull()))
            var itemImageFileMPBody: MultipartBody.Part?
            val photo_id: RequestBody = yourFile.asRequestBody(("".toMediaTypeOrNull()))
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> =
                webservice.rebootServer(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody)
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200) {
                                val handler = Handler()
                                handler.postDelayed({
                                    startServerPD.dismiss()
                                    resultRebootupdateupgrade.text = resp.message
                                    sharedPreferences.clearAllPrefExceptOfSShkeyPassword(requireContext())
                                    startActivity(Intent(requireActivity(), MainEntryActivityNew::class.java))
                                }, AppConstants.TIMEFORWAITLN2)
                            } else {
                                startServerPD.dismiss()
                                resultRebootupdateupgrade.text = resp.message
                            }
                        } else {
                            startServerPD.dismiss()
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "Retry", "")
                        }
                    } else {
                        startServerPD.dismiss()
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "Retry", "")
                    }
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    goAlertDialogwithOneBTn(1, "", t.message, "Retry", "")
                    startServerPD.dismiss()
                }
            })
        } else {
            goAlertDialogwithOneBTn(1, "", "SSH is Missing", "Retry", "")
        }
    }

    private fun goAlertDialogwithOneBTn(
        i: Int,
        alertTitleMessage: String,
        alertMessage: String?,
        alertBtn1Message: String,
        alertBtn2Message: String
    ) {
        val goAlertDialogwithOneBTnDialog = Dialog(requireContext())
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

    fun checkAppFlow() {
        val merchantData = sessionService.getMerchantData()
        if (merchantData != null) {
            if (merchantData.ssh_ip_port != null && merchantData.ssh_password != null && merchantData.ssh_username != null && merchantData.rpc_username != null && merchantData.rpc_password != null) {
                val type = "status"
                val ssh = merchantData.ssh_ip_port
                if (ssh != null) {
                    if (!ssh.isEmpty()) {
                        if (ssh.contains(":")) {
                            val sh = ssh.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            val host = sh[0]
                            val port = sh[1]
                            if (merchantData.isIs_own_bitcoin) {
                                //TODO:When  Own BTC
                                val sshPass = merchantData.ssh_password
                                val sshUsername = merchantData.ssh_username
                                val rpcUserName = merchantData.rpc_username
                                val rpcPassword = merchantData.rpc_password
                                goToOwnBitcoinCase(host, port, sshUsername, sshPass, rpcUserName, rpcPassword)
                            } else {
                                //TODO:When Not Own BTC
                                val sshPass = merchantData.ssh_password
                                val sshUsername = merchantData.ssh_username
                                val rpcUserName = merchantData.rpc_username
                                val rpcPassword = merchantData.rpc_password
                                goTOtheNotOwnBitcoinCase()
                            }
                        } else {
                            //TODO
                            val builder = AlertDialog.Builder(
                                requireContext()
                            )
                            builder.setMessage("Invalid SSH IP!")
                                .setCancelable(false)
                                .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                        }
                    } else {
                        //TODO
                        val builder = AlertDialog.Builder(
                            requireContext()
                        )
                        builder.setMessage("Empty SSH IP!")
                            .setCancelable(false)
                            .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                    }
                } else {
                    //TODO
                    val builder = AlertDialog.Builder(
                        requireContext()
                    )
                    builder.setMessage("Unavailable SSH IP!")
                        .setCancelable(false)
                        .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                }
            } else {
                val builder = AlertDialog.Builder(
                    requireContext()
                )
                builder.setMessage("Merchant Info Missing")
                    .setCancelable(false)
                    .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
            }
        } else {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Enter Merchant ID")
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
        }

    }

    //TODO: When Own Bitcoin
    private fun goToOwnBitcoinCase(
        host: String,
        port: String,
        sshUsername: String,
        sshPass: String,
        rpcUserName: String,
        rpcPassword: String
    ) {
        getinfo()
        checkBitcoinNodeStatus(host, port, sshUsername, sshPass, rpcUserName, rpcPassword)
    }

    //TODO: When No Own Bitcoin
    private fun goTOtheNotOwnBitcoinCase(
    ) {
        getinfo()
        updateResultBitcoinStatus("NO LOCAL NODE")
    }

    private fun checkBitcoinNodeStatus(
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
        var yourFile: File?
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
            return
        }
        if (yourFile.exists()) {
            checkStatusPD.show()
            var sshkeypasval = sharedPreferences.getString("sshkeypass", requireContext())
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = sshkeypasval.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val host2: RequestBody = host.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val port2: RequestBody = port.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshUsername2: RequestBody = sshUsername.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshPass2: RequestBody = sshPass.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val rpcUserName2: RequestBody = rpcUserName.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val rpcPassword2: RequestBody = rpcPassword.toRequestBody(("text/plain".toMediaTypeOrNull()))
            var itemImageFileMPBody: MultipartBody.Part?
            val photo_id: RequestBody = yourFile.asRequestBody(("".toMediaTypeOrNull()))
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> = webservice.checkBitcoinNodeServerStatus2(
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
                                updateResultBitcoinStatus("ACTIVE")
                            } else if (resp.code == 200 && resp.message == "down") {
                                updateResultBitcoinStatus("INACTIVE")
                            }
                        } else {
                            val builder = AlertDialog.Builder(
                                requireContext()
                            )
                            builder.setMessage("Invalid SSH Info")
                                .setCancelable(false)
                                .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                        }
                    } else {
                        val builder = AlertDialog.Builder(
                            requireContext()
                        )
                        builder.setMessage("Invalid SSH Info")
                            .setCancelable(false)
                            .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                    }
                    checkStatusPD.dismiss()
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    val builder = AlertDialog.Builder(
                        requireContext()
                    )
                    builder.setMessage("Server Side Issue")
                        .setCancelable(false)
                        .setPositiveButton("Retry") { dialog, id -> dialog.cancel() }.show()
                    checkStatusPD.dismiss()
                }
            })
        } else {
            showToast("SSh is Missing")
        }
    }

    private fun startBitcoinServer(type: String, host: String, port: String, sshUsername: String) {
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File?
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
            return
        }
        if (yourFile.exists()) {
            startServerPD.show()
            var sshkeypasval = sharedPreferences.getString("sshkeypass", requireContext())
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = sshkeypasval.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val type2: RequestBody = type.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val host2: RequestBody = host.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val port2: RequestBody = port.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshUsername2: RequestBody = sshUsername.toRequestBody(("text/plain".toMediaTypeOrNull()))
            var itemImageFileMPBody: MultipartBody.Part?
            val photo_id: RequestBody = yourFile.asRequestBody(("".toMediaTypeOrNull()))
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> =
                webservice.startBitcoinServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody)
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200) {
                                val respMessgr = resp.message
                                val handler = Handler()
                                handler.postDelayed({ // yourMethod();
                                    startServerPD.dismiss()
                                    updateResultBitcoinStatus(respMessgr)
                                }, AppConstants.TIMEFORWAITLN2)
                            } else {
                                startServerPD.dismiss()
                                updateResultBitcoinStatus(resp.message)
                            }
                        } else {
                            val builder = AlertDialog.Builder(
                                requireContext()
                            )
                            builder.setMessage("Invalid SSH Info!")
                                .setCancelable(false)
                                .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                            startServerPD.dismiss()
                        }
                    } else {
                        val builder = AlertDialog.Builder(
                            requireContext()
                        )
                        builder.setMessage("Invalid SSH Info!")
                            .setCancelable(false)
                            .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                        startServerPD.dismiss()
                    }
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    val builder = AlertDialog.Builder(
                        requireContext()
                    )
                    builder.setMessage("Server Side Issue")
                        .setCancelable(false)
                        .setPositiveButton("Retry") { dialog, id -> dialog.cancel() }.show()
                    startServerPD.dismiss()
                }
            })
        } else {
            showToast("SSh is Missing")
        }
    }

    private fun stopBitcoinServer(type: String, host: String, port: String, sshUsername: String, sshPass: String) {
        val yourFilePath = (Environment
            .getExternalStorageDirectory().toString()
                + "/merhantapp")
        var yourFile: File?
        try {
            yourFile = File(yourFilePath)
        } catch (e: Exception) {
            showToast("File Not Found")
            return
        }
        if (yourFile.exists()) {
            startServerPD.show()
            var sshkeypasval = sharedPreferences.getString("sshkeypass", requireContext())
            if (sshkeypasval == null) {
                sshkeypasval = ""
            }
            val sshkeypass: RequestBody = sshkeypasval.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val type2: RequestBody = type.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val host2: RequestBody = host.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val port2: RequestBody = port.toRequestBody(("text/plain".toMediaTypeOrNull()))
            val sshUsername2: RequestBody = sshUsername.toRequestBody(("text/plain".toMediaTypeOrNull()))
            var itemImageFileMPBody: MultipartBody.Part? = null
            val photo_id: RequestBody = yourFile.asRequestBody(("".toMediaTypeOrNull()))
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.path, photo_id)
            val call: Call<NodeResp> =
                webservice.stopBitcoinServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody)
            call.enqueue(object : Callback<NodeResp?> {
                override fun onResponse(call: Call<NodeResp?>, response: Response<NodeResp?>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        if (resp != null) {
                            if (resp.code == 200) {
                                updateResultBitcoinStatus(resp.message)
                            } else {
                                updateResultBitcoinStatus(resp.message)
                            }
                        } else {
                            val builder = AlertDialog.Builder(
                                requireContext()
                            )
                            builder.setMessage("Invalid SSH Info!")
                                .setCancelable(false)
                                .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                        }
                    } else {
                        val builder = AlertDialog.Builder(
                            requireContext()
                        )
                        builder.setMessage("Invalid SSH Info!")
                            .setCancelable(false)
                            .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }.show()
                    }
                    startServerPD.dismiss()
                }

                override fun onFailure(call: Call<NodeResp?>, t: Throwable) {
                    val builder = AlertDialog.Builder(
                        requireContext()
                    )
                    builder.setMessage("Server Side Issue")
                        .setCancelable(false)
                        .setPositiveButton("Retry") { dialog, id -> dialog.cancel() }.show()
                    startServerPD.dismiss()
                }
            })
        } else {
            showToast("SSH is Missing")
        }
    }

    //TODO:Update the Result on View
    private fun updateResultLightningStatus(s: String) {
        when (s) {
            "ACTIVE" -> {
                sharedPreferences.setBoolean(true, LIGHTNINGSTATUS, requireContext())
                resultLightning.text = s
                resultLightning.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text_colour))
            }

            "INACTIVE" -> {
                sharedPreferences.setBoolean(false, LIGHTNINGSTATUS, requireContext())
                resultLightning.text = s
                resultLightning.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text_colour))
            }

            else -> {
                sharedPreferences.setBoolean(false, LIGHTNINGSTATUS, requireContext())
                resultLightning.text = s
                resultLightning.setTextColor(ContextCompat.getColor(requireContext(), R.color.remote_text_colour))
            }
        }
    }

    private fun updateResultBitcoinStatus(s: String) {
        when (s) {
            "NO LOCAL NODE" -> {
                sharedPreferences.setBoolean(true, BITCOINSTATUS, requireContext())
                resultBitcoin.text = s
                resultBitcoin.setTextColor(ContextCompat.getColor(requireContext(), R.color.remote_text_colour))
            }

            "ACTIVE" -> {
                resultBitcoin.text = s
                resultBitcoin.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text_colour))
            }

            "INACTIVE" -> {
                sharedPreferences.setBoolean(false, BITCOINSTATUS, requireContext())
                resultBitcoin.text = s
                resultBitcoin.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text_colour))
            }

            else -> {
                sharedPreferences.setBoolean(false, BITCOINSTATUS, requireContext())
                resultBitcoin.text = s
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().stopService(Intent(requireContext(), MyLogOutService::class.java))
    }

    override fun onStart() {
        super.onStart()
    }

    fun startlightning() {
        startcall.show()
        val clientCoinPrice = OkHttpClient()
        val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext())
                val json =
                    "{\"token\" : \"$token\", \"commands\" : [\"screen -S Lightning -p 0 -X stuff \\\"lightningd --disable-plugin bcli^M\\\"\"] }"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    startcall.show()
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                requireActivity().runOnUiThread { //                        parseJSONForRefunds(text);
                    try {
                        val jsonObject = JSONObject(text)
                        if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                            webSocket.close(1000, null)
                            webSocket.cancel()
                        } else {
                            startcall.show()
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

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                //TODO: stuff
                startcall.dismiss()
                requireActivity().runOnUiThread { //                        showToast(String.valueOf(response));
                    simpleLoader.show()
                    val handler = Handler()
                    handler.postDelayed({
                        getinfo()
                        simpleLoader.dismiss()
                    }, 30000)
                }
            }
        }
        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice)
        clientCoinPrice.dispatcher.executorService.shutdown()
    }

    fun stoplightning() {
        stopcall.show()
        val clientCoinPrice = OkHttpClient()
        val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext())
                val json =
                    "{\"token\" : \"$token\", \"commands\" : [\"screen -S Lightning -p 0 -X stuff \\\"^C\\\"\"] }"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    requireActivity().runOnUiThread { stopcall.dismiss() }
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
                        } else {
                            stopcall.dismiss()
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

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                //TODO: stuff
                stopcall.dismiss()
                requireActivity().runOnUiThread { //                        showToast(String.valueOf(response));
                    simpleLoader.show()
                    val handler = Handler()
                    handler.postDelayed({
                        getinfo()
                        simpleLoader.dismiss()
                    }, 5000)
                }
            }
        }
        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice)
        clientCoinPrice.dispatcher.executorService.shutdown()
    }

    private fun getinfo() {
        getinfocall.show()
        val uri: URI
        uri = try {
            // Connect to local host
            URI(gdaxUrl)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen() {
                val token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext())
                val json = "{\"token\" : \"$token\", \"commands\" : [\"lightning-cli getinfo\"] }"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocketClient.send(obj.toString())
                } catch (t: Throwable) {
                    requireActivity().runOnUiThread { getinfocall.dismiss() }
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                }
                Log.i("WebSocket", "Session is starting")
                //                Toast.makeText(getApplicationContext(), "opend", Toast.LENGTH_SHORT).show();
            }

            override fun onTextReceived(s: String) {
                Log.e("TAG", "MESSAGE: $s")
                println(s)
                val gson = Gson()
                try {
                    val getinfoerror = gson.fromJson(s, Getinfoerror::class.java)
                    if (getinfoerror.isError) {
                        requireActivity().runOnUiThread {
                            sharedPreferences.setBoolean(false, THORSTATUS, requireContext())
                            isThorConfirmed = false
                            thorNodeStatusImg.setImageDrawable(requireContext().getDrawable(R.drawable.redstatus))
                            updateResultLightningStatus("INACTIVE ")
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            updateResultLightningStatus("ACTIVE ")
                            sharedPreferences.setBoolean(true, THORSTATUS, requireContext())
                            isThorConfirmed = true
                            thorNodeStatusImg.setImageDrawable(requireContext().getDrawable(R.drawable.greenstatus))
                        }
                    }
                } catch (e: Exception) {
                    try {
                        val jsonObject = JSONObject(s)
                        code1 = jsonObject.getString("id")
                        if (code1 == "") {
                            requireActivity().runOnUiThread {
                                sharedPreferences.setBoolean(false, THORSTATUS, requireContext())
                                isThorConfirmed = false
                                thorNodeStatusImg.setImageDrawable(requireContext().getDrawable(R.drawable.redstatus))
                                updateResultLightningStatus("INACTIVE ")
                            }
                            sharedPreferences.setvalueofconnectedSocket("", "socketconnected", requireContext())
                        } else {
                            sharedPreferences.setvalueofconnectedSocket(code1, "socketconnected", requireContext())
                            requireActivity().runOnUiThread {
                                updateResultLightningStatus("ACTIVE ")
                                sharedPreferences.setBoolean(true, THORSTATUS, requireContext())
                                isThorConfirmed = true
                                thorNodeStatusImg.setImageDrawable(requireContext().getDrawable(R.drawable.greenstatus))
                            }
                        }
                        if (code == 724) {
                            sharedPreferences.setvalueofSocketCode(code, "socketcode", requireContext())
                        } else if (code == 724) {
                            sharedPreferences.setvalueofSocketCode(code, "socketcode", requireContext())
                        }
                    } catch (err: JSONException) {
                        Log.d("Error", err.toString())
                    }
                }
                requireActivity().runOnUiThread { getinfocall.dismiss() }
            }

            override fun onBinaryReceived(data: ByteArray) {
//                showToast("binary" + data.toString());
            }

            override fun onPingReceived(data: ByteArray) {
//                showToast("ping" + data.toString());
            }

            override fun onPongReceived(data: ByteArray) {
//                showToast("ping2" + data.toString());
            }

            override fun onException(e: Exception) {
                println(e.message)
                getinfocall.dismiss()
                //                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

    fun screen_ls() {
        screenCall.show()
        val clientCoinPrice = OkHttpClient()
        val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext())
                val json = "{\"token\" : \"$token\", \"commands\" : [\"screen -ls\"] }"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    screenCall.dismiss()
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                try {
                    val jsonObject = JSONObject(text)
                    if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                        webSocket.close(1000, null)
                        webSocket.cancel()
                    } else {
                        if (text == "There are screens on:\r\r\n") {
                        } else if (text == "No Sockets found in /run/screen/S-routing-node-4.\r\n\r\r\n") {
                            requireActivity().runOnUiThread {
                                addScreen() //1
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                var s = text
                                s = s.replace("[\\n\\t\\r]".toRegex(), " ")
                                s = s.lowercase(Locale.getDefault())
                                val words = s.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                for (i in words.indices) {
                                    if (words[i] == "(detached)" || words[i] == "(attached)" || words[i] == "detached" || words[i] == "attached") {
                                        val secreeninfo = ScreenInfo()
                                        secreeninfo.status = words[i]
                                        if (list.size == 0) {
                                            list.add(0, secreeninfo)
                                        } else {
                                            list.add(list.size, secreeninfo)
                                        }
                                    }
                                }
                                newlist.clear()
                                for (i in words.indices) {
                                    if (words[i].contains(".lightning")) {
                                        val str = words[i]
                                        val kept = str.substring(0, str.indexOf("."))
                                        val secreeninfo = ScreenInfo()
                                        secreeninfo.pid = kept
                                        if (newlist.size == 0) {
                                            newlist.add(0, secreeninfo)
                                        } else {
                                            newlist.add(newlist.size, secreeninfo)
                                        }
                                    }
                                }
                                for (i in list.indices) {
                                    for (j in newlist.indices) {
                                        if (i == j) {
                                            list[i].pid = newlist[j].pid
                                        }
                                    }
                                }
                                if (list.size == 0) {
                                    requireActivity().runOnUiThread {
                                        screenCall.dismiss()
                                        simpleLoader.show()
                                        val handler = Handler()
                                        handler.postDelayed({
                                            list.clear()
                                            addScreen() //2
                                            simpleLoader.dismiss()
                                        }, 2000)
                                    }
                                } else {
                                    requireActivity().runOnUiThread {
                                        screenCall.dismiss()
                                        simpleLoader.show()
                                        val handler = Handler()
                                        handler.postDelayed({
                                            quitcreenScnerio()
                                            simpleLoader.dismiss()
                                        }, 2000)
                                    }
                                }
                            }
                        }
                        screenCall.dismiss()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
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

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                //TODO: stuff
                requireActivity().runOnUiThread {
                    screenCall.dismiss()
                    showToast(response.toString())
                }
            }
        }
        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice)
        clientCoinPrice.dispatcher.executorService.shutdown()
    }

    fun screen_ls_afterdetach() {
        screenCall.show()
        val clientCoinPrice = OkHttpClient()
        val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext())
                val json = "{\"token\" : \"$token\", \"commands\" : [\"screen -ls\"] }"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    screenCall.dismiss()
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                try {
                    val jsonObject = JSONObject(text)
                    if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                        requireActivity().runOnUiThread {
                            webSocket.close(1000, null)
                            webSocket.cancel()
                        }
                    } else {
                        when (text) {
                            "There are screens on:\r\r\n" -> {
                            }
                            "No Sockets found in /run/screen/S-routing-node-4.\r\n\r\r\n" -> {
                                requireActivity().runOnUiThread {
                                    addScreen() //3
                                }
                            }
                            else -> {
                                requireActivity().runOnUiThread {
                                    var s = text
                                    s = s.replace("[\\n\\t\\r]".toRegex(), " ")
                                    s = s.lowercase(Locale.getDefault())
                                    val words = s.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                    listafterDetach.clear()
                                    for (i in words.indices) {
                                        if (words[i] == "(detached)" || words[i] == "attached" || words[i] == "(attached)" || words[i] == "detached") {
                                            val secreeninfo = ScreenInfo()
                                            secreeninfo.status = words[i]
                                            if (listafterDetach.size == 0) {
                                                listafterDetach.add(0, secreeninfo)
                                            } else {
                                                listafterDetach.add(listafterDetach.size, secreeninfo)
                                            }
                                        }
                                    }
                                    newlist.clear()
                                    for (i in words.indices) {
                                        if (words[i].contains(".lightning")) {
                                            val str = words[i]
                                            val kept = str.substring(0, str.indexOf("."))
                                            val secreeninfo = ScreenInfo()
                                            secreeninfo.pid = kept
                                            if (newlist.size == 0) {
                                                newlist.add(0, secreeninfo)
                                            } else {
                                                newlist.add(newlist.size, secreeninfo)
                                            }
                                        }
                                    }
                                    for (i in list.indices) {
                                        for (j in newlist.indices) {
                                            if (i == j) {
                                                list[i].pid = newlist[j].pid
                                            }
                                        }
                                    }
                                    if (listafterDetach.size == 0) {
                                        requireActivity().runOnUiThread {
                                            screenCall.dismiss()
                                            simpleLoader.show()
                                            val handler = Handler()
                                            handler.postDelayed({
                                                addScreen() //4
                                                simpleLoader.dismiss()
                                            }, 2000)
                                        }
                                    } else {
                                        requireActivity().runOnUiThread {
                                            screenCall.dismiss()
                                            simpleLoader.show()
                                            val handler = Handler()
                                            handler.postDelayed({
                                                Ifdetached()
                                                simpleLoader.dismiss()
                                            }, 2000)
                                        }
                                    }
                                }
                                screenCall.dismiss()
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
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

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                //TODO: stuff
                requireActivity().runOnUiThread {
                    showToast(response.toString())
                    screenCall.dismiss()
                }
            }
        }
        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice)
        clientCoinPrice.dispatcher.executorService.shutdown()
    }

    fun screen_ls_afterAddnew() {
        screenCall.show()
        val clientCoinPrice = OkHttpClient()
        val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext())
                val json = "{\"token\" : \"$token\", \"commands\" : [\"screen -ls\"] }"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    screenCall.dismiss()
                    Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("TAG", "MESSAGE: $text")
                try {
                    val jsonObject = JSONObject(text)
                    if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                        requireActivity().runOnUiThread {
                            webSocket.close(1000, null)
                            webSocket.cancel()
                        }
                    } else {
                        when (text) {
                            "There are screens on:\r\r\n" -> {
                            }
                            "No Sockets found in /run/screen/S-routing-node-4.\r\n\r\r\n" -> {
                                requireActivity().runOnUiThread {
                                    addScreen() //5
                                }
                            }
                            else -> {
                                requireActivity().runOnUiThread {
                                    var s = text
                                    s = s.replace("[\\n\\t\\r]".toRegex(), " ")
                                    s = s.lowercase(Locale.getDefault())
                                    val words = s.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                    listafterAdd.clear()
                                    for (i in words.indices) {
                                        if (words[i] == "(detached)" || words[i] == "(attached)" || words[i] == "detached" || words[i] == "attached") {
                                            val secreeninfo = ScreenInfo()
                                            secreeninfo.status = words[i]
                                            if (listafterAdd.size == 0) {
                                                listafterAdd.add(0, secreeninfo)
                                            } else {
                                                listafterAdd.add(listafterAdd.size, secreeninfo)
                                            }
                                        }
                                    }
                                    newlist.clear()
                                    for (i in words.indices) {
                                        if (words[i].contains(".lightning")) {
                                            val str = words[i]
                                            val kept = str.substring(0, str.indexOf("."))
                                            val secreeninfo = ScreenInfo()
                                            secreeninfo.pid = kept
                                            if (newlist.size == 0) {
                                                newlist.add(0, secreeninfo)
                                            } else {
                                                newlist.add(newlist.size, secreeninfo)
                                            }
                                        }
                                    }
                                    for (i in list.indices) {
                                        for (j in newlist.indices) {
                                            if (i == j) {
                                                list[i].pid = newlist[j].pid
                                            }
                                        }
                                    }
                                    if (listafterAdd.size == 0) {
                                        simpleLoader.show()
                                        val handler = Handler()
                                        handler.postDelayed({
                                            screenCall.dismiss()
                                            listafterAdd.clear()
                                            addScreen() //6
                                            simpleLoader.dismiss()
                                        }, 2000)
                                    } else {
                                        screenCall.dismiss()
                                        simpleLoader.show()
                                        val handler = Handler()
                                        handler.postDelayed({
                                            detachscreenScnerio()
                                            simpleLoader.dismiss()
                                        }, 2000)
                                    }
                                }
                                screenCall.dismiss()
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
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

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                //TODO: stuff
                requireActivity().runOnUiThread {
                    showToast(response.toString())
                    screenCall.dismiss()
                }
            }
        }
        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice)
        clientCoinPrice.dispatcher.executorService.shutdown()
    }

    fun DetachScreen(id: String) {
        detachscreencall.show()
        val clientCoinPrice = OkHttpClient()
        val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext())
                val json = "{\"token\" : \"$token\", \"commands\" : [\" screen -d $id\"] }"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    detachscreencall.dismiss()
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
                            }
                        } else {
                            var s = text
                            s = s.replace("[\\n\\t\\r]".toRegex(), " ")
                            s = s.lowercase(Locale.getDefault())
                            val words = s.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            for (i in words.indices) {
                                if (words[i] == "(detached)" || words[i] == "(attached)") {
                                    val secreeninfo = ScreenInfo()
                                    secreeninfo.status = words[i]
                                    if (listafterDetach.size == 0) {
                                        listafterDetach.add(0, secreeninfo)
                                    } else {
                                        listafterDetach.add(listafterDetach.size, secreeninfo)
                                    }
                                }
                            }
                            for (i in words.indices) {
                                if (words[i].contains(".lightning")) {
                                    for (j in listafterDetach.indices) {
                                        val str = words[i]
                                        val kept = str.substring(0, str.indexOf("."))
                                        listafterDetach[j].pid = kept
                                    }
                                }
                            }
                            countDetach++
                            if (listafterAdd.size == countDetach) {
                                requireActivity().runOnUiThread {
                                    detachscreencall.dismiss()
                                    simpleLoader.show()
                                    val handler = Handler()
                                    handler.postDelayed({
                                        screen_ls_afterdetach()
                                        countDetach = 0
                                        listafterAdd.clear()
                                        simpleLoader.dismiss()
                                    }, 2000)
                                }
                            }
                            detachscreencall.dismiss()
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

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                //TODO: stuff
                requireActivity().runOnUiThread {
                    showToast(response.toString())
                    countDetach = 0
                    detachscreencall.dismiss()
                }
            }
        }
        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice)
        clientCoinPrice.dispatcher.executorService.shutdown()
    }

    fun addScreen() {
        if (countAddScreen == 2) {
            showToast("Please start again")
        } else {
            addscreenCall.show()
            countAddScreen++
            val clientCoinPrice = OkHttpClient()
            val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
            val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                    val token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext())
                    val json = "{\"token\" : \"$token\", \"commands\" : [\"screen -S Lightning\"] }"
                    try {
                        val obj = JSONObject(json)
                        Log.d("My App", obj.toString())
                        webSocket.send(obj.toString())
                    } catch (t: Throwable) {
                        requireActivity().runOnUiThread { addscreenCall.dismiss() }
                        Log.e("My App", "Could not parse malformed JSON: \"$json\"")
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.e("TAG", "MESSAGE: $text")
                    try {
                        val jsonObject = JSONObject(text)
                        if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                            requireActivity().runOnUiThread {
                                webSocket.close(1000, null)
                                webSocket.cancel()
                            }
                        } else {
                            if (text == "routing-node-4@routingnode4-desktop:~$ ") {
                                requireActivity().runOnUiThread {
                                    requireActivity().runOnUiThread {
                                        addscreenCall.dismiss()
                                        simpleLoader.show()
                                        val handler = Handler()
                                        handler.postDelayed({
                                            screen_ls_afterAddnew()
                                            simpleLoader.dismiss()
                                        }, 3000)
                                    }
                                }
                            } else {
                                addscreenCall.dismiss()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
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

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                    //TODO: stuff
                    addscreenCall.dismiss()
                    requireActivity().runOnUiThread {
                        //                        showToast(String.valueOf(response));
                        simpleLoader.show()
                        val handler = Handler()
                        handler.postDelayed({ //                                getinfo();
                            simpleLoader.dismiss()
                        }, 2000)
                    }
                }
            }
            clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice)
            clientCoinPrice.dispatcher.executorService.shutdown()
        }
    }

    fun quitScreen(pid: String) {
        quitCall.show()
        val clientCoinPrice = OkHttpClient()
        val requestCoinPrice: Request = Request.Builder().url(gdaxUrl).build()
        val webSocketListenerCoinPrice: WebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext())
                val json = "{\"token\" : \"$token\", \"commands\" : [\"screen -XS $pid quit\"] }"
                try {
                    val obj = JSONObject(json)
                    Log.d("My App", obj.toString())
                    webSocket.send(obj.toString())
                } catch (t: Throwable) {
                    requireActivity().runOnUiThread { quitCall.dismiss() }
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
                            }
                        } else {
                            quitCall.dismiss()
                            simpleLoader.show()
                            val handler = Handler()
                            handler.postDelayed({
                                if (countquit == list.size) {
                                    addScreen() //7
                                    list.clear()
                                    countquit = 0
                                }
                                simpleLoader.dismiss()
                            }, 2000)
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

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                //TODO: stuff
                quitCall.dismiss()
                requireActivity().runOnUiThread {
                    //                        showToast(String.valueOf(response));
                    simpleLoader.show()
                    val handler = Handler()
                    handler.postDelayed({
                        countquit++
                        if (countquit == list.size) {
                            countquit = 0
                            list.clear()
                            addScreen() //8
                        }
                        simpleLoader.dismiss()
                    }, 2000)
                }
            }
        }
        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice)
        clientCoinPrice.dispatcher.executorService.shutdown()
    }

    fun detachscreenScnerio() {
        for (i in listafterAdd.indices) {
            if (listafterAdd.size > 0) {
                DetachScreen(listafterAdd[i].pid)
            }
        }
    }

    fun quitcreenScnerio() {
        for (i in list.indices) {
            if (list.size > 0) {
                quitScreen(list[i].pid)
            }
        }
    }

    fun Ifdetached() {
        for (i in listafterDetach.indices) {
            if (listafterDetach.size > 0) {
                if (listafterDetach[i].status == "(detached)" || listafterDetach[i].status == "detached") {
                    startlightning()
                } else {
                    showToast("Attached")
                }
            } else if (listafterDetach[i].status == "attached") {
                listafterDetach.clear()
                detachscreenScnerio()
            }
        }
    }
}