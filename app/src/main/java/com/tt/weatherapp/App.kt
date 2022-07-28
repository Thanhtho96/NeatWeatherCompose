package com.tt.weatherapp

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.search.SearchEngine
import com.tt.weatherapp.di.appModule
import com.tt.weatherapp.di.dispatcherModule
import com.tt.weatherapp.di.networkModule
import com.tt.weatherapp.di.viewModelModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        mInstance = this
        mAppLifeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        appLifeScope.launch(Dispatchers.IO) {
            searchEngine = try {
                SearchEngine()
            } catch (e: InstantiationErrorException) {
                null
            }
        }

        startKoin {
            androidContext(this@App)
            modules(appModule, networkModule, viewModelModule, dispatcherModule)
        }
    }

    companion object {
        // At the top level of your kotlin file:
        val Context.dataStore by preferencesDataStore(name = "settings")

        private var mAppLifeScope: CoroutineScope? = null
        val appLifeScope get() = mAppLifeScope!!

        private var mInstance: App? = null
        val instance get() = mInstance!!

        var searchEngine: SearchEngine? = null
            private set
    }
}