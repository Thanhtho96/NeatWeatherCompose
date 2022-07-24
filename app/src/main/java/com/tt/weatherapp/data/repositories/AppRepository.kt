package com.tt.weatherapp.data.repositories

import com.tt.weatherapp.model.LocationSuggestion

interface AppRepository {
    suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        isForceRefresh: Boolean,
        language: String
    )

    suspend fun addSearchLocation(locationSuggestion: LocationSuggestion, language: String)
}
