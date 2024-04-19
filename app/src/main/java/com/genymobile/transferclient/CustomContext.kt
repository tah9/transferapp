package com.genymobile.transferclient

import android.app.Application
import android.util.Log
import com.tencent.mmkv.MMKV

class CustomContext : Application(), Thread.UncaughtExceptionHandler {
    private val TAG = "CustomContext"

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(this)
        MMKV.initialize(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.d(TAG, "CustomContext uncaughtException: thread=${t},error=${e}")
    }
}