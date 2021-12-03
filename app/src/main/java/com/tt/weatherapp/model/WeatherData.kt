package com.tt.weatherapp.model

import com.tt.weatherapp.common.Constant

data class WeatherData(
    val current: Current,
    val daily: MutableList<Daily>,
    val hourly: MutableList<Hourly>,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezone_offset: Int,
    var unit: Constant.Unit
)