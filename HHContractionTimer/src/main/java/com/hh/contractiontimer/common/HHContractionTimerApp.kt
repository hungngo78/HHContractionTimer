package com.hh.contractiontimer.common

import android.app.Application
import android.content.Context

class HHContractionTimerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        myApplicationContext = applicationContext
    }

    companion object {
        lateinit var myApplicationContext : Context

    }
}