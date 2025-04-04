package com.tt.weatherapp.data.repositories

import android.app.AlarmManager
import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.tt.weatherapp.R
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.data.local.DataStoreHelper
import com.tt.weatherapp.data.local.WeatherDao
import com.tt.weatherapp.data.remotes.NetworkDataSource
import com.tt.weatherapp.model.Location
import com.tt.weatherapp.model.LocationSuggestion
import com.tt.weatherapp.model.LocationType
import com.tt.weatherapp.model.WidgetLocation
import com.tt.weatherapp.utils.LocationUtil
import com.tt.weatherapp.utils.convertSpeed
import com.tt.weatherapp.utils.convertTemperature
import com.tt.weatherapp.widget.WeatherInfo
import com.tt.weatherapp.widget.WidgetUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class AppRepositoryImpl(
    private val context: Context,
    private val apiService: NetworkDataSource,
    private val dataStoreHelper: DataStoreHelper,
    private val weatherDao: WeatherDao,
    private val ioDispatcher: CoroutineDispatcher
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
                    Date().time - cachedLocation.weatherData.dt * 1000 <= AlarmManager.INTERVAL_FIFTEEN_MINUTES

                if (distance <= LIMIT_DISTANCE_KILOMETER && isLessThanFifteenMinutes) {
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

            Log.d(TAG, "getWeatherData: weatherData: $weatherData")

            when (cachedLocation?.type) {
                LocationType.GPS, null -> {
                    val locationName = LocationUtil.getLocationName(context, latitude, longitude)

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
                        lat = latitude,
                        lon = longitude,
                        clouds = weatherData.clouds,
                        cod = weatherData.cod,
                        coord = weatherData.coord,
                        dt = weatherData.dt,
                        id = weatherData.id,
                        main = weatherData.main,
                        name = weatherData.name,
                        rain = weatherData.rain,
                        snow = weatherData.snow,
                        sys = weatherData.sys,
                        timezone = weatherData.timezone,
                        visibility = weatherData.visibility,
                        weather = weatherData.weather,
                        wind = weatherData.wind,
                        unit = chosenUnit
                    )
                }

                LocationType.SEARCH -> {
                    val location = cachedLocation.copy(weatherData = weatherData)
                    weatherDao.insertLocation(location)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getWeatherData: ${e.cause} ${e.message}")
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

                val main = weatherData.main.copy(
                    feelsLike = weatherData.main.feelsLike.convertTemperature(newUnit),
                    temp = weatherData.main.temp.convertTemperature(newUnit),
                )

                val wind = weatherData.wind.copy(
                    speed = weatherData.wind.speed.convertSpeed(newUnit),
                )

                val newWeatherData = weatherData.copy(
                    main = main,
                    wind = wind,
                    unit = newUnit
                )

                location.copy(weatherData = newWeatherData)
            }

        weatherDao.insertLocation(updatedWeathers)

        val manager = GlanceAppWidgetManager(context)
        val listWidget = weatherDao.loadListWidget()

        val newListWidget =
            listWidget.filter { it.location.weatherData != null }
                .map { widget ->
                    val weatherData = widget.location.weatherData!!.let { weatherData ->
                        val main = weatherData.main.copy(
                            feelsLike = weatherData.main.feelsLike.convertTemperature(newUnit),
                            temp = weatherData.main.temp.convertTemperature(newUnit),
                        )

                        val wind = weatherData.wind.copy(
                            speed = weatherData.wind.speed.convertSpeed(newUnit),
                        )

                        weatherData.copy(
                            main = main,
                            wind = wind,
                            unit = newUnit
                        )
                    }

                    WidgetLocation(widget.widgetId, widget.location.copy(weatherData = weatherData))
                }

        weatherDao.insertWidgetLocation(newListWidget)

        WidgetUtil.setWidgetState(
            context,
            newListWidget.associate {
                val glanceId = try {
                    manager.getGlanceIdBy(it.widgetId)
                } catch (e: IllegalArgumentException) {
                    null
                }
                glanceId to WeatherInfo.Available(it.location)
            }
        )
    }

    override suspend fun refreshWidgetLocation() = withContext(ioDispatcher) {
        val chosenUnit = dataStoreHelper.getChosenUnit(context)
        val manager = GlanceAppWidgetManager(context)

        val list = weatherDao.loadListWidget()
        val newListWidget = list.map {
            async {
                val weatherData = apiService.getWeatherByCoordinate(
                    it.location.lat,
                    it.location.lon,
                    "",
                    chosenUnit.value
                ).apply {
                    unit = chosenUnit
                }

                WidgetLocation(it.widgetId, it.location.copy(weatherData = weatherData))
            }
        }.awaitAll()

        weatherDao.insertWidgetLocation(newListWidget)

        WidgetUtil.setWidgetState(
            context,
            newListWidget.associate {
                val glanceId = try {
                    manager.getGlanceIdBy(it.widgetId)
                } catch (e: IllegalArgumentException) {
                    null
                }
                glanceId to WeatherInfo.Available(it.location)
            }
        )
    }

    companion object {
        private const val TAG = "AppRepositoryImpl"
        private const val LIMIT_DISTANCE_KILOMETER = 5
    }
}
