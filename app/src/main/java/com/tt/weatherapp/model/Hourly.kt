package com.tt.weatherapp.model

data class Hourly(
    val dt: Long,
    val feels_like: Double,
    val rain: Rain?,
    val snow: Snow?,
    val temp: Double,
    val weather: List<Weather>,
    val wind_deg: Int,
    val wind_speed: Double,
    val pop: Double?,
    var dtHeader: Long?,
)