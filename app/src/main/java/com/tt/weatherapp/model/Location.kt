package com.tt.weatherapp.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import java.util.*

@Entity(primaryKeys = ["lat", "lon", "type"])
data class Location(
    val lat: Double,
    val lon: Double,
    val name: String,
    var isDisplay: Boolean,
    @Embedded val weatherData: WeatherData?,
    val type: LocationType = LocationType.GPS,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") val createAt: Long = Date().time
)

enum class LocationType {
    GPS,
    SEARCH
}
