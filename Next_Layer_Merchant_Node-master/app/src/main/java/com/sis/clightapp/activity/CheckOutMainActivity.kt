package com.sis.clightapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.navigation.NavigationView
import com.sis.clightapp.R
import com.sis.clightapp.ViewPager.CustomViewPager
import com.sis.clightapp.ViewPager.FragmentAdapter
import com.sis.clightapp.fragments.checkout.CheckOutFragment1
import com.sis.clightapp.fragments.checkout.CheckOutsFragment2
import com.sis.clightapp.fragments.checkout.CheckOutsFragment3
import com.sis.clightapp.fragments.shared.ExitDialogFragment
import com.sis.clightapp.session.MyLogOutService
import com.sis.clightapp.util.GlobalState
import java.util.*

class CheckOutMainActivity : BaseActivity() {
    private var drawerLayout: DrawerLayout? = null
    private var customViewPager: CustomViewPager? = null
    var TAG = "CheckOutMainActivity"
    override fun onDestroy() {
        finish()
        super.onDestroy()
        Runtime.getRuntime().gc()
        System.gc()
        stopService(Intent(this, MyLogOutService::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out_main11)
        Log.e(TAG, "Mode:Login As UserMode/CheckOut")
        initView()
        initViewPagerAdapter()
        configureNavigationDrawer()
        val toolbar = findViewById<Toolbar>(R.id.toolbarcheckout)
        val navImg = toolbar.findViewById<ImageView>(R.id.imageView9)
        val cartIconImg = toolbar.findViewById<RelativeLayout>(R.id.imageView8)
        cartIconImg.setOnClickListener { view: View? -> customViewPager!!.currentItem = 2 }
        navImg.setOnClickListener { view: View? ->
            val drawer = findViewById<DrawerLayout>(R.id.checkoutdrawer_layout)
            drawer.openDrawer(GravityCompat.START)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == android.R.id.home) {
            drawerLayout!!.openDrawer(GravityCompat.START)
            return true
        }
        return true
    }

    private fun initView() {
        customViewPager = findViewById(R.id.custom_view_pager)
        drawerLayout = findViewById(R.id.checkoutdrawer_layout)
    }

    private fun initViewPagerAdapter() {
        customViewPager!!.setPagingEnabled(false)
        val pagerAdapter = FragmentAdapter(
            supportFragmentManager,
            FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            fragmentList
        )
        customViewPager!!.adapter = pagerAdapter
        customViewPager!!.offscreenPageLimit = 5
    }

    private val fragmentList: List<Fragment>
        get() {
            val fragmentList: MutableList<Fragment> = ArrayList()
            fragmentList.add(CheckOutFragment1())
            fragmentList.add(CheckOutsFragment2())
            fragmentList.add(CheckOutsFragment3())
            return fragmentList
        }

    private fun configureNavigationDrawer() {
        drawerLayout = findViewById(R.id.checkoutdrawer_layout)
        val navView = findViewById<NavigationView>(R.id.checkoutnavigation)
        navView.setNavigationItemSelectedListener { menuItem: MenuItem ->
            val itemId = menuItem.itemId
            when (itemId) {
                R.id.menu_1 -> {
                    setFragment(0)
                }
                R.id.menu_2 -> {
                    setFragment(1)
                }
                R.id.menu_3 -> {
                    setFragment(2)
                }
            }
            false
        }
    }

    private fun setFragment(fragmentPosition: Int) {
        drawerLayout!!.closeDrawers()
        customViewPager!!.currentItem = fragmentPosition
    }

    fun swipeToCheckOutFragment3(fragmentPosition: Int) {
        setFragment(fragmentPosition)
    }

    override fun onBackPressed() {
        ExitDialogFragment {
            stopService(Intent(this, MyLogOutService::class.java))
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            null
        }.show(supportFragmentManager, null)
    }

    fun updateCartIcon(count: Int) {
        val toolbar = findViewById<Toolbar>(R.id.toolbarcheckout)
        val actionbarNotifcationTextview = toolbar.findViewById<TextView>(R.id.textView8)
        if (count == 0) {
            actionbarNotifcationTextview.background = getDrawable(R.drawable.before)
            actionbarNotifcationTextview.text = ""
        } else {
            actionbarNotifcationTextview.background = getDrawable(R.drawable.after)
            actionbarNotifcationTextview.text = count.toString()
        }
    }
}