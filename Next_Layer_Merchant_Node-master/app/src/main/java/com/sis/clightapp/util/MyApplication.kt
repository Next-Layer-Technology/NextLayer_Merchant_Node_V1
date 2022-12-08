package com.sis.clightapp.util

import android.app.Application
import com.sis.clightapp.util.MyApplication.ScreenReceiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.sis.clightapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class MyApplication : Application() {
    private var isScreenOff: Boolean = false
    override fun onCreate() {
        super.onCreate()
        ScreenReceiver()
        startKoin{
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule)
        }
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
}