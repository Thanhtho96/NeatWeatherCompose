package com.tt.weatherapp.data.local

import androidx.room.*
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM location WHERE type = 'GPS'")
    fun getGPSLocation(): Flow<Location?>

    @Query("SELECT * FROM location WHERE isDisplay = 1")
    suspend fun loadDisplayLocation(): Location?

    @Query("SELECT * FROM location WHERE isDisplay = 1")
    fun getDisplayLocation(): Flow<Location?>

    @Query("SELECT * FROM location ORDER BY type, createAt")
    fun getListLocation(): Flow<List<Location>>

    @Query("SELECT * FROM location")
    suspend fun loadListLocation(): List<Location>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: List<Location>)

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

    @Query("UPDATE location set isDisplay = 1 WHERE type = 'GPS'")
    suspend fun setGPSLocationToDisplay()

    @Query("SELECT * FROM location WHERE lat = :lat AND lon = :lon AND type = 'SEARCH'")
    suspend fun getDraftLocation(lat: Double, lon: Double): Location?

    @Delete
    suspend fun deleteLocation(location: Location)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidgetLocation(widgetLocation: WidgetLocation)

    @Delete
    suspend fun deleteWidgetLocation(widgetLocation: WidgetLocation)

    @Query("SELECT * FROM widgetlocation WHERE widgetId = :widgetId")
    suspend fun getWidgetData(widgetId: Int): WidgetLocation?
}