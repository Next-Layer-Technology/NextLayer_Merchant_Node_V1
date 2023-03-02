package com.sis.clightapp.adapter

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sis.clightapp.R
import com.sis.clightapp.fragments.merchant.MerchantFragment2
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemLIstModel
import com.sis.clightapp.util.AppConstants
import com.sis.clightapp.util.getBitMapFromHex
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.*
import java.math.BigDecimal

class MerchantItemAdapter    // RecyclerView recyclerView;
    (
    private val itemsArrayList: ArrayList<ItemLIstModel>,
    private val mContext: Context,
    private val merchantFragment2: MerchantFragment2
) : RecyclerView.Adapter<MerchantItemAdapter.ViewHolder>() {
    val scope = CoroutineScope(Job() + Dispatchers.Main)

    fun updateList(itemList: List<ItemLIstModel>?) {
        itemsArrayList.clear()
        itemsArrayList.addAll(itemList!!)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem =
            layoutInflater.inflate(R.layout.merchant_inventroy_item_list2, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemsArrayList[position]
        holder.setIsRecyclable(false)
        val image_url = AppConstants.MERCHANT_ITEM_IMAGE + currentItem.image_path
        Log.d(this::class.simpleName, image_url)
        Glide.with(mContext).load(image_url).into(holder.imageView)
        holder.name.text = currentItem.name
        holder.price.text = "$" + excatFigure(round(currentItem.unit_price.toDouble(), 2))
        holder.quantity.text = currentItem.quantity_left

        holder.customRowLayout.setOnClickListener {
            val image = ImageView(mContext)

            scope.launch {
                val bitmap = getBitMapFromHex(currentItem.upc_code, 200, 200)
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        image.setImageBitmap(bitmap)
                    } else {
                        image.setImageResource(R.drawable.ic_launcher2)
                    }
                }
            }
            val builder =
                AlertDialog.Builder(mContext).setCancelable(false)
                    .setMessage("Item UPC:" + currentItem.upc_code)
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
                    .setView(image)
            builder.create().show()
        }
        holder.customRowLayout.setOnLongClickListener { //merchantFragment2.dialogBoxForUpdateItemMerchant2(currentItem.getName());
            merchantFragment2.dialogBoxForUpdateDelItem(currentItem)
            true
        }
    }

    override fun getItemCount(): Int {
        return itemsArrayList.size
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        var customRowLayout: RelativeLayout
        var imageView: CircleImageView
        var name: TextView
        var price: TextView
        var quantity: TextView

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        init {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            customRowLayout = itemView.findViewById<View>(R.id.linearLayout2) as RelativeLayout
            imageView = itemView.findViewById<View>(R.id.imageView) as CircleImageView
            name = itemView.findViewById<View>(R.id.textView_name3) as TextView
            price = itemView.findViewById<View>(R.id.price) as TextView
            quantity = itemView.findViewById<View>(R.id.textViewquatity) as TextView
        }
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