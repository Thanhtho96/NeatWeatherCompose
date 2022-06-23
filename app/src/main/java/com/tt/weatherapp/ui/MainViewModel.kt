package com.tt.weatherapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.tt.weatherapp.R
import com.tt.weatherapp.common.BaseViewModel
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.common.Resource
import com.tt.weatherapp.data.repositories.AppRepository
import com.tt.weatherapp.model.CurrentWeatherGridInfo
import com.tt.weatherapp.model.DailyTempInfo
import com.tt.weatherapp.model.Hourly
import com.tt.weatherapp.model.WeatherData
import com.tt.weatherapp.utils.DateUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainViewModel(private val appRepository: AppRepository) : BaseViewModel() {

    var weatherData by mutableStateOf<WeatherData?>(null)
        private set

    var hourly by mutableStateOf<List<Hourly>>(emptyList())
        private set

    var listCurrentWeatherData by mutableStateOf<List<CurrentWeatherGridInfo>>(emptyList())
        private set

    var listDailyTempInfo by mutableStateOf<List<DailyTempInfo>>(emptyList())
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        getWeatherInfo()
    }

    fun refresh() {
        isRefreshing = true
    }

    fun getWeatherInfo(
        latitude: Double? = null,
        longitude: Double? = null,
        isChangeUnit: Boolean = false
    ) {
        viewModelScope.launch {
            appRepository.getWeatherOneCall(
                latitude,
                longitude,
                isChangeUnit,
                isRefreshing
            )
                .onCompletion { isRefreshing = false }
                .shareIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                ).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> refresh()
                        is Resource.Success -> {
                            weatherData = resource.value
                            val data = resource.value ?: return@collect

                            val current = data.current
                            val daily = data.daily.filterIndexed { index, _ -> index != 0 }

                            listCurrentWeatherData = arrayListOf(
                                CurrentWeatherGridInfo(
                                    R.drawable.ic_feel_like_white,
                                    R.string.txt_feel_like,
                                    when (data.unit) {
                                        Constant.Unit.METRIC -> R.string.txt_celsius_temp
                                        Constant.Unit.IMPERIAL -> R.string.txt_fahrenheit_temp
                                    },
                                    current.feels_like.roundToInt().toString()
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
                                    R.drawable.ic_dew_point_white,
                                    R.string.txt_dew_point,
                                    when (data.unit) {
                                        Constant.Unit.METRIC -> R.string.txt_celsius_dew_point
                                        Constant.Unit.IMPERIAL -> R.string.txt_fahrenheit_dew_point
                                    },
                                    current.dew_point.roundToInt().toString()
                                ),
                                CurrentWeatherGridInfo(
                                    R.drawable.ic_uv_index_white,
                                    R.string.txt_uv_index,
                                    null,
                                    current.uvi.roundToInt().toString()
                                ),
                                CurrentWeatherGridInfo(
                                    R.drawable.ic_visibility_white,
                                    R.string.txt_visibility,
                                    if (current.visibility / 1000 > 0)
                                        R.string.txt_kilometer_visibility
                                    else
                                        R.string.txt_meter_visibility,
                                    if (current.visibility / 1000 > 0)
                                        String.format("%d", current.visibility / 1000)
                                    else
                                        current.visibility.toString()
                                ),
                            )

                            val minTemp = daily.minOf { it.temp.min }.roundToInt()
                            val maxTemp = daily.maxOf { it.temp.max }.roundToInt()
                            val eachPortion = maxOf(17.dp.value, (maxTemp - minTemp) / 5F)

                            listDailyTempInfo = daily.map {
                                val mMaxTemp = it.temp.max.roundToInt()
                                val mMinTemp = it.temp.min.roundToInt()

                                val height = (mMaxTemp - mMinTemp) * eachPortion
                                val top = (maxTemp - mMaxTemp) * eachPortion

                                DailyTempInfo(
                                    mApplication.getString(
                                        R.string.txt_temp_without_unit,
                                        it.temp.max.roundToInt().toString()
                                    ),
                                    mApplication.getString(
                                        R.string.txt_temp_without_unit,
                                        it.temp.min.roundToInt().toString()
                                    ),
                                    Constant.getWeatherIcon(it.weather[0].icon),
                                    DateUtil.format(DateUtil.DateFormat.DAY_OF_WEEK, it.dt * 1000),
                                    top,
                                    height
                                )
                            }

                            var dayHeader = ""
                            var currentDt = 0L
                            val listHourly = ArrayList<Hourly>()

                            data.hourly.forEach {
                                val currentDay =
                                    DateUtil.format(DateUtil.DateFormat.DAY, it.dt * 1000)

                                if (dayHeader == "" || currentDay != dayHeader) {
                                    dayHeader = currentDay
                                    currentDt = it.dt * 1000
                                }

                                listHourly.add(it.apply {
                                    dtHeader = currentDt
                                    unit = data.unit
                                })
                            }
                            hourly = listHourly
                        }
                    }
                }
        }
    }
}