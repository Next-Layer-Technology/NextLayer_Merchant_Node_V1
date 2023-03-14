package com.sis.clightapp.services

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.google.gson.Gson
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData
import java.util.*
import kotlin.concurrent.fixedRateTimer


class SessionService(private val context: Context) {
    private var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()
    private val _isExpired = MutableLiveData<Boolean>(false)
    val isExpired: LiveData<Boolean> get() = _isExpired;
    private fun isTokenExpired(token: String?): Boolean {
        val jwt: DecodedJWT = JWT.decode(token)
        return jwt.expiresAt.before(Date())
    }

    init {
        fixedRateTimer("jwt_token_check", false, 0L, 3 * 1000) {
            val token = prefs.getString("accessToken", null)
            if (token != null) {
                _isExpired.postValue(isTokenExpired(token))
            }
        }
    }

    fun getMerchantData(): MerchantData? {
        val data = prefs.getString("merchant_data", null)
        return if (data != null)
            gson.fromJson(data, MerchantData::class.java)
        else null
    }

    fun setMerchantData(data: MerchantData) {
        prefs.edit {
            putString("merchant_data", gson.toJson(data))
        }
    }
}