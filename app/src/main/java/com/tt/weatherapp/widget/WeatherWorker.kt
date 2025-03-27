package com.tt.weatherapp.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.tt.weatherapp.data.local.WeatherDao
import com.tt.weatherapp.data.repositories.AppRepository
import com.tt.weatherapp.utils.LocationUtil
import com.tt.weatherapp.utils.PermissionUtil
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class WeatherWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), KoinComponent {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val weatherDao by inject<WeatherDao>()
    private val appRepository by inject<AppRepository>()

    companion object {
        private val uniqueWorkName = WeatherWorker::class.java.simpleName

        /**
         * Enqueues a new worker to refresh weather data only if not enqueued already
         *
         * Note: if you would like to have different workers per widget instance you could provide
         * the unique name based on some criteria (e.g selected weather location).
         */
        fun enqueue(context: Context) {
            val manager = WorkManager.getInstance(context)
            val requestBuilder = PeriodicWorkRequestBuilder<WeatherWorker>(
                15,
                TimeUnit.MINUTES
            )

            // Replace any enqueued work and expedite the request
            val workPolicy = ExistingPeriodicWorkPolicy.KEEP

            manager.enqueueUniquePeriodicWork(
                uniqueWorkName,
                workPolicy,
                requestBuilder.build()
            )
        }

        /**
         * Cancel any ongoing worker
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            updateLocation()
            appRepository.refreshWidgetLocation()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 10) {
                // Exponential backoff strategy will avoid the request to repeat
                // too fast in case of failures.
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun updateLocation() = suspendCancellableCoroutine { continuation ->
        val isLocationPermissionGranted =
            PermissionUtil.isLocationPermissionGranted(context)

        if (isLocationPermissionGranted) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnCompleteListener {
                // Failure
                it.exception?.let { exception ->
                    Log.e(uniqueWorkName, "lastLocation exception: $exception")
                    listenToLocation(continuation)
                    return@addOnCompleteListener
                }

                // Success
                runBlocking {
                    val location = it.result
                    if (location == null) {
                        listenToLocation(continuation)
                        return@runBlocking
                    }

                    val locationName =
                        LocationUtil.getLocationName(context, location.latitude, location.longitude)

                    weatherDao.updateGPSWidgetLocation(
                        location.latitude,
                        location.longitude,
                        locationName
                    )
                    continuation.resume(Unit)
                }
            }
            return@suspendCancellableCoroutine
        }

        continuation.resume(Unit)
    }

    private fun listenToLocation(continuation: CancellableContinuation<Unit>) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.filterNotNull().firstOrNull()?.let {
                    runBlocking {
                        weatherDao.updateGPSWidgetLocation(it.latitude, it.longitude, "")
                    }
                }
                continuation.resume(Unit)
            }
        }
        createLocationRequest()
        startLocationService()

        continuation.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationService() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun createLocationRequest() {
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000_000).build()
    }
}