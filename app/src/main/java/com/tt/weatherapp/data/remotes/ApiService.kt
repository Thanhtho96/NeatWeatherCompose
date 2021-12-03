package com.tt.weatherapp.data.remotes

import com.tt.weatherapp.BuildConfig
import com.tt.weatherapp.model.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    companion object {
        private const val APP_ID = "appid"
        private const val EXCLUDE = "exclude"
        private const val LANG = "lang"
        private const val LAT = "lat"
        private const val LON = "lon"
        private const val MINUTELY = "minutely"
        private const val UNITS = "units"
    }

    @GET("onecall")
    suspend fun getWeatherByYourLocation(
        @Query(LAT) latitude: Double,
        @Query(LON) longitude: Double,
        @Query(LANG) language: String,
        @Query(UNITS) units: String?,
        @Query(APP_ID) appId: String = BuildConfig.OPEN_WEATHER_MAP_APP_ID,
        @Query(EXCLUDE) exclude: String = MINUTELY
    ): WeatherData
}