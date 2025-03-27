package com.tt.weatherapp

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.search.SearchEngine
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

        initializeHERESDK()
        initializeSearchEngine()

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

    private fun initializeSearchEngine() {
        searchEngine = try {
            SearchEngine()
        } catch (e: InstantiationErrorException) {
            Log.e(TAG, "Initialization of SearchEngine failed: " + e.error.name)
            null
        }
    }

    private fun initializeHERESDK() {
        val accessKeyID = "Rfw2SKb_URpfhSeIXfpN0g"
        val accessKeySecret =
            "-WdjwtZKGAJei0zsbk_nvhFlJh6Yq_5qMUnbTZlOE8ZdF-QSsCpEWCWX5cS2pQC3fZrezna-Z1icbBQfIaSbdw"
        val options = SDKOptions(accessKeyID, accessKeySecret)
        try {
            SDKNativeEngine.makeSharedInstance(this, options)
        } catch (e: InstantiationErrorException) {
            Log.e(TAG, "Initialization of HERE SDK failed: " + e.error.name)
        }
    }

    companion object {
        private const val TAG = "App"

        // At the top level of your kotlin file:
        val Context.dataStore by preferencesDataStore(name = "settings")

        val appLifeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        private var mInstance: App? = null
        val instance get() = mInstance!!

        var searchEngine: SearchEngine? = null
            private set
    }
}