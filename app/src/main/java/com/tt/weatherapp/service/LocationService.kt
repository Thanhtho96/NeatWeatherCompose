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
import com.tt.weatherapp.data.local.WeatherDatabase
import com.tt.weatherapp.data.repositories.AppRepository
import com.tt.weatherapp.ui.MainActivity
import com.tt.weatherapp.utils.PermissionUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

@ExperimentalPermissionsApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
class LocationService : Service() {
    private val appRepository by inject<AppRepository>()
    private val scope by inject<CoroutineScope>()
    val database by inject<WeatherDatabase>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var isForceRefresh = true

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
        val isLocationPermissionGranted = PermissionUtil.isLocationPermissionGranted(this)
        if (isLocationPermissionGranted.not()) return START_NOT_STICKY

        isForceRefresh = intent.getBooleanExtra(Constant.IS_FORCE_REFRESH, true)
        startForeground(startId, notification)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location -> // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    getWeatherData(location.latitude, location.longitude)
                } else {
                    startLocationService()
                }
            }
        return START_STICKY
    }

    private fun getWeatherData(latitude: Double, longitude: Double) {
        scope.launch {
            appRepository.getWeatherOneCall(latitude, longitude, isForceRefresh)
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

    companion object {
        private const val CHANNEL_ID = "WEATHER_LOCATION_CHANNEL"
    }
}