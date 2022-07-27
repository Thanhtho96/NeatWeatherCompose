package com.tt.weatherapp.data.repositories

import com.tt.weatherapp.model.LocationSuggestion
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        isForceRefresh: Boolean,
        language: String
    ): Flow<Boolean>

    suspend fun addSearchLocation(locationSuggestion: LocationSuggestion, language: String)
}
