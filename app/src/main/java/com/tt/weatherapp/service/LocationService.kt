package com.tt.weatherapp.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.StringRes
import com.google.android.gms.location.*
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.data.local.WeatherDao
import com.tt.weatherapp.data.repositories.AppRepository
import com.tt.weatherapp.model.LocationSuggestion
import com.tt.weatherapp.model.LocationType
import com.tt.weatherapp.utils.PermissionUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class LocationService : Service() {
    private val appRepository by inject<AppRepository>()
    private val scope by inject<CoroutineScope>()
    private val weatherDao by inject<WeatherDao>()
    private val ioDispatcher by inject<CoroutineDispatcher>(named(Constant.Dispatcher.IO))
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var isForceRefresh = true
    private var getWeatherJob: Job? = null
    private val binder = LocalBinder()

    var weatherState: WeatherState? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initLocationCallBack()
        createLocationRequest()
    }

    @SuppressLint("MissingPermission")
    fun getWeatherData(isForceRefresh: Boolean) {
        scope.launch {
            this@LocationService.isForceRefresh = isForceRefresh
            val cachedLocation = weatherDao.loadDisplayLocation()
            if (cachedLocation != null && cachedLocation.type == LocationType.SEARCH) {
                getWeatherData(cachedLocation.lat, cachedLocation.lon)
                return@launch
            }

            val isLocationPermissionGranted =
                PermissionUtil.isLocationPermissionGranted(this@LocationService)
            if (isLocationPermissionGranted.not()) return@launch
            fusedLocationClient.lastLocation.addOnCompleteListener {
                it.exception?.let { exception ->
                    Log.e(TAG, "lastLocation exception: $exception")
                }
                weatherState?.onComplete()
            }.addOnSuccessListener {
                getWeatherData(it.latitude, it.longitude)
            }.addOnFailureListener {
                startLocationService()
            }
        }

    }

    fun chooseSuggestLocation(locationSuggestion: LocationSuggestion) {
        scope.launch(ioDispatcher) {
            appRepository.addSearchLocation(locationSuggestion, "")
            stopSelf()
        }
    }

    fun toggleUnit(@StringRes unitId: Int) {
        scope.launch(ioDispatcher) {
            appRepository.toggleUnit(unitId)
        }
    }

    private fun getWeatherData(latitude: Double, longitude: Double) {
        if (getWeatherJob?.isActive == true) return
        getWeatherJob = scope.launch(ioDispatcher) {
            appRepository.getWeatherData(latitude, longitude, isForceRefresh, "").collect {
                weatherState?.onLoading(it)
            }
            stopSelf()
        }
    }

    private fun initLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach {
                    getWeatherData(it.latitude, it.longitude)
                    return@forEach
                }
            }
        }
    }

    private fun createLocationRequest() {
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000_000).build()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationService() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService() = this@LocationService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        weatherState = null
        return super.onUnbind(intent)
    }

    companion object {
        private const val TAG = "LocationService"
    }
}

interface WeatherState {
    fun onComplete()
    fun onLoading(isLoading: Boolean)
}