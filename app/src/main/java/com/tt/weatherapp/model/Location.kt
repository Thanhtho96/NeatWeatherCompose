package com.tt.weatherapp.model

import androidx.room.Embedded
import androidx.room.Entity

@Entity(primaryKeys = ["lat", "lon", "type"])
data class Location(
    val lat: Double,
    val lon: Double,
    val name: String,
    val isDisplay: Boolean,
    @Embedded val weatherData: WeatherData?,
    val type: LocationType = LocationType.GPS
)

enum class LocationType {
    GPS,
    SEARCH
}
