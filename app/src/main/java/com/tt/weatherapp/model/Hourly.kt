package com.tt.weatherapp.model

import com.tt.weatherapp.common.Constant

data class Hourly(
    val clouds: Int,
    val dew_point: Double,
    val dt: Long,
    val feels_like: Double,
    val humidity: Int,
    val pressure: Int,
    val rain: Rain?,
    val snow: Snow?,
    val temp: Double,
    val weather: List<Weather>,
    val wind_deg: Int,
    val wind_speed: Double,
    val pop: Double?,
    var dtHeader: Long,
    var unit: Constant.Unit
)