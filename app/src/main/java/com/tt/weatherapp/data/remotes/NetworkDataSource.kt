package com.tt.weatherapp.data.remotes

import com.tt.weatherapp.model.WeatherData

interface NetworkDataSource {
    suspend fun getWeatherByCoordinate(
        latitude: Double,
        longitude: Double,
        language: String,
        units: String?
    ): WeatherData
}