package com.sis.clightapp.fragments.shared

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonObject
import com.sis.clightapp.Interface.ApiPaths2
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.R
import com.sis.clightapp.model.REST.get_session_response
import com.sis.clightapp.model.WebsocketResponse.WebSocketOTPresponse
import com.sis.clightapp.util.CustomSharedPreferences
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class Auth2FaFragment : DialogFragment() {
    private val apiClient: ApiPaths2 by inject()
    private val webservice: Webservice by inject()
    private lateinit var ctx: Context

    lateinit var progressDialog: ProgressDialog
    val sharedPreferences = CustomSharedPreferences()
    override fun onStart() {
        super.onStart()
        val width = requireActivity().resources.displayMetrics.widthPixels * .9
        val height = requireActivity().resources.displayMetrics.heightPixels * .45
        dialog?.window?.apply {
            setLayout(width.toInt(), height.toInt())
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        progressDialog = ProgressDialog(requireContext())
        val dialog = Dialog(requireContext())
        this.ctx = requireContext()
        dialog.setContentView(R.layout.dialog_authenticate_session)
        dialog.setCancelable(false)
        val et2faPass = dialog.findViewById<EditText>(R.id.taskEditText)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val ivBack = dialog.findViewById<ImageView>(R.id.iv_back_invoice)
        ivBack.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            val twoFaString = et2faPass.text.toString()
            if (twoFaString.isEmpty()) {
                showToast("Enter 2FA Password")
            } else {
                //Get Session
                dialog.dismiss()
                progressDialog.show()
                progressDialog.setCancelable(false)
                progressDialog.setCanceledOnTouchOutside(false)
                getSessionToken(twoFaString)
            }
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        return dialog
    }

    private fun getSessionToken(twoFaCode: String) {
        val call = webservice.get_session("merchant", "haiww82uuw92iiwu292isk")
        call.enqueue(object : Callback<get_session_response?> {
            override fun onResponse(
                call: Call<get_session_response?>,
                response: Response<get_session_response?>
            ) {
                if (response.body() != null) {
                    val resp = response.body()
                    if (resp != null && resp.session_token.toInt() != -1) {
                        sharedPreferences.setvalueofExpierTime(
                            resp.session_token.toInt(),
                            ctx
                        )
                        val token =
                            sharedPreferences.getvalueofRefresh(
                                "refreshToken",
                                ctx
                            )
                        getToken(token, twoFaCode)
                    } else {
                        progressDialog.dismiss()
                    }
                } else {
                    progressDialog.dismiss()
                    try {
                        Toast.makeText(
                            ctx,
                            response.errorBody()!!.string(),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: IOException) {
                        showToast(e.message)
                    }
                }
            }

            override fun onFailure(call: Call<get_session_response?>, t: Throwable) {
                Log.e("get-funding-nodes:", t.message!!)
                progressDialog.dismiss()
                showToast(t.message)

            }
        })
    }

    private fun getToken(refresh: String, key: String) {
        val time = CustomSharedPreferences().getvalueofExpierTime(ctx)
        val jsonObject1 = JsonObject()
        jsonObject1.addProperty("refresh", refresh)
        jsonObject1.addProperty("twoFactor", key)
        jsonObject1.addProperty("time", time)
        val call = apiClient.gettoken(jsonObject1) as Call<WebSocketOTPresponse>
        call.enqueue(object : Callback<WebSocketOTPresponse?> {
            override fun onResponse(
                call: Call<WebSocketOTPresponse?>,
                response: Response<WebSocketOTPresponse?>
            ) {
                progressDialog.dismiss()
                if (response.body() != null) {
                    val webSocketOTPresponse = response.body()
                    if (webSocketOTPresponse!!.code == 700) {
                        sharedPreferences.setislogin(true, "registered", ctx)
                        if (webSocketOTPresponse.token != "") {
                            sharedPreferences.setvalueofaccestoken(
                                webSocketOTPresponse.token,
                                "accessToken",
                                ctx
                            )
                        }
                        showToast("Access token successfully registered")
                    } else if (webSocketOTPresponse.code == 701) {
                        showToast("Missing 2FA code when requesting an access token")
                    } else if (webSocketOTPresponse.code == 702) {
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
                        showToast(webSocketOTPresponse.code.toString() + ": " + webSocketOTPresponse.message)
                    } else if (webSocketOTPresponse.code == 725) {
                        showToast("Misc websocket error, \"message\" field will include more data")
                    }
                }
            }

            override fun onFailure(call: Call<WebSocketOTPresponse?>, t: Throwable) {
                progressDialog.dismiss()
                Log.e("get-funding-nodes:", t.message!!)
                showToast(t.message)

            }
        })
    }

    fun showToast(message: String?) =
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()

    companion object {
        private var INSTANCE: Auth2FaFragment? = null
        fun getInstance(): Auth2FaFragment = INSTANCE ?: run {
            INSTANCE = Auth2FaFragment()
            return INSTANCE!!
        }
    }
}