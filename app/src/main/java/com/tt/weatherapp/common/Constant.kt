package com.tt.weatherapp.common

import androidx.annotation.Keep

object Constant {
    const val BASE_URL = "https://api.openweathermap.org/"

    fun getWeatherIcon(icon: String) = "http://openweathermap.org/img/wn/${icon}@2x.png"

    @Keep
    enum class Unit(val value: String) {
        METRIC("Metric"),
        IMPERIAL("Imperial");

        companion object {
            private val map = entries.associateBy(Unit::value)
            fun fromString(unit: String) = map[unit] ?: METRIC
        }
    }

    enum class Dispatcher {
        MAIN,
        DEFAULT,
        IO,
        UNCONFINED
    }
}