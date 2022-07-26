package com.tt.weatherapp.data.repositories

import android.app.AlarmManager
import android.content.Context
import android.location.Geocoder
import com.tt.weatherapp.data.local.SharedPrefHelper
import com.tt.weatherapp.data.local.WeatherDao
import com.tt.weatherapp.data.remotes.NiaNetworkDataSource
import com.tt.weatherapp.model.Location
import com.tt.weatherapp.model.LocationSuggestion
import com.tt.weatherapp.model.LocationType
import java.io.IOException
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class AppRepositoryImpl(
    private val context: Context,
    private val apiService: NiaNetworkDataSource,
    private val sharedPrefHelper: SharedPrefHelper,
    private val weatherDao: WeatherDao
) : AppRepository {
    // open weather
    override suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        isForceRefresh: Boolean,
        language: String
    ) {
        val cachedLocation = weatherDao.loadDisplayLocation()

        try {
            if (isForceRefresh.not() && cachedLocation != null && cachedLocation.weatherData != null) {
                val distance = 6371 *
                        acos(
                            cos(Math.toRadians(latitude)) *
                                    cos(Math.toRadians(cachedLocation.lat)) *
                                    cos(
                                        Math.toRadians(cachedLocation.lon) -
                                                Math.toRadians(longitude)
                                    ) +
                                    sin(Math.toRadians(latitude)) *
                                    sin(Math.toRadians(cachedLocation.lat))
                        )

                val isLessThanFifteenMinutes =
                    Date().time - cachedLocation.weatherData.current.dt * 1000 <= AlarmManager.INTERVAL_FIFTEEN_MINUTES

                if (distance <= 5 && isLessThanFifteenMinutes) {
                    return
                }
            }

            val address = try {
                Geocoder(context, Locale.getDefault()).getFromLocation(
                    latitude,
                    longitude,
                    1
                )
            } catch (e: IOException) {
                emptyList()
            }

            val builder = StringBuilder()
            val subAdminArea = address.firstOrNull()?.subAdminArea
            val adminArea = address.firstOrNull()?.adminArea
            subAdminArea?.let {
                builder.append(it)
                builder.append(", ")
            }
            adminArea?.let {
                builder.append(it)
                builder.append(", ")
            }

            val locationName = if (builder.isNotEmpty()) {
                builder.substring(0, builder.length - 2)
            } else {
                ""
            }

            if (cachedLocation == null) {
                val draftLocation = Location(
                    latitude,
                    longitude,
                    locationName,
                    true,
                    null
                )
                weatherDao.insertLocation(draftLocation)
            }

            val weatherData =
                apiService.getWeatherByCoordinate(
                    latitude,
                    longitude,
                    language,
                    sharedPrefHelper.getChosenUnit().value
                ).apply {
                    unit = sharedPrefHelper.getChosenUnit()
                }

            when (cachedLocation?.type ?: LocationType.GPS) {
                LocationType.GPS -> {
                    weatherDao.updateGPSLocation(
                        latitude,
                        longitude,
                        locationName,
                        weatherData.current,
                        weatherData.daily,
                        weatherData.hourly,
                        weatherData.timezone,
                        weatherData.timezone_offset,
                        sharedPrefHelper.getChosenUnit()
                    )
                }
                LocationType.SEARCH -> {
                    val location = cachedLocation!!.copy(
                        lat = latitude,
                        lon = longitude,
                        weatherData = weatherData
                    )

                    weatherDao.insertLocation(location)
                }
            }
        } catch (e: Exception) {
            cachedLocation?.let {
                weatherDao.insertLocation(it)
            }
        }
    }

    override suspend fun addSearchLocation(
        locationSuggestion: LocationSuggestion,
        language: String
    ) {
        val draftLocation = Location(
            locationSuggestion.lat,
            locationSuggestion.lon,
            "${locationSuggestion.title}, ${locationSuggestion.detail}",
            false,
            null,
            LocationType.SEARCH
        )
        weatherDao.insertLocation(draftLocation)

        try {
            val weatherData =
                apiService.getWeatherByCoordinate(
                    locationSuggestion.lat,
                    locationSuggestion.lon,
                    language,
                    sharedPrefHelper.getChosenUnit().value
                ).apply {
                    unit = sharedPrefHelper.getChosenUnit()
                }

            weatherDao.getDraftLocation(locationSuggestion.lat, locationSuggestion.lon) ?: return

            weatherDao.insertLocation(draftLocation.copy(weatherData = weatherData))
        } catch (e: Exception) {
            weatherDao.insertLocation(draftLocation)
        }
    }
}
