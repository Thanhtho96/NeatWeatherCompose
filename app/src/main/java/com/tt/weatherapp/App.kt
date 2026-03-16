package com.tt.weatherapp

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.tt.weatherapp.di.appModule
import com.tt.weatherapp.di.dispatcherModule
import com.tt.weatherapp.di.networkModule
import com.tt.weatherapp.di.viewModelModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        mInstance = this

        startKoin {
            androidContext(this@App)
            modules(appModule, networkModule, viewModelModule, dispatcherModule)
        }
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }

    companion object {
        // At the top level of your kotlin file:
        val Context.dataStore by preferencesDataStore(name = "settings")

        val appLifeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        private var mInstance: App? = null
        val instance get() = mInstance!!
    }
}