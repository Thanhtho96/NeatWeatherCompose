package com.tt.weatherapp

import android.app.Application
import com.tt.weatherapp.di.appModule
import com.tt.weatherapp.di.networkModule
import com.tt.weatherapp.di.viewModelModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        mInstance = this
        mAppLifeScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        startKoin {
            androidContext(this@App)
            modules(appModule, networkModule, viewModelModule)
        }
    }

    companion object {
        private var mAppLifeScope: CoroutineScope? = null
        val appLifeScope get() = mAppLifeScope!!

        private var mInstance: App? = null
        val instance get() = mInstance!!
    }
}