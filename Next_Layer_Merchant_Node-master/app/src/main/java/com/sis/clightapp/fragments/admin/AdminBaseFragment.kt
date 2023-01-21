package com.sis.clightapp.fragments.admin

import android.app.ProgressDialog
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
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

open class AdminBaseFragment : Fragment() {
    lateinit var confirmingProgressDialog: ProgressDialog
    lateinit var sharedPreferences: CustomSharedPreferences
    val TAG = "AdminBaseFragment"

    override fun onResume() {
        super.onResume()
        sharedPreferences = CustomSharedPreferences()
        confirmingProgressDialog = ProgressDialog(requireContext())
        confirmingProgressDialog.setMessage("Confirming...")
        confirmingProgressDialog.setCancelable(false)
        confirmingProgressDialog.setCanceledOnTouchOutside(false)
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

    fun mSatoshoToBtc(msatoshhi: Double): Double {
        val msatoshiToSatoshi: Double = msatoshhi / AppConstants.satoshiToMSathosi
        return msatoshiToSatoshi / AppConstants.btcToSathosi
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
            val formatter = SimpleDateFormat(mDateFormate, Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("CST")
            val value = formatter.parse(date)
            val dateFormatter = SimpleDateFormat(mDateFormate, Locale.US)
            dateFormatter.timeZone = TimeZone.getDefault()
            date = dateFormatter.format(Objects.requireNonNull(value))
            return date
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date
    }

    val unixTimeStamp: String
        get() {
            val tsLong = System.currentTimeMillis() / 1000
            return tsLong.toString()
        }

    companion object {
        fun round(value: Double, places: Int): Double {
            var value = value
            require(places >= 0)
            val factor = Math.pow(10.0, places.toDouble()).toLong()
            value *= factor
            val tmp = value.roundToInt()
            return tmp.toDouble() / factor
        }
    }
}