package com.tt.weatherapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Daily(
    val dew_point: Double,
    val dt: Long,
    val feels_like: FeelsLike,
    val rain: Double?,
    val snow: Double?,
    val sunrise: Long,
    val sunset: Long,
    val temp: Temp,
    val uvi: Double,
    val weather: List<Weather>,
    val wind_deg: Int,
    val wind_speed: Double
)