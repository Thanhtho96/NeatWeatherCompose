package com.tt.weatherapp.data.remotes

import com.tt.weatherapp.BuildConfig
import com.tt.weatherapp.model.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitNetworkApi {
    companion object {
        private const val APP_ID = "appid"
        private const val LANG = "lang"
        private const val LAT = "lat"
        private const val LON = "lon"
        private const val UNITS = "units"
    }

    @GET("data/2.5/weather")
    suspend fun getWeatherByYourLocation(
        @Query(LAT) latitude: Double,
        @Query(LON) longitude: Double,
        @Query(LANG) language: String,
        @Query(UNITS) units: String?,
        @Query(APP_ID) appId: String = BuildConfig.OPEN_WEATHER_MAP_APP_ID,
    ): WeatherData
}