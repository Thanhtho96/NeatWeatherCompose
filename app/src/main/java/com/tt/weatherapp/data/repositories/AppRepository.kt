package com.tt.weatherapp.data.repositories

import android.app.AlarmManager
import android.content.Context
import android.location.Geocoder
import com.tt.weatherapp.data.local.SharedPrefHelper
import com.tt.weatherapp.data.local.WeatherDatabase
import com.tt.weatherapp.data.remotes.ApiService
import java.io.IOException
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class AppRepository(
    private val context: Context,
    private val apiService: ApiService,
    private val sharedPrefHelper: SharedPrefHelper,
    private val database: WeatherDatabase
) {
    // open weather
    suspend fun getWeatherOneCall(
        latitude: Double,
        longitude: Double,
        isForceRefresh: Boolean,
        language: String = ""
    ) {
        try {
            val cachedWeatherData = database.weatherDao().loadWeather()

            if (isForceRefresh.not() && cachedWeatherData != null) {
                val distance = 6371 *
                        acos(
                            cos(Math.toRadians(latitude)) *
                                    cos(Math.toRadians(cachedWeatherData.lat)) *
                                    cos(
                                        Math.toRadians(cachedWeatherData.lon) -
                                                Math.toRadians(longitude)
                                    ) +
                                    sin(Math.toRadians(latitude)) *
                                    sin(Math.toRadians(cachedWeatherData.lat))
                        )

                val isLessThanFifteenMinutes =
                    Date().time - cachedWeatherData.current.dt * 1000 <= AlarmManager.INTERVAL_FIFTEEN_MINUTES

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

            val location = if (builder.isNotEmpty()) {
                builder.substring(0, builder.length - 2)
            } else {
                ""
            }

            val weatherData =
                apiService.getWeatherByYourLocation(
                    latitude,
                    longitude,
                    language,
                    sharedPrefHelper.getChosenUnit().value
                ).apply {
                    unit = sharedPrefHelper.getChosenUnit()
                    this.location = location
                }
            database.weatherDao().insertWeather(weatherData)
        } catch (e: Exception) {
        }
    }
}
