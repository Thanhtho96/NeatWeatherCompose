package com.tt.weatherapp.data.repositories

import android.app.AlarmManager
import android.content.Context
import android.location.Geocoder
import androidx.annotation.StringRes
import com.tt.weatherapp.R
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.data.local.DataStoreHelper
import com.tt.weatherapp.data.local.WeatherDao
import com.tt.weatherapp.data.remotes.NetworkDataSource
import com.tt.weatherapp.model.Location
import com.tt.weatherapp.model.LocationSuggestion
import com.tt.weatherapp.model.LocationType
import com.tt.weatherapp.utils.convertSpeed
import com.tt.weatherapp.utils.convertTemperature
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class AppRepositoryImpl(
    private val context: Context,
    private val apiService: NetworkDataSource,
    private val dataStoreHelper: DataStoreHelper,
    private val weatherDao: WeatherDao
) : AppRepository {
    // open weather
    override suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        isForceRefresh: Boolean,
        language: String
    ) = flow {
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
                    return@flow
                }
            }

            emit(true)
            val chosenUnit = dataStoreHelper.getChosenUnit(context)

            val weatherData =
                apiService.getWeatherByCoordinate(
                    latitude,
                    longitude,
                    language,
                    chosenUnit.value
                ).apply {
                    unit = chosenUnit
                }

            when (cachedLocation?.type) {
                LocationType.GPS, null -> {
                    val address = try {
                        Geocoder(context, Locale.getDefault()).getFromLocation(
                            latitude,
                            longitude,
                            1
                        ) ?: emptyList()
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

                    weatherDao.updateGPSLocation(
                        latitude,
                        longitude,
                        locationName,
                        weatherData.current,
                        weatherData.daily,
                        weatherData.hourly,
                        weatherData.timezone,
                        weatherData.timezone_offset,
                        chosenUnit
                    )
                }
                LocationType.SEARCH -> {
                    val location = cachedLocation.copy(weatherData = weatherData)
                    weatherDao.insertLocation(location)
                }
            }
        } catch (e: Exception) {
            cachedLocation?.let {
                weatherDao.insertLocation(it)
            }
        } finally {
            emit(false)
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

        val chosenUnit = dataStoreHelper.getChosenUnit(context)

        try {
            val weatherData =
                apiService.getWeatherByCoordinate(
                    locationSuggestion.lat,
                    locationSuggestion.lon,
                    language,
                    chosenUnit.value
                ).apply {
                    unit = chosenUnit
                }

            weatherDao.getDraftLocation(locationSuggestion.lat, locationSuggestion.lon) ?: return

            weatherDao.insertLocation(draftLocation.copy(weatherData = weatherData))
        } catch (e: Exception) {
            weatherDao.insertLocation(draftLocation)
        }
    }

    override suspend fun toggleUnit(@StringRes unitId: Int) {
        val currentUnit = dataStoreHelper.getChosenUnit(context)

        val newUnit = when (unitId) {
            R.string.txt_metric -> Constant.Unit.METRIC
            R.string.txt_imperial -> Constant.Unit.IMPERIAL
            else -> return
        }

        if (currentUnit == newUnit) return

        dataStoreHelper.setChosenUnit(context, newUnit)

        val updatedWeathers = weatherDao.loadListLocation()
            .filter { it.weatherData != null }
            .map { location ->
                val weatherData = location.weatherData!!

                val current = weatherData.current.copy(
                    dew_point = weatherData.current.dew_point.convertTemperature(newUnit),
                    feels_like = weatherData.current.feels_like.convertTemperature(newUnit),
                    temp = weatherData.current.temp.convertTemperature(newUnit),
                    wind_speed = weatherData.current.wind_speed.convertSpeed(newUnit),
                )

                val daily = weatherData.daily.map {
                    it.copy(
                        dew_point = it.dew_point.convertTemperature(newUnit),
                        feels_like = it.feels_like.copy(
                            day = it.feels_like.day.convertTemperature(newUnit),
                            eve = it.feels_like.eve.convertTemperature(newUnit),
                            morn = it.feels_like.morn.convertTemperature(newUnit),
                            night = it.feels_like.night.convertTemperature(newUnit)
                        ),
                        temp = it.temp.copy(
                            day = it.temp.day.convertTemperature(newUnit),
                            eve = it.temp.eve.convertTemperature(newUnit),
                            morn = it.temp.morn.convertTemperature(newUnit),
                            night = it.temp.night.convertTemperature(newUnit),
                            max = it.temp.max.convertTemperature(newUnit),
                            min = it.temp.min.convertTemperature(newUnit)
                        ),
                        wind_speed = it.wind_speed.convertSpeed(newUnit)
                    )
                }

                val hourly = weatherData.hourly.map {
                    it.copy(
                        feels_like = it.feels_like.convertTemperature(newUnit),
                        temp = it.temp.convertTemperature(newUnit),
                        wind_speed = it.wind_speed.convertSpeed(newUnit)
                    )
                }

                val newWeatherData = weatherData.copy(
                    current = current,
                    daily = daily,
                    hourly = hourly,
                    unit = newUnit
                )

                location.copy(weatherData = newWeatherData)
            }

        weatherDao.insertLocation(updatedWeathers)
    }

    override suspend fun refreshLocation(language: String) {

    }
}
