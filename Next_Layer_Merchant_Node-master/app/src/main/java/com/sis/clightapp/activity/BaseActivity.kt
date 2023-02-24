package com.sis.clightapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.sis.clightapp.fragments.shared.Auth2FaFragment
import com.sis.clightapp.services.SessionService
import com.sis.clightapp.session.MyLogOutService
import com.sis.clightapp.util.CustomSharedPreferences
import com.sis.clightapp.util.GlobalState
import org.koin.android.ext.android.inject

open class BaseActivity : AppCompatActivity(), LifecycleOwner {
    val sessionService: SessionService by inject()
    val IS_USER_LOGIN = "isuserlogin"
    val LASTDATE = "lastdate"
    val THORSTATUS = "thorstatus"
    val LIGHTNINGSTATUS = "lightningstatus"
    val BITCOINSTATUS = "bitcoinstatus"
    val ISALLSERVERUP = "isallserverup"
    lateinit var sharedPreferences: CustomSharedPreferences
    lateinit var dialog: ProgressDialog
    lateinit var loginDialog: ProgressDialog
    var lat = 0.0
    var lon = 0.0
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var wayLatitude = 0.0
    private var wayLongitude = 0.0
    private val locationRequest: LocationRequest? = null
    private val locationCallback: LocationCallback? = null
    lateinit var loginLoadingProgressDialog: ProgressDialog

    public override fun onStart() {
        super.onStart()
        startService(Intent(this, MyLogOutService::class.java))
        sessionService.isExpired.observe(this) { expired: Boolean ->
            val auth2FaFragment = Auth2FaFragment.getInstance()
            if (expired) {
                val prev = supportFragmentManager.findFragmentByTag("2fa_fragment")
                if (prev == null) {
                    auth2FaFragment.show(supportFragmentManager, "2fa_fragment")
                } else {
                    auth2FaFragment.dismiss()
                }
            } else {
                auth2FaFragment.dismiss()
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, MyLogOutService::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun initView() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = CustomSharedPreferences()
    }

    fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun setTextWithSpan(textView: TextView, text: String, spanText: String, style: StyleSpan?) {
        val sb = SpannableStringBuilder(text)
        val start = text.indexOf(spanText)
        val end = start + spanText.length
        sb.setSpan(style, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textView.text = sb
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    mFusedLocationClient!!.lastLocation.addOnSuccessListener(this) { location: Location? ->
                        if (location != null) {
                            wayLatitude = location.latitude
                            wayLongitude = location.longitude
                            lat = wayLatitude
                            lon = wayLongitude
                            Log.e("BaseActivity", "Latitude:$wayLatitude Longitude: $wayLongitude")
                            GlobalState.getInstance().setLattitude(wayLatitude.toString())
                            GlobalState.getInstance().longitude = wayLongitude.toString()
                        } else {
                            mFusedLocationClient!!.requestLocationUpdates(
                                locationRequest!!,
                                locationCallback!!,
                                Looper.getMainLooper()
                            )
                        }
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            123 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkForPermission()
    }

    fun checkForPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                val alertBuilder = AlertDialog.Builder(this)
                alertBuilder.setCancelable(true)
                alertBuilder.setTitle("Permission necessary")
                alertBuilder.setMessage("Write Storage permission is necessary for using this App!!!")
                alertBuilder.setPositiveButton(
                    "Yes"
                ) { dialog: DialogInterface?, which: Int ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, 123)
                }
                val alert = alertBuilder.create()
                alert.show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    123
                )
            }
        }
    }
}