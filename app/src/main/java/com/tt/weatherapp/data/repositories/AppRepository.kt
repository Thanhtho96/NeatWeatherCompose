package com.tt.weatherapp.data.repositories

import com.tt.weatherapp.data.remotes.ApiService
import com.tt.weatherapp.model.WeatherData
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class AppRepository(private val apiService: ApiService) {
    // open weather
    fun getWeatherOneCall(
        latitude: Double,
        longitude: Double,
        language: String
    ) = flow<WeatherData?> {
        val weatherData = apiService.getWeatherByYourLocation(latitude, longitude, language)
        emit(weatherData)
    }.catch {
        emit(null)
    }
}
