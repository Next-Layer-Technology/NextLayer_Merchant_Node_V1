package com.sis.clightapp.fragments.checkout

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import com.sis.clightapp.Interface.ApiPaths2
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.R
import com.sis.clightapp.model.REST.get_session_response
import com.sis.clightapp.model.WebsocketResponse.WebSocketOTPresponse
import com.sis.clightapp.util.CustomSharedPreferences
import com.sis.clightapp.util.GlobalState
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.math.BigDecimal

open class CheckOutBaseFragment : Fragment() {
    /**
     * Could handle back press.
     *
     * @return true if back press was handled
     */

    private val apiClient: ApiPaths2 by inject()
    private val webservice: Webservice by inject()

    lateinit var addItemprogressDialog: ProgressDialog
    var getItemListprogressDialog: ProgressDialog? = null

    var exitFromServerProgressDialog: ProgressDialog? = null
    var createInvoiceProgressDialog: ProgressDialog? = null
    var confirmInvoicePamentProgressDialog: ProgressDialog? = null
    var connectCLiChannel: ProgressDialog? = null
    var updatingInventoryProgressDialog: ProgressDialog? = null
    var fContext: Context? = null
    private var confirmingProgressDialog: ProgressDialog? = null
    private var sharedPreferences: CustomSharedPreferences? = null
    fun showToast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun setTextWithSpan(textView: TextView, text: String, spanText: String, style: StyleSpan?) {
        val sb = SpannableStringBuilder(text)
        val start = text.indexOf(spanText)
        val end = start + spanText.length
        sb.setSpan(style, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textView.text = sb
    }

    val unixTimeStamp: String
        get() {
            val tsLong = System.currentTimeMillis() / 1000
            return tsLong.toString()
        }

    fun getBtcFromUsd(usd: Double): Double {
        var ret = 0.0
        ret = if (GlobalState.getInstance().currentAllRate != null) {
            Log.e("usdbefore", usd.toString())
            val btcRatePerDollar = 1 / GlobalState.getInstance().currentAllRate.usd.last
            val priceInBTC = btcRatePerDollar * usd
            Log.e("usdaftertobtc", priceInBTC.toString())
            priceInBTC
        } else {
            0.0
        }
        return ret
    }

    fun getTaxOfBTC(btc: Double): Double {
        var taxamount = 0.0
        if (GlobalState.getInstance().tax != null) {
            val t = GlobalState.getInstance().tax
            var taxprcntBTC = GlobalState.getInstance().tax.taxpercent / 100
            taxprcntBTC = taxprcntBTC * btc
            //            double taxprcntUSD=GlobalState.getInstance().getTax().getTaxpercent()/100;
//            taxprcntUSD=1*taxprcntUSD;
            taxamount = taxprcntBTC
        } else {
            taxamount = 0.0
        }
        return taxamount
    }

    fun getTaxOfUSD(usd: Double): Double {
        var taxamount = 0.0
        if (GlobalState.getInstance().tax != null) {
            var taxprcntUSD = GlobalState.getInstance().tax.taxpercent / 100
            taxprcntUSD = usd * taxprcntUSD
            taxamount = taxprcntUSD
        } else {
            taxamount = 0.0
        }
        return taxamount
    }

    override fun onResume() {
        super.onResume()
        fContext = context
        sharedPreferences = CustomSharedPreferences()
        confirmingProgressDialog = ProgressDialog(fContext)
        confirmingProgressDialog!!.setMessage("Confirming...")
        confirmingProgressDialog!!.setCancelable(false)
        confirmingProgressDialog!!.setCanceledOnTouchOutside(false)
    }

    fun goTo2FaPasswordDialog() {
        val enter2FaPassDialog = Dialog(requireContext())
        enter2FaPassDialog.setContentView(R.layout.dialog_authenticate_session)
        enter2FaPassDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        enter2FaPassDialog.setCancelable(false)
        val et2faPass = enter2FaPassDialog.findViewById<EditText>(R.id.taskEditText)
        val btnConfirm = enter2FaPassDialog.findViewById<Button>(R.id.btn_confirm)
        val btnCancel = enter2FaPassDialog.findViewById<Button>(R.id.btn_cancel)
        val ivBack = enter2FaPassDialog.findViewById<ImageView>(R.id.iv_back_invoice)
        ivBack.setOnClickListener {
            enter2FaPassDialog.dismiss()
        }
        btnConfirm.setOnClickListener { v: View? ->
            // closeSoftKeyBoard();
            val twoFaString = et2faPass.text.toString()
            if (twoFaString.isEmpty()) {
                showToast("Enter 2FA Password")
            } else {
                //Get Session
                enter2FaPassDialog.dismiss()
                confirmingProgressDialog!!.show()
                confirmingProgressDialog!!.setCancelable(false)
                confirmingProgressDialog!!.setCanceledOnTouchOutside(false)
                getSessionToken(twoFaString)
            }
        }
        btnCancel.setOnClickListener { enter2FaPassDialog.dismiss() }
        enter2FaPassDialog.show()
    }

    private fun getSessionToken(twoFaCode: String) {
        val call: Call<get_session_response> = webservice
            .get_session("merchant", "haiww82uuw92iiwu292isk")
        call.enqueue(object : Callback<get_session_response?> {
            override fun onResponse(call: Call<get_session_response?>, response: Response<get_session_response?>) {
                if (response.body() != null) {
                    val loginresponse = response.body()
                    if (loginresponse!!.session_token.toInt() != -1) {
                        //callRefresh(accessToken, twoFaCode, loginresponse.getSession_token());
                        CustomSharedPreferences().setvalueofExpierTime(loginresponse.session_token.toInt(), fContext)
                        val RefToken = CustomSharedPreferences().getvalueofRefresh("refreshToken", fContext)
                        getToken(RefToken, twoFaCode)
                    } else {
                        confirmingProgressDialog!!.dismiss()
                        showToast("Response empty")
                    }
                } else {
                    confirmingProgressDialog!!.dismiss()
                    try {
                        showToast(response.errorBody()!!.string())
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

    private fun getToken(refresh: String, twofactor_key: String) {
        val time = CustomSharedPreferences().getvalueofExpierTime(fContext)
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
                        sharedPreferences!!.setislogin(true, "registered", fContext)
                        if (webSocketOTPresponse.token != "") {
                            sharedPreferences!!.setvalueofaccestoken(
                                webSocketOTPresponse.token,
                                "accessToken",
                                fContext
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
                confirmingProgressDialog!!.dismiss()
                Log.e("get-funding-nodes:", t.message!!)
            }
        })
    }

    companion object {
        fun removeLastChars(str: String, chars: Int): String {
            return str.substring(0, str.length - chars)
        }

        @JvmStatic
        fun excatFigure2(value: Double): String {
            val d = BigDecimal(value.toString())
            return d.toPlainString()
        }
    }
}