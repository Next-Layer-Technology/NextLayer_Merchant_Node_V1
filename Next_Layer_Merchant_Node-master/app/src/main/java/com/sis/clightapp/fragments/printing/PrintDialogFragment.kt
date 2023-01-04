package com.sis.clightapp.fragments.printing

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.sis.clightapp.R
import com.sis.clightapp.model.GsonModel.Invoice
import com.sis.clightapp.model.GsonModel.Items
import com.sis.clightapp.model.GsonModel.Pay
import com.sis.clightapp.util.*
import com.sis.clightapp.util.print.PrintPic
import com.sis.clightapp.util.print.PrinterCommands
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import kotlin.math.round


class PrintDialogFragment(
    private val invoice: Invoice? = null,
    private val payment: Pay? = null,
    private val items: List<Items> = listOf(),
) : DialogFragment() {

    private val requestCode = 2
    private lateinit var bluetoothAdapter: BluetoothAdapter
    var btReceiver: BroadcastReceiver? = null
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var printingProgressBar: ProgressDialog
    private lateinit var btDevicesDialog: Dialog
    private val permissions: MutableList<String> = ArrayList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        printingProgressBar = ProgressDialog(requireContext())
        printingProgressBar.setMessage("Printing...")
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        btDevicesDialog = Dialog(requireContext())
        btDevicesDialog.setContentView(R.layout.blutoothdevicelistdialoglayout)
        btDevicesDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        btDevicesDialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        //init dialog views
        val ivBack: ImageView = btDevicesDialog.findViewById(R.id.iv_back_invoice)
        val scanDevices: Button = btDevicesDialog.findViewById(R.id.btn_scanDevices)
        val btnClose: ImageView = btDevicesDialog.findViewById(R.id.btn_close)
        btnClose.setOnClickListener {
            dismiss();
        }
        initializeBluetooth()
        scanDevices.setOnClickListener {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            requireActivity().registerReceiver(btReceiver, filter)
            bluetoothAdapter.startDiscovery()
        }
        ivBack.setOnClickListener { btDevicesDialog.dismiss() }

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.BLUETOOTH)
        permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        ActivityCompat.requestPermissions(requireActivity(), permissions.toTypedArray(), 1)
        return btDevicesDialog
    }

    override fun onActivityResult(mRequestCode: Int, mResultCode: Int, mDataIntent: Intent?) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent)
        when (mRequestCode) {
            requestCode -> if (mResultCode == Activity.RESULT_OK) {
                initializeBluetooth()
            } else {
                Toast.makeText(requireContext(), "Message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initializeBluetooth() {
        if (Build.VERSION.SDK_INT >= 31 && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), permissions.toTypedArray(), 1)
        }
        val dialog: ProgressBar = btDevicesDialog.findViewById(R.id.printerProgress)
        dialog.visibility = View.VISIBLE
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, requestCode)
            return
        }
        val mPairedDevicesArrayAdapter =
            object : ArrayAdapter<BluetoothDevice>(requireContext(), R.layout.device_name) {
                @SuppressLint("MissingPermission")
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    var currentItemView: View? = convertView
                    if (currentItemView == null) {
                        currentItemView = LayoutInflater.from(context)
                            .inflate(android.R.layout.simple_list_item_1, parent, false);
                    }
                    val item = getItem(position)!!
                    (currentItemView as TextView?)?.let {
                        it.text = item.name.toString() + " - " + item.address
                    }
                    currentItemView?.setOnClickListener {
                        val tvStatus: TextView = btDevicesDialog.findViewById(R.id.tv_status)
                        try {
                            dialog.visibility = View.VISIBLE
                            tvStatus.text = "Device Status:Connecting...."
                            bluetoothAdapter.cancelDiscovery()
                            val device = bluetoothAdapter.getRemoteDevice(item.address)
                            try {
                                ConnectThread(device).start()
                                printingProgressBar.show()
                                tvStatus.text = "Status: Connecting"
                                dialog.visibility = View.GONE
                            } catch (eConnectException: IOException) {
                                tvStatus.text = "Status: Try Again"
                                dialog.visibility = View.GONE
                                Log.e("ConnectError", eConnectException.toString())
                            }
                        } catch (ex: java.lang.Exception) {
                            Log.e("ConnectError", ex.toString())
                        }
                    }
                    return currentItemView!!
                }
            }
        val blueDeviceListView: ListView =
            btDevicesDialog.findViewById(R.id.blueDeviceListView)
        blueDeviceListView.adapter = mPairedDevicesArrayAdapter
        val mPairedDevices =
            HashSet(bluetoothAdapter.bondedDevices)
        mPairedDevicesArrayAdapter.addAll(mPairedDevices)
        btReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device = intent
                        .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        mPairedDevices.add(device)
                        if (mPairedDevices.size > 0) {
                            mPairedDevicesArrayAdapter.clear()
                            for (mDevice in mPairedDevices) {
                                mPairedDevicesArrayAdapter.add(mDevice)
                            }
                        }
                        blueDeviceListView.adapter = mPairedDevicesArrayAdapter
                    }
                }
            }
        }
        dialog.visibility = View.GONE
    }

    companion object {
        const val TAG = "PrintDialogFragment"
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(uuid)
        }

        override fun run() {
            bluetoothAdapter.cancelDiscovery()
            mmSocket?.let { socket ->
                try {
                    socket.connect()
                    if (invoice != null) {
                        sendInvoice(socket)
                    } else {
                        sendPayment(socket)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Socket timeout")
                }
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.all {
                it == PackageManager.PERMISSION_GRANTED
            }) {
            initializeBluetooth()
        } else {
            Toast.makeText(requireContext(), "Please allow all permissions", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun sendInvoice(socket: BluetoothSocket) {
        Thread {
            try {

                val outputStream = socket.outputStream
                var bytes = byteArrayOf()
                bytes += PrinterCommands.reset

                // the text typed by the user
                val precision = DecimalFormat("0.00")
                if (invoice != null) {
                    val paidAt: String = dateStringUTCTimestamp(
                        invoice.paid_at,
                        AppConstants.OUTPUT_DATE_FORMATE
                    )
                    val btc =
                        String.format("%.9f", satoshiToBtc(invoice.msatoshi)) + " BTC"
                    val usd =
                        String.format("%.9f", btcToUsd(satoshiToBtc(invoice.msatoshi))) + " USD"
                    try {
                        // This is printer specific code you can comment ==== > Start
                        bytes += PrinterCommands.ESC_ALIGN_CENTER
                        bytes += "Sale/Incoming Funds".toByteArray()
                        bytes += feed(1)
                        bytes += PrinterCommands.ESC_ALIGN_CENTER
                        bytes += "---------------------".toByteArray()
                        bytes += feed()
                        bytes += PrinterCommands.ESC_ALIGN_LEFT
                        bytes += invoice.description.toByteArray()
                        bytes += feed(1)
                        if (items.isNotEmpty()) {
                            bytes += PrinterCommands.ESC_ALIGN_CENTER
                            bytes += "Items:".toByteArray()
                            bytes += feed();
                            items.forEach {
                                bytes += PrinterCommands.ESC_ALIGN_RIGHT
                                bytes += it.name.toByteArray()
                                bytes += feed()
                            }
                        }
                        bytes += PrinterCommands.ESC_ALIGN_LEFT
                        bytes += "Amount:".toByteArray()
                        //amountInBTC should right
                        bytes += PrinterCommands.ESC_ALIGN_RIGHT
                        bytes += btc.toByteArray()
                        //amountInBTC should right
                        bytes += "\t".toByteArray()
                        bytes += usd.toByteArray()
                        bytes += feed(1)
                        //Paid at title should center
                        bytes += PrinterCommands.ESC_ALIGN_CENTER
                        bytes += "Received:".toByteArray()
                        bytes += PrinterCommands.ESC_ALIGN_RIGHT
                        bytes += paidAt.toByteArray()
                        bytes += feed(1)
                        bytes += PrinterCommands.ESC_ALIGN_CENTER
                        bytes += "Bolt 11 Invoice:".toByteArray()
                        bytes += feed()
                        bytes += PrinterCommands.ESC_ALIGN_RIGHT
                        bytes += qr(invoice.bolt11)
                        bytes += feed(1)
                        bytes += PrinterCommands.ESC_ALIGN_CENTER
                        bytes += "Payment Hash:".toByteArray()
                        bytes += feed()
                        bytes += PrinterCommands.ESC_ALIGN_RIGHT
                        bytes += qr(invoice.payment_hash)
                        bytes += PrinterCommands.ESC_ALIGN_LEFT
                        bytes += feed(1)
                    } catch (e: Exception) {
                        Log.e("PrintError", "Exe ", e)
                    }
                } else {
                    bytes += feed()
                    val paidAt = "Not Data Found"
                    bytes += paidAt.toByteArray()
                }
                bytes += feed(1)
                bytes += PrinterCommands.FEED_PAPER_AND_CUT
                val bigNum = 2048
                var i = 0
                while (i < bytes.size) {
                    val b = if (i + bigNum < bytes.size) bigNum else bytes.size - i
                    outputStream.write(bytes, i, b)
                    outputStream.flush()
                    i += bigNum
                    Thread.sleep(250)
                }

                socket.close()
                requireActivity().runOnUiThread {
                    printingProgressBar.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Error printing")
            }
        }.start()
    }

    private fun sendPayment(socket: BluetoothSocket) {
        Thread {
            try {
                val outputStream = socket.outputStream
                var bytes = byteArrayOf()
                // the text typed by the user
                val precision = DecimalFormat("0.00")
                if (payment != null) {
                    val paidAt: String = dateStringUTCTimestamp(
                        payment.created_at.toLong(),
                        AppConstants.OUTPUT_DATE_FORMATE
                    )
                    val btc =
                        String.format("%.9f", satoshiToBtc(payment.msatoshi)) + " BTC"
                    val usd =
                        String.format("%.9f", btcToUsd(satoshiToBtc(payment.msatoshi))) + " USD"
                    val des = payment.message ?: ""
                    try {
                        // This is printer specific code you can comment ==== > Start
                        bytes += PrinterCommands.reset
                        bytes += PrinterCommands.INIT
                        bytes += feed(2)
                        bytes += "Refund / Outgoing".toByteArray()
                        bytes += feed(1)
                        bytes += "---------------------".toByteArray()
                        bytes += feed()
                        bytes += des.toByteArray()
                        bytes += feed(2)
                        bytes += "Amount: ".toByteArray()
                        bytes += feed()
                        bytes += btc.toByteArray()
                        bytes += feed()
                        bytes += usd.toByteArray()
                        bytes += feed(2)
                        //Paid at title should center
                        bytes += "Sent: ".toByteArray()
                        bytes += feed()
                        //Paid at   should center
                        bytes += paidAt.toByteArray()
                        bytes += feed(1)
                        bytes += PrinterCommands.ESC_ALIGN_LEFT
                        bytes += "Bolt 11 Invoice:".toByteArray()
                        bytes += feed()
                        bytes += PrinterCommands.ESC_ALIGN_RIGHT
                        bytes += qr(payment.bolt11)
                        bytes += feed()
                        bytes += "Payment Hash:".toByteArray()
                        bytes += feed()
                        bytes += PrinterCommands.ESC_ALIGN_RIGHT
                        bytes += qr(payment.payment_hash)
                        bytes += feed()
                        bytes += PrinterCommands.FEED_PAPER_AND_CUT
                    } catch (e: Exception) {
                        Log.e("PrintError", e.message.toString())
                    }
                } else {
                    bytes += PrinterCommands.reset
                    bytes += PrinterCommands.INIT
                    bytes += PrinterCommands.FEED_LINE
                    val paidAt = "Not Data Found"
                    bytes += paidAt.toByteArray()
                }
                bytes += PrinterCommands.FEED_PAPER_AND_CUT
                outputStream.write(bytes)
                socket.close()
                requireActivity().runOnUiThread {
                    printingProgressBar.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Error printing")
            }
        }.start()

    }

    private fun qr(text: String): ByteArray {
        val bitmap: Bitmap = getBitMapFromHex(text) ?: return byteArrayOf()
        val printPic = PrintPic.getInstance()
        printPic.init(bitmap)
        return printPic.printDraw()
    }

    private fun feed(lines: Int = 1): ByteArray {
        var bytes = byteArrayOf()
        for (i in 0..lines) {
            bytes += PrinterCommands.FEED_LINE
        }
        return bytes;
    }

    private fun getBitMapFromHex(hex: String?, width: Int = 200, height: Int = 200): Bitmap? {
        if (hex == null)
            return null
        val multiFormatWriter = MultiFormatWriter()
        var bitMatrix: BitMatrix? = null
        try {
            bitMatrix = multiFormatWriter.encode(hex, BarcodeFormat.QR_CODE, width, height)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.createBitmap(bitMatrix)
    }
}