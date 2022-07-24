package com.tt.weatherapp.data.remotes

import com.tt.weatherapp.model.WeatherData

class RetrofitNiaNetwork(private val networkApi: RetrofitNiaNetworkApi) : NiaNetworkDataSource {
    override suspend fun getWeatherByCoordinate(
        latitude: Double,
        longitude: Double,
        language: String,
        units: String?
    ): WeatherData = networkApi.getWeatherByYourLocation(latitude, longitude, language, units)
}