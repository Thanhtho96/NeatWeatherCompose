package com.tt.weatherapp.common

object Constant {
    const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val ENGLISH = "en"
    const val VIETNAMESE = "vi"

    fun getWeatherIcon(icon: String) = "https://openweathermap.org/img/wn/${icon}@2x.png"

    enum class Unit(val value: String) {
        METRIC("Metric"),
        IMPERIAL("Imperial");

        companion object {
            private val map = values().associateBy(Unit::value)
            fun fromString(unit: String) = map[unit] ?: METRIC
        }
    }
}