package com.tt.weatherapp.ui

import androidx.lifecycle.viewModelScope
import com.tt.weatherapp.common.BaseViewModel
import com.tt.weatherapp.data.repositories.AppRepository
import com.tt.weatherapp.model.Daily
import com.tt.weatherapp.model.Hourly
import com.tt.weatherapp.model.WeatherData
import com.tt.weatherapp.utils.DateUtil
import kotlinx.coroutines.flow.*

class MainViewModel(private val appRepository: AppRepository) : BaseViewModel() {

    lateinit var weatherData: StateFlow<WeatherData?>
        private set

    lateinit var hourlyCustom: StateFlow<Map<Long, List<Hourly>>>
        private set

    lateinit var dailyCustom: StateFlow<List<Daily>>
        private set

    init {
        getWeatherInfo()
    }

    private fun getWeatherInfo() {
        weatherData =
            appRepository.getWeatherOneCall(21.027763, 105.834160, "")
                .onStart { showLoading(true) }
                .onCompletion { showLoading(false) }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    null
                )

        hourlyCustom = weatherData
            .filterNotNull()
            .transform { weatherData ->
                var dayHeader = ""
                var currentDt = 0L
                val listHourly = ArrayList<Hourly>()

                weatherData.hourly.forEach {
                    val currentDay = DateUtil.format(DateUtil.DateFormat.DAY, it.dt * 1000)

                    if (dayHeader == "" || currentDay != dayHeader) {
                        dayHeader = currentDay
                        currentDt = it.dt * 1000
                    }

                    listHourly.add(it.apply {
                        dtHeader = currentDt
                        unit = weatherData.unit
                    })
                }

                emit(listHourly.groupBy { it.dtHeader })
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap()
            )

        dailyCustom = weatherData
            .filterNotNull()
            .transform {
                emit(it.daily)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
    }
}