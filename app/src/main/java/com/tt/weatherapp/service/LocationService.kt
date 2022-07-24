package com.tt.weatherapp.service

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.*
import com.tt.weatherapp.R
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.data.local.WeatherDao
import com.tt.weatherapp.data.repositories.AppRepository
import com.tt.weatherapp.model.LocationSuggestion
import com.tt.weatherapp.ui.MainActivity
import com.tt.weatherapp.utils.PermissionUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

@ExperimentalPermissionsApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
class LocationService : Service() {
    private val appRepository by inject<AppRepository>()
    private val scope by inject<CoroutineScope>()
    val weatherDao by inject<WeatherDao>()
    private val ioDispatcher by inject<CoroutineDispatcher>(named(Constant.Dispatcher.IO))
    var weatherState: WeatherState? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var isForceRefresh = true
    private var getWeatherJob: Job? = null

    private val binder = LocalBinder()

    // If the notification supports a direct reply action, use
    // PendingIntent.FLAG_MUTABLE instead.
    private val pendingIntent by lazy {
        Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    private val notification by lazy {
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.weather_location_title))
            .setContentText(getText(R.string.weather_location_message))
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = getString(R.string.weather_location_title)
            val descriptionText = getString(R.string.weather_location_message)
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initLocationCallBack()
        createLocationRequest()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return getWeatherData(intent.getBooleanExtra(Constant.IS_FORCE_REFRESH, true)).also {
            if (it == START_STICKY) startForeground(startId, notification)
        }
    }

    fun getWeatherData(isForceRefresh: Boolean): Int {
        val isLocationPermissionGranted = PermissionUtil.isLocationPermissionGranted(this)
        if (isLocationPermissionGranted.not()) return START_NOT_STICKY
        this.isForceRefresh = isForceRefresh
        fusedLocationClient.lastLocation.addOnCompleteListener {
            if (it.result != null) {
                getWeatherData(it.result.latitude, it.result.longitude)
            } else {
                startLocationService()
            }
            weatherState?.onSuccess()
        }
        return START_STICKY
    }

    fun chooseSuggestLocation(locationSuggestion: LocationSuggestion) {
        scope.launch(ioDispatcher) {
            appRepository.addSearchLocation(locationSuggestion, "")
            stopSelf()
        }
    }

    private fun getWeatherData(latitude: Double, longitude: Double) {
        if (getWeatherJob?.isActive == true) return
        getWeatherJob = scope.launch(ioDispatcher) {
            appRepository.getWeatherData(latitude, longitude, isForceRefresh, "")
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
        locationRequest = LocationRequest.create().apply {
            interval = 1000000
            fastestInterval = 500000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
    }

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
        private const val CHANNEL_ID = "WEATHER_LOCATION_CHANNEL"
    }
}

interface WeatherState {
    fun onSuccess()
}