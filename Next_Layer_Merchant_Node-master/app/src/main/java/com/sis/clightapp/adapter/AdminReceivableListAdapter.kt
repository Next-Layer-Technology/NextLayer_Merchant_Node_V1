package com.sis.clightapp.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.sis.clightapp.R
import com.sis.clightapp.fragments.printing.PrintDialogFragment
import com.sis.clightapp.model.GsonModel.Invoice
import com.sis.clightapp.util.AppConstants
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.roundToInt


class AdminReceivableListAdapter(
    private val activity: FragmentActivity,
    private val salesList: List<Invoice>
) : ArrayAdapter<Invoice?>(
    activity, 0, salesList
) {
    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null)
            view = LayoutInflater.from(activity)
                .inflate(R.layout.merchant_sale_list_item_layout, parent, false)
        view?.apply {
            val currentSale = salesList[position]
            val salelabel = findViewById<TextView>(R.id.labelval)
            salelabel.text = currentSale.label.toString()
            val amountsat = findViewById<TextView>(R.id.amountsatval)
            amountsat.text = excatFigure(satoshiToBtc(currentSale.msatoshi)) + "BTC"
            val paidat = findViewById<TextView>(R.id.paidatval)
            paidat.text =
                getDateFromUTCTimestamp(currentSale.paid_at, AppConstants.OUTPUT_DATE_FORMATE)
            val description = findViewById<TextView>(R.id.descriptionval)
            description.text = currentSale.description

            //Invoice id=BOlt11 hex string
            val bolt11invoiceid = findViewById<ImageView>(R.id.boltinvoiceidval)
            if (currentSale.bolt11 != null) bolt11invoiceid.setImageBitmap(
                getBitMapFromHex(
                    currentSale.bolt11
                )
            )
            //payment pre image = payhash
            val paymentpreimage = findViewById<ImageView>(R.id.paypreimageval)
            if (currentSale.payment_preimage != null) paymentpreimage.setImageBitmap(
                getBitMapFromHex(
                    currentSale.payment_preimage
                )
            )
        }
        return view!!
    }

    private fun getDateFromUTCTimestamp(mTimestamp: Long, format: String?): String? {
        var date: String? = null
        try {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("CST"))
            cal.timeInMillis = mTimestamp * 1000L
            date = DateFormat.format(format, cal.timeInMillis).toString()
            val formatter = SimpleDateFormat(format, Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("CST")
            return date
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date
    }

    private fun getBitMapFromHex(hex: String?): Bitmap {
        val multiFormatWriter = MultiFormatWriter()
        var bitMatrix: BitMatrix? = null
        try {
            bitMatrix = multiFormatWriter.encode(hex, BarcodeFormat.QR_CODE, 600, 600)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.createBitmap(bitMatrix)
    }

    private fun satoshiToBtc(msatoshhi: Double): Double {
        val msatoshiToSatoshi = msatoshhi / AppConstants.satoshiToMSathosi
        return msatoshiToSatoshi / AppConstants.btcToSathosi
    }

    fun excatFigure(value: Double): String {
        val d = BigDecimal(value.toString())
        return d.toPlainString()
    }

    companion object {
        fun round(value: Double, places: Int): Double {
            var rounded = value
            require(places >= 0)
            val factor = 10.0.pow(places.toDouble()).toLong()
            rounded *= factor
            val tmp = value.roundToInt()
            return tmp.toDouble() / factor
        }
    }
}