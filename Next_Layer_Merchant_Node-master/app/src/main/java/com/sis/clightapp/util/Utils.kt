package com.sis.clightapp.util

import android.content.Context
import android.graphics.Bitmap
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

fun satoshiToBtc(msatoshhi: Double): Double {
    val msatoshiToSatoshi = msatoshhi / AppConstants.satoshiToMSathosi
    return msatoshiToSatoshi / AppConstants.btcToSathosi
}

const val QR_CODE = "QR_CODE"
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

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}


fun getBitMapFromHex(hex: String?, width: Int = 600, height: Int = 600): Bitmap? {
    val multiFormatWriter = MultiFormatWriter()
    var bitMatrix: BitMatrix? = null
    try {
        Log.d(QR_CODE,hex!!)
        bitMatrix = multiFormatWriter.encode(hex, BarcodeFormat.QR_CODE, width, height)
    } catch (e: WriterException) {
        e.printStackTrace()
    }
    val barcodeEncoder = BarcodeEncoder()
    return barcodeEncoder.createBitmap(bitMatrix)
}