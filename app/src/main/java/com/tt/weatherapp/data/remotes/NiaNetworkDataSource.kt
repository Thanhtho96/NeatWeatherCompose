package com.tt.weatherapp.data.remotes

import com.tt.weatherapp.model.WeatherData

interface NiaNetworkDataSource {
    suspend fun getWeatherByCoordinate(
        latitude: Double,
        longitude: Double,
        language: String,
        units: String?
    ): WeatherData
}