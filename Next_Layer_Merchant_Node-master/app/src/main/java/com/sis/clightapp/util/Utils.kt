package com.sis.clightapp.util

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

class Utils {
    companion object {
        fun satoshiToBtc(msatoshhi: Double): Double {
            val msatoshiToSatoshi = msatoshhi / AppConstants.satoshiToMSathosi
            return msatoshiToSatoshi / AppConstants.btcToSathosi
        }

        fun btcToUsd(btc: Double): Double {
            val ret: Double = if (GlobalState.getInstance().channel_btcResponseData != null) {
                val btcRate = GlobalState.getInstance().channel_btcResponseData.price
                val priceInUSD = btcRate * btc
                priceInUSD
            } else {
                0.0
            }
            return ret
        }

        fun round(value: Double, places: Int): Double {
            var v = value
            require(places >= 0)
            val factor = 10.0.pow(places.toDouble()).toLong()
            v *= factor
            val tmp = v.roundToInt()
            return tmp.toDouble() / factor
        }

        fun dateStringUTCTimestamp(timestamp: Long, format: String?): String {
            try {
                val cal = Calendar.getInstance(TimeZone.getTimeZone(""))
                cal.timeInMillis = timestamp * 1000L
                var date = DateFormat.format(format, cal.timeInMillis).toString()
                val formatter = SimpleDateFormat(format, Locale.US)
                formatter.timeZone = TimeZone.getTimeZone("CST")
                val value = formatter.parse(date)
                val dateFormatter = SimpleDateFormat(format, Locale.US)
                dateFormatter.timeZone = TimeZone.getDefault()
                if (value != null) {
                    date = dateFormatter.format(value)
                }
                return date
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }
    }
}