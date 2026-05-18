package com.prusoft.easybiglauncher

import android.app.Application
import com.prusoft.easybiglauncher.BuildConfig
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
