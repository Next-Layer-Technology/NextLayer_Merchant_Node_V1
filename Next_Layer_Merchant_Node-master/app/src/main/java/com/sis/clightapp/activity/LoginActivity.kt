package com.sis.clightapp.activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.R
import com.sis.clightapp.fragments.shared.ExitDialogFragment
import com.sis.clightapp.model.REST.Loginresponse
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {
    private val webservice: Webservice by inject()

    private var role: String? = ""
    private lateinit var loginbtn: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    lateinit var setTextWithSpan: TextView
    private var merchantId = ""
    override fun onBackPressed() {
        ExitDialogFragment {
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            intent.putExtra("isFromLogin", true)
            startActivity(intent)
            finish()
            null
        }.show(supportFragmentManager, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)
        setTextWithSpan = findViewById(R.id.imageView3)
        val boldStyle = StyleSpan(Typeface.BOLD)
        setTextWithSpan(
            setTextWithSpan,
            getString(R.string.welcome_text),
            getString(R.string.welcome_text_bold),
            boldStyle
        )
        dialog = ProgressDialog(this@LoginActivity)
        dialog.setMessage("Connecting...")
        loginDialog = ProgressDialog(this@LoginActivity)
        loginDialog.setMessage("Logging In")
        loginLoadingProgressDialog = ProgressDialog(this@LoginActivity)
        loginLoadingProgressDialog.setMessage("Logging In")
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etEmail.setText("merchant")
        etPassword.setText("abc123")
        loginbtn = findViewById(R.id.btn_login)
        if (sessionService.getMerchantData() != null)
            merchantId = sessionService.getMerchantData()!!.merchant_id.toString()
        val iin = intent
        val b = iin.extras
        if (b != null) {
            role = b["role"] as String?
        }
        loginbtn.setOnClickListener {
            val strEmail = etEmail.text.toString()
            val strPassword = etPassword.text.toString()
            if (strEmail.isEmpty()) {
                showToast(getString(R.string.empty))
                return@setOnClickListener
            }
            if (strPassword.isEmpty()) {
                return@setOnClickListener
            }
            setlogin(strEmail, strPassword)
        }
    }

    private fun setlogin(strEmail: String, strPassword: String) {
        when (role) {
            "admin" -> onLoginClicked(merchantId, strEmail, strPassword, "Admin")
            "merchant" -> onLoginClicked(merchantId, strEmail, strPassword, "Merchant")
            "checkout" -> onLoginClicked(merchantId, strEmail, strPassword, "Checkout")
        }
    }

    private fun onLoginClicked(merchantId: String, name: String, password: String, type: String) {
        val call: Call<Loginresponse> = webservice.merchantsuser_login(merchantId, name, password, type)
        call.enqueue(object : Callback<Loginresponse?> {
            override fun onResponse(call: Call<Loginresponse?>, response: Response<Loginresponse?>) {
                if (response.body() != null) {
                    val loginresponse = response.body()
                    if (loginresponse!!.message == "successfully done") {
                        if (loginresponse.loginData != null) {
                            sharedPreferences.setBoolean(true, IS_USER_LOGIN, this@LoginActivity)
                            when (loginresponse.loginData.user_type) {
                                "Checkout" -> {
                                    val i = Intent(applicationContext, CheckOutMainActivity::class.java)
                                    startActivity(i)
                                }

                                "Admin" -> {
                                    sharedPreferences.setBoolean(true, IS_USER_LOGIN, this@LoginActivity)
                                    val i = Intent(applicationContext, AdminMainActivity::class.java)
                                    startActivity(i)
                                }

                                "Merchant" -> {
                                    sharedPreferences.setBoolean(true, IS_USER_LOGIN, this@LoginActivity)
                                    val i = Intent(applicationContext, MerchantMainActivity::class.java)
                                    startActivity(i)
                                }

                                else -> showToast("text mismatch")
                            }
                        } else {
                            showToast("Response empty")
                        }
                    } else {
                        showToast("Invalid User Name Or Password")
                    }
                }
            }

            override fun onFailure(call: Call<Loginresponse?>, t: Throwable) {
                Log.e("get-funding-nodes:", t.message.toString())
                showToast(t.message)
            }
        })
    }
}