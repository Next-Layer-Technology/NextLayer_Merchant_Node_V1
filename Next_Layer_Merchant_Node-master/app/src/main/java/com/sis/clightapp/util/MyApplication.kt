package com.sis.clightapp.util

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.sis.clightapp.di.appModule
import com.sis.clightapp.session.MyLogOutService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import java.util.*

class MyApplication : Application() {
    private var isScreenOff: Boolean = false
    override fun onCreate() {
        super.onCreate()
        ScreenReceiver()
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule)
        }
        val observer = AppLifecycleObserver()
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
    }

    private inner class ScreenReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                isScreenOff = true
            } else if (intent.action == Intent.ACTION_SCREEN_ON) {
                isScreenOff = false
            }
        }

        init {
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            registerReceiver(this, filter)
        }
    }

    internal inner class AppLifecycleObserver : DefaultLifecycleObserver {
        var timer = Timer()
        override fun onStart(owner: LifecycleOwner) { // app moved to foreground
            timer.cancel()
            timer = Timer()
            Log.d(this.javaClass.simpleName, "timer cancelled")
        }

        override fun onStop(owner: LifecycleOwner) { // app moved to background
            Log.d(this.javaClass.simpleName, "app moved to background")
            Log.d(this.javaClass.simpleName, "timer started")
            timer.schedule(object : TimerTask() {
                override fun run() {
                    stopService(Intent(this@MyApplication, MyLogOutService::class.java))
                    onTerminate()
                }
            }, 600000)
        }
    }
}