package com.example.myapplication

import android.app.Application
import android.util.Log

import com.hypertrack.hyperlog.HyperLog

class OppiaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        HyperLog.initialize(this)

        HyperLog.setURL("<Set URL>")
    }
}
