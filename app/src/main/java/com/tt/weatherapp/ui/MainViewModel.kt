package com.tt.weatherapp.ui

import android.app.AlarmManager
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LanguageCode
import com.here.sdk.search.SearchEngine
import com.here.sdk.search.SearchOptions
import com.here.sdk.search.TextQuery
import com.tt.weatherapp.R
import com.tt.weatherapp.common.BaseViewModel
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.data.local.DataStoreHelper
import com.tt.weatherapp.data.local.WeatherDao
import com.tt.weatherapp.model.CurrentWeatherGridInfo
import com.tt.weatherapp.model.HomeWeatherUnit
import com.tt.weatherapp.model.Location
import com.tt.weatherapp.model.LocationSuggestion
import com.tt.weatherapp.model.WeatherRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class MainViewModel(
    private val searchEngine: SearchEngine?,
    private val weatherDao: WeatherDao,
    private val dataStoreHelper: DataStoreHelper,
    private val ioDispatcher: CoroutineDispatcher,
    private val mainDispatcher: CoroutineDispatcher
) : BaseViewModel() {

    var locationData by mutableStateOf<Location?>(null)
        private set

    var listCurrentWeatherData by mutableStateOf<List<CurrentWeatherGridInfo>>(emptyList())
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var listLocation by mutableStateOf<List<Location>>(emptyList())
        private set

    var selectUnit by mutableStateOf(Constant.Unit.METRIC)
        private set

    private val mIsForceRefresh = MutableStateFlow(WeatherRequest())
    val isForceRefresh = mIsForceRefresh.asStateFlow()

    private var refreshJob: Job? = null
    private var searchPlaceJob: Job? = null

    var listSuggestion by mutableStateOf<List<LocationSuggestion>>(emptyList())
        private set

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val searchOptions by lazy {
        SearchOptions().apply {
            languageCode = LanguageCode.EN_US
            maxItems = 30
        }
    }

    fun setRefresh(isRefreshing: Boolean) {
        this.isRefreshing = isRefreshing
    }

    init {
        getWeatherPeriodically()
        getWeatherInfo()
        listenUnit()
    }

    private fun getWeatherPeriodically() {
        viewModelScope.launch {
            isForceRefresh.filter { it.isRefresh.not() }.collect {
                refreshJob?.cancelAndJoin()
                refreshJob = launch {
                    delay(AlarmManager.INTERVAL_FIFTEEN_MINUTES)
                    setIsForceRefresh(isRefresh = true, isForce = true)
                }
            }
        }
    }

    private fun listenUnit() {
        viewModelScope.launch {
            dataStoreHelper.listenChosenUnit(mApplication)
                .stateIn(viewModelScope)
                .collect {
                    delay(300)
                    selectUnit = it
                }
        }
    }

    fun setIsForceRefresh(isRefresh: Boolean, isForce: Boolean) {
        mIsForceRefresh.value = mIsForceRefresh.value.copy(isRefresh = isRefresh, isForce = isForce)
    }

    fun searchPlaceWithKeyword(keyword: String) {
        if (keyword.isBlank()) {
            listSuggestion = listOf()
            return
        }

        viewModelScope.launch(ioDispatcher) {
            searchPlaceJob?.cancelAndJoin()
            searchPlaceJob = launch {
                try {
                    listSuggestion = searchPlace(keyword)
                } catch (e: RuntimeException) {
                    listSuggestion = listOf()
                    withContext(mainDispatcher) {
                        Toast.makeText(mApplication, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun searchPlace(keyword: String) = suspendCancellableCoroutine { continuation ->
        if (searchEngine == null) {
            continuation.resumeWith(
                Result.failure(
                    RuntimeException(mApplication.getString(R.string.search_engine_fail))
                )
            )
            return@suspendCancellableCoroutine
        }
        searchEngine.search(
            TextQuery(keyword, TextQuery.Area(GeoCoordinates(latitude, longitude))),
            searchOptions,
        ) { searchError, list ->
            if (searchError != null || list == null) {
                continuation.resumeWith(Result.success(listOf()))
                return@search
            }

            val listResult =
                list.asSequence().filter { it != null && it.geoCoordinates != null }
                    .filter { it.address.city.isNotBlank() && it.address.country.isNotBlank() }
                    .distinctBy { "${it.address.city}, ${it.address.country}" }
                    .filter { location -> "${location.address.city}, ${location.address.country}" !in listLocation.map { it.searchName } }
                    .map {
                        LocationSuggestion(
                            it.address.city,
                            it.address.country,
                            it.geoCoordinates!!.latitude,
                            it.geoCoordinates!!.longitude
                        )
                    }.toList()

            continuation.resumeWith(Result.success(listResult))
        }
    }

    private fun getWeatherInfo() {
        viewModelScope.launch(ioDispatcher) {
            weatherDao.getDisplayLocation()
                .shareIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000)
                )
                .collect { location ->
                    val data = location?.weatherData ?: return@collect
                    locationData = location

                    val current = data.main
                    val homeWeatherUnit = HomeWeatherUnit(data)

                    listCurrentWeatherData = arrayListOf(
                        CurrentWeatherGridInfo(
                            R.drawable.ic_feel_like_white,
                            R.string.txt_feel_like,
                            homeWeatherUnit.feelLike,
                            current.feelsLike.roundToInt().toString()
                        ),
                        CurrentWeatherGridInfo(
                            R.drawable.ic_pressure_white,
                            R.string.txt_pressure,
                            R.string.txt_hpa_pressure,
                            current.pressure.toString()
                        ),
                        CurrentWeatherGridInfo(
                            R.drawable.ic_humidity_white,
                            R.string.txt_humidity,
                            R.string.txt_percentage_humidity,
                            current.humidity.toString()
                        ),
                        CurrentWeatherGridInfo(
                            R.drawable.ic_visibility_white,
                            R.string.txt_visibility,
                            if (data.visibility / 1000 > 0)
                                R.string.txt_kilometer_visibility
                            else
                                R.string.txt_meter_visibility,
                            if (data.visibility / 1000 > 0)
                                String.format("%d", data.visibility / 1000)
                            else
                                data.visibility.toString()
                        ),
                    )
                }
        }

        viewModelScope.launch(ioDispatcher) {
            weatherDao.getGPSLocation()
                .shareIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000)
                )
                .collect {
                    if (it == null) return@collect
                    latitude = it.lat
                    longitude = it.lon
                }
        }

        viewModelScope.launch(ioDispatcher) {
            weatherDao.getListLocation()
                .shareIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000)
                )
                .collect {
                    listLocation = it
                }
        }
    }

    fun deleteLocation(location: Location) {
        viewModelScope.launch(ioDispatcher) {
            weatherDao.deleteLocation(location)
            if (location.isDisplay) {
                weatherDao.setGPSLocationToDisplay()
            }
        }
    }

    suspend fun changeDisplayLocation(location: Location) {
        val updatedList = weatherDao.loadListLocation().onEach {
            it.isDisplay = it == location
        }

        weatherDao.insertLocation(updatedList)
    }
}
