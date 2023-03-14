package com.sis.clightapp.fragments.merchant

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.sis.clightapp.util.AppConstants
import com.sis.clightapp.util.CustomSharedPreferences
import com.sis.clightapp.util.GlobalState
import com.sis.clightapp.util.QR_CODE
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

open class MerchantBaseFragment : Fragment() {
    /**
     * Could handle back press.
     *
     * @return true if back press was handled
     */
    @JvmField
    val THORSTATUS = "thorstatus"

    @JvmField
    val LIGHTNINGSTATUS = "lightningstatus"

    val BITCOINSTATUS = "bitcoinstatus"
    lateinit var sharedPreferences: CustomSharedPreferences
    override fun onResume() {
        super.onResume()
        sharedPreferences = CustomSharedPreferences()
    }

    fun showToast(message: String?) {
        if (message != null)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun setTextWithSpan(textView: TextView, text: String, spanText: String, style: StyleSpan?) {
        val sb = SpannableStringBuilder(text)
        val start = text.indexOf(spanText)
        val end = start + spanText.length
        sb.setSpan(style, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textView.text = sb
    }

    fun getDayDiffDates(millis1: Long, millis2: Long): Long {
        val diff = millis2 - millis1
        return diff / (24 * 60 * 60 * 1000)
    }

    val unixTimeStamp: String
        get() {
            val tsLong = System.currentTimeMillis() / 1000
            return tsLong.toString()
        }
    val unixTimeStampInLong: Long
        get() = System.currentTimeMillis() / 1000

    fun getDateInCorrectFormat(year: Int, monthOfYear: Int, dayOfMonth: Int): String {
        var date = ""
        var formatedMonth = ""
        var formatedDay = ""
        formatedMonth = if (monthOfYear < 9) {
            "0" + (monthOfYear + 1)
        } else {
            (monthOfYear + 1).toString()
        }
        formatedDay = if (dayOfMonth < 10) {
            "0$dayOfMonth"
        } else {
            dayOfMonth.toString()
        }
        date = "$formatedMonth-$formatedDay-$year"
        return date
    }

    fun getBitMapImg(hex: String?, widht: Int, height: Int): Bitmap {
        val multiFormatWriter = MultiFormatWriter()
        var bitMatrix: BitMatrix? = null
        try {
            Log.d(QR_CODE, hex!!)
            bitMatrix = multiFormatWriter.encode(hex, BarcodeFormat.QR_CODE, widht, height)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.createBitmap(bitMatrix)
    }

    fun excatFigure(value: Double): String {
        val d = BigDecimal(value.toString())
        return d.toPlainString()
    }

    fun getDateFromUTCTimestamp(mTimestamp: Long, mDateFormate: String?): String? {
        var date: String? = null
        try {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("CST"))
            cal.timeInMillis = mTimestamp * 1000L
            date = DateFormat.format(mDateFormate, cal.timeInMillis).toString()
            val formatter = SimpleDateFormat(mDateFormate)
            formatter.timeZone = TimeZone.getTimeZone("CST")
            val value = formatter.parse(date)
            val dateFormatter = SimpleDateFormat(mDateFormate)
            dateFormatter.timeZone = TimeZone.getDefault()
            date = dateFormatter.format(value)
            return date
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date
    }

    fun mSatoshoToBtc(msatoshhi: Double): Double {
        val msatoshiToSatoshi = msatoshhi / AppConstants.satoshiToMSathosi
        return msatoshiToSatoshi / AppConstants.btcToSathosi
    }

    companion object {
        const val DAY_In_MINUTES = (1000 * 60 * 1440 //5 minutes in milliseconds
                ).toLong()

        @JvmStatic
        fun getDateFromUTCTimestamp2(mTimestamp: Long, mDateFormate: String?): String? {
            var date: String? = null
            try {
                val cal = Calendar.getInstance(TimeZone.getTimeZone("CST"))
                cal.timeInMillis = mTimestamp * 1000L
                date = DateFormat.format(mDateFormate, cal.timeInMillis).toString()
                val formatter = SimpleDateFormat(mDateFormate)
                formatter.timeZone = TimeZone.getTimeZone("CST")
                val value = formatter.parse(date)
                val dateFormatter = SimpleDateFormat(mDateFormate)
                dateFormatter.timeZone = TimeZone.getDefault()
                date = dateFormatter.format(value)
                return date
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return date
        }

        @JvmStatic
        fun round(value: Double, places: Int): Double {
            var value = value
            require(places >= 0)
            val factor = Math.pow(10.0, places.toDouble()).toLong()
            value = value * factor
            val tmp = Math.round(value)
            return tmp.toDouble() / factor
        }

        @JvmStatic
        fun excatFigure2(value: Double): String {
            val d = BigDecimal(value.toString())
            return d.toPlainString()
        }
    }
}