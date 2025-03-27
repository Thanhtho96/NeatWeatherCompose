package com.tt.weatherapp.model

import com.tt.weatherapp.common.Constant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    @SerialName("clouds")
    val clouds: Clouds = Clouds(),
    @SerialName("cod")
    val cod: Int = 0,
    @SerialName("coord")
    val coord: Coord = Coord(),
    @SerialName("dt")
    val dt: Long = 0L,
    @SerialName("id")
    val id: Int = 0,
    @SerialName("main")
    val main: Main = Main(),
    @SerialName("name")
    val name: String = "",
    @SerialName("rain")
    val rain: Rain? = Rain(),
    @SerialName("snow")
    val snow: Snow? = Snow(),
    @SerialName("sys")
    val sys: Sys = Sys(),
    @SerialName("timezone")
    val timezone: Int = 0,
    @SerialName("visibility")
    val visibility: Int = 0,
    @SerialName("weather")
    val weather: List<Weather> = emptyList(),
    @SerialName("wind")
    val wind: Wind = Wind(),
    var unit: Constant.Unit = Constant.Unit.METRIC
)