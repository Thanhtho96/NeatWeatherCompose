package com.tt.weatherapp.model

import com.tt.weatherapp.common.Constant
import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val current: Current,
    val daily: List<Daily>,
    val hourly: List<Hourly>,
    val timezone: String,
    val timezone_offset: Int,
    var unit: Constant.Unit,
)