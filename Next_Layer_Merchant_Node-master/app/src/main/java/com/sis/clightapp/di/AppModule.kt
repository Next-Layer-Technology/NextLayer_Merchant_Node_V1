package com.sis.clightapp.di

import com.sis.clightapp.services.BTCService
import com.sis.clightapp.services.FCMService
import com.sis.clightapp.services.LightningService
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { FCMService() }
    single(createdAtStart = true) { BTCService() }
    single { LightningService(androidApplication()) }
}