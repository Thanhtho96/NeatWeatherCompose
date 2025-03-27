package com.tt.weatherapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Main(
    @SerialName("feels_like")
    val feelsLike: Double = 0.0,
    @SerialName("grnd_level")
    val grndLevel: Int = 0,
    @SerialName("humidity")
    val humidity: Int = 0,
    @SerialName("pressure")
    val pressure: Int = 0,
    @SerialName("sea_level")
    val seaLevel: Int = 0,
    @SerialName("temp")
    val temp: Double = 0.0,
    @SerialName("temp_max")
    val tempMax: Double = 0.0,
    @SerialName("temp_min")
    val tempMin: Double = 0.0
)