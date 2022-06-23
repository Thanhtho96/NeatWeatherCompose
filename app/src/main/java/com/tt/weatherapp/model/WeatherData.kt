package com.tt.weatherapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tt.weatherapp.common.Constant

@Entity
data class WeatherData(
    @PrimaryKey var id: Int = 1,
    val current: Current,
    val daily: List<Daily>,
    val hourly: List<Hourly>,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezone_offset: Int,
    var unit: Constant.Unit,
    var location: String
)