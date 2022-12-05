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
import android.widget.AdapterView.OnItemClickListener
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.sis.clightapp.R
import com.sis.clightapp.util.AppConstants
import com.sis.clightapp.util.GlobalState
import com.sis.clightapp.util.print.PrintPic
import com.sis.clightapp.util.print.PrinterCommands
import com.sis.clightapp.util.Utils
import com.sis.clightapp.model.GsonModel.InvoiceForPrint
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.*

class PrintDialogFragment : DialogFragment() {
    private val requestCode = 2
    private lateinit var bluetoothAdapter: BluetoothAdapter
    var btReceiver: BroadcastReceiver? = null
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var printingProgressBar: ProgressDialog
    private lateinit var btDevicesDialog: Dialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        printingProgressBar = ProgressDialog(requireContext())
        printingProgressBar.setMessage("Printing...")
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        btDevicesDialog = Dialog(requireContext())
        btDevicesDialog.setContentView(R.layout.blutoothdevicelistdialoglayout)
        btDevicesDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        btDevicesDialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        btDevicesDialog.setCancelable(false)
        //init dialog views
        val ivBack: ImageView = btDevicesDialog.findViewById(R.id.iv_back_invoice)
        val scanDevices: Button = btDevicesDialog.findViewById(R.id.btn_scanDevices)
        initializeBluetooth()
        scanDevices.setOnClickListener { initializeBluetooth() }
        ivBack.setOnClickListener { btDevicesDialog.dismiss() }

        val permissions: MutableList<String> = ArrayList()
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
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val dialog: ProgressBar = btDevicesDialog.findViewById(R.id.printerProgress)
        dialog.visibility = View.VISIBLE
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, requestCode)
            return
        }

        bluetoothAdapter.startDiscovery()
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
                    return currentItemView!!
                }
            }
        val blueDeviceListView: ListView =
            btDevicesDialog.findViewById(R.id.blueDeviceListView)
        blueDeviceListView.adapter = mPairedDevicesArrayAdapter
        blueDeviceListView.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, mPosition: Int, _: Long ->
                val tvStatus: TextView = btDevicesDialog.findViewById(R.id.tv_status)
                try {
                    dialog.visibility = View.VISIBLE
                    tvStatus.text = "Device Status:Connecting...."
                    bluetoothAdapter.cancelDiscovery()
                    val device = bluetoothAdapter.getRemoteDevice(
                        mPairedDevicesArrayAdapter.getItem(mPosition)!!.address
                    )
                    try {
                        GlobalState.getInstance().invoiceForPrint = InvoiceForPrint().apply {
                            msatoshi = 1.0
                            paid_at = 12312312312312
                            purchasedItems = "Items"
                            tax = "1.0"
                            desscription = "Description"
                            created_at = 12312312312312.0
                            destination = "Destination"
                            payment_hash = "Hash"

                        }
                        ConnectThread(device).start()
                        printingProgressBar.show()
                        printingProgressBar.setCancelable(false)
                        printingProgressBar.setCanceledOnTouchOutside(false)
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
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val mPairedDevices =
            HashSet(bluetoothAdapter.bondedDevices)
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        mPairedDevicesArrayAdapter.addAll(mPairedDevices)
        btReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device = intent
                        .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        mPairedDevices.add(device)
                        mPairedDevicesArrayAdapter.clear()
                        if (mPairedDevices.size > 0) {
                            for (mDevice in mPairedDevices) {
                                mPairedDevicesArrayAdapter.add(mDevice)
                            }
                        }
                        blueDeviceListView.adapter = mPairedDevicesArrayAdapter
                    }
                }
            }
        }
        requireActivity().registerReceiver(btReceiver, filter)
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
                socket.connect()
                sendData(socket)
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

    private fun sendData(socket: BluetoothSocket) {
        Thread {
            try {
                val outputStream = socket.outputStream
                // the text typed by the user
                val recInvoiceForPrint = GlobalState.getInstance().invoiceForPrint
                val precision = DecimalFormat("0.00")
                if (recInvoiceForPrint != null) {
                    val paidAt: String = Utils.dateStringUTCTimestamp(
                        recInvoiceForPrint.paid_at,
                        AppConstants.OUTPUT_DATE_FORMATE
                    )
                    val amountInBtc: String = Utils.round(
                        Utils.satoshiToBtc(recInvoiceForPrint.msatoshi),
                        9
                    ).toString() + " BTC"
                    val amountInUsd = precision.format(
                        Utils.round(
                            Utils.btcToUsd(
                                Utils.satoshiToBtc(recInvoiceForPrint.msatoshi)
                            ), 2
                        )
                    ) + " USD"
                    val des = recInvoiceForPrint.purchasedItems
                    val bitmap: Bitmap? = getBitMapFromHex(recInvoiceForPrint.payment_preimage)
                    try {
                        // This is printer specific code you can comment ==== > Start
                        outputStream.write(PrinterCommands.reset)
                        outputStream.write(PrinterCommands.INIT)
                        outputStream.write("\n\n".toByteArray())
                        outputStream.write("    Sale / Incoming Funds".toByteArray())
                        outputStream.write("\n".toByteArray())
                        outputStream.write("    ---------------------".toByteArray())
                        outputStream.write("\n".toByteArray())
                        outputStream.write(des.toByteArray())
                        outputStream.write("\n\n".toByteArray())
                        outputStream.write("\tAmount: ".toByteArray())
                        outputStream.write("\n\t".toByteArray())
                        //amountInBTC should right
                        outputStream.write(amountInBtc.toByteArray())
                        outputStream.write("\n\t".toByteArray())
                        //amountInBTC should right
                        outputStream.write(amountInUsd.toByteArray())
                        outputStream.write("\n".toByteArray())
                        outputStream.write("\n".toByteArray())
                        //Paid at title should center
                        outputStream.write("\tReceived:".toByteArray())
                        outputStream.write("\n  ".toByteArray())
                        //Paid at   should center
                        outputStream.write("  ".toByteArray())
                        outputStream.write(paidAt.toByteArray())
                        outputStream.write("\n\n".toByteArray())
                        outputStream.write("\tPayment Hash:".toByteArray())
                        outputStream.write(PrinterCommands.FEED_LINE);
                        if (bitmap != null) {
                            val bMapScaled = Bitmap.createScaledBitmap(bitmap, 250, 250, true)
                            ByteArrayOutputStream()
                            val printPic = PrintPic.getInstance()
                            printPic.init(bMapScaled)
                            val bitmapdata = printPic.printDraw()
                            outputStream.write(PrinterCommands.print)
                            outputStream.write(bitmapdata)
                            outputStream.write(PrinterCommands.print)
                            outputStream.write("\n\n".toByteArray())
                        }
                        outputStream.write(PrinterCommands.FEED_PAPER_AND_CUT)
                        Thread.sleep(1000)
                    } catch (e: java.lang.Exception) {
                        Log.e("PrintError", "Exe ", e)
                    }
                } else {
                    outputStream.write(PrinterCommands.reset)
                    outputStream.write(PrinterCommands.INIT)
                    outputStream.write(PrinterCommands.FEED_LINE)
                    val paidAt = "Not Data Found"
                    outputStream.write(paidAt.toByteArray())
                }
                socket.close()
                requireActivity().runOnUiThread {
                    printingProgressBar.dismiss()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Log.e(TAG, "Error printing")
            }

        }.start()
    }


    private fun getBitMapFromHex(hex: String?): Bitmap? {
        if (hex == null)
            return null
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

}