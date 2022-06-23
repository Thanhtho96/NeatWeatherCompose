package com.tt.weatherapp.data.repositories

import android.app.AlarmManager
import android.content.Context
import android.location.Geocoder
import com.tt.weatherapp.common.Resource
import com.tt.weatherapp.data.dao.WeatherDao
import com.tt.weatherapp.data.local.SharedPrefHelper
import com.tt.weatherapp.data.remotes.ApiService
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import java.io.IOException
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class AppRepository(
    private val context: Context,
    private val apiService: ApiService,
    private val sharedPrefHelper: SharedPrefHelper,
    private val weatherDao: WeatherDao
) {
    // open weather
    fun getWeatherOneCall(
        latitude: Double?,
        longitude: Double?,
        isChangeUnit: Boolean,
        language: String = ""
    ) = flow {
        val cachedWeatherData = weatherDao.loadWeather()

        if (isChangeUnit.not()) {
            if (latitude == null || longitude == null) {
                emit(Resource.Success(cachedWeatherData))
                return@flow
            }

            if (cachedWeatherData != null) {
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
                    emit(Resource.Success(cachedWeatherData))
                    return@flow
                }
            }
        }

        val cachedLat = cachedWeatherData?.lat
        val cachedLon = cachedWeatherData?.lon

        emit(Resource.Loading(true))

        val address = try {
            Geocoder(context, Locale.getDefault()).getFromLocation(
                if (isChangeUnit) cachedLat!! else latitude!!,
                if (isChangeUnit) cachedLon!! else longitude!!,
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
                if (isChangeUnit) cachedLat!! else latitude!!,
                if (isChangeUnit) cachedLon!! else longitude!!,
                language,
                sharedPrefHelper.getChosenUnit().value
            ).apply {
                unit = sharedPrefHelper.getChosenUnit()
                this.location = location
            }
        weatherDao.insertWeather(weatherData)
        emit(Resource.Success(weatherData))
    }.catch {
        emit(Resource.Success(null))
    }.onCompletion {
        emit(Resource.Loading(false))
    }
}
