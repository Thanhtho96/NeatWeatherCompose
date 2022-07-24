package com.tt.weatherapp.data.local

import androidx.room.*
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.model.Current
import com.tt.weatherapp.model.Daily
import com.tt.weatherapp.model.Hourly
import com.tt.weatherapp.model.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM location WHERE type = 'GPS'")
    fun getGPSLocation(): Flow<Location?>

    @Query("SELECT * FROM location WHERE isDisplay = 1")
    suspend fun loadDisplayLocation(): Location?

    @Query("SELECT * FROM location WHERE isDisplay = 1")
    fun getDisplayLocation(): Flow<Location?>

    @Query("SELECT * FROM location ORDER BY type")
    fun getListLocation(): Flow<List<Location>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location)

    @Query("UPDATE location SET lat = :lat, lon = :lon, name = :name, current = :current, daily = :daily, hourly = :hourly, timezone = :timezone, timezone_offset = :timezone_offset, unit = :unit WHERE type = 'GPS'")
    suspend fun updateGPSLocation(
        lat: Double,
        lon: Double,
        name: String,
        current: Current,
        daily: List<Daily>,
        hourly: List<Hourly>,
        timezone: String,
        timezone_offset: Int,
        unit: Constant.Unit
    )

    @Query("SELECT * FROM location WHERE lat = :lat AND lon = :lon AND type = 'SEARCH'")
    suspend fun getDraftLocation(lat: Double, lon: Double): Location?

    @Delete
    suspend fun deleteLocation(location: Location)
}