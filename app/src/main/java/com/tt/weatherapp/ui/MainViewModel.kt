package com.tt.weatherapp.ui

import androidx.lifecycle.viewModelScope
import com.tt.weatherapp.common.BaseViewModel
import com.tt.weatherapp.data.repositories.AppRepository
import com.tt.weatherapp.model.Daily
import com.tt.weatherapp.model.HourlyCustom
import com.tt.weatherapp.model.WeatherData
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel(private val appRepository: AppRepository) : BaseViewModel() {

    lateinit var weatherData: StateFlow<WeatherData?>
        private set

    lateinit var hourlyCustom: StateFlow<List<HourlyCustom>>
        private set

    lateinit var dailyCustom: StateFlow<List<Daily>>
        private set

    init {
        getWeatherInfo()
    }

    private fun getWeatherInfo() {
        weatherData =
            appRepository.getWeatherOneCall(1.0, 1.0, "")
                .onStart { showLoading(true) }
                .onCompletion { showLoading(false) }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    null
                )

        hourlyCustom = weatherData
            .filterNotNull()
            .transform {
                var dayHeader = 0
                val listHourlyCustom = ArrayList<HourlyCustom>()

                for (hourly in it.hourly) {
                    var hourlyCustom: HourlyCustom
                    val currentDay =
                        SimpleDateFormat("dd", Locale.getDefault()).format(hourly.dt * 1000).toInt()

                    when {
                        dayHeader == 0 -> {
                            dayHeader = currentDay
                            hourlyCustom = HourlyCustom(hourly, true)
                        }
                        currentDay != dayHeader -> {
                            dayHeader = currentDay
                            hourlyCustom = HourlyCustom(hourly, true)
                        }
                        else -> {
                            hourlyCustom = HourlyCustom(hourly, false)
                        }
                    }
                    listHourlyCustom.add(hourlyCustom)
                }

                emit(listHourlyCustom)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
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