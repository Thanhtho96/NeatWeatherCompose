package com.tt.weatherapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tt.weatherapp.model.WeatherData
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weatherdata")
    suspend fun loadWeather(): WeatherData?

    @Query("SELECT * FROM weatherdata")
    fun getWeather(): Flow<WeatherData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherData: WeatherData)
}