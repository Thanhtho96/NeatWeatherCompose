package com.tt.weatherapp.data.remotes

import com.tt.weatherapp.model.WeatherData

class RetrofitNetwork(private val networkApi: RetrofitNetworkApi) : NetworkDataSource {
    override suspend fun getWeatherByCoordinate(
        latitude: Double,
        longitude: Double,
        language: String,
        units: String?
    ): WeatherData = networkApi.getWeatherByYourLocation(latitude, longitude, language, units)
}