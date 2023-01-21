package com.sis.clightapp.adapter

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.sis.clightapp.R
import com.sis.clightapp.model.GsonModel.Refund
import com.sis.clightapp.util.AppConstants
import com.sis.clightapp.util.getBitMapFromHex
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

//TODO: Same as the AdminSendablesListAdapter
class MerchantRefundsListAdapter(private val mContext: Context, @LayoutRes list: ArrayList<Refund>) :
    ArrayAdapter<Refund?>(
        mContext, 0, list as List<Refund?>
    ) {
    private var refundsList: List<Refund> = ArrayList()
    val scope = CoroutineScope(Job() + Dispatchers.Main)

    init {
        refundsList = list
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        if (listItem == null) listItem =
            LayoutInflater.from(mContext).inflate(R.layout.merchant_refund_list_item_layout, parent, false)
        val currentRefund = refundsList[position]
        val time = listItem!!.findViewById<View>(R.id.timeval) as TextView
        time.text = getDateFromUTCTimestamp(currentRefund.created_at, AppConstants.OUTPUT_DATE_FORMATE)
        val amountsat = listItem.findViewById<View>(R.id.amountsatval) as TextView
        amountsat.text =
            excatFigure(round(mSatoshoToBtc(currentRefund.msatoshi), 9)) + "BTC"
        val bolt11InvoiceID = listItem.findViewById<View>(R.id.boltval) as ImageView
        val paymenthash = listItem.findViewById<View>(R.id.paymenthashval) as ImageView

        scope.launch {
            if (currentRefund.bolt11 != null) {
                val bitmap = getBitMapFromHex(currentRefund.bolt11,200,200)
                withContext(Dispatchers.Main) {
                    bolt11InvoiceID.setImageBitmap(bitmap)
                }
            }
        }
        scope.launch {
            val bitmap = getBitMapFromHex(currentRefund.payment_hash,200,200)
            if (currentRefund.payment_hash != null) {
                withContext(Dispatchers.Main) {
                    paymenthash.setImageBitmap(bitmap)
                }
            }
        }
        return listItem
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

//            SimpleDateFormat dateFormatter = new SimpleDateFormat(mDateFormate);
//            dateFormatter.setTimeZone(TimeZone.getTimeZone("CST"));
//            date = dateFormatter.format(value);
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

    fun excatFigure(value: Double): String {
        val d = BigDecimal(value.toString())
        return d.toPlainString()
    }

    companion object {
        fun round(value: Double, places: Int): Double {
            var value = value
            require(places >= 0)
            val factor = Math.pow(10.0, places.toDouble()).toLong()
            value = value * factor
            val tmp = Math.round(value)
            return tmp.toDouble() / factor
        }
    }
}