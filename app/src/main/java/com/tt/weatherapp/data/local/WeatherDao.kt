package com.tt.weatherapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.model.Clouds
import com.tt.weatherapp.model.Coord
import com.tt.weatherapp.model.Location
import com.tt.weatherapp.model.Main
import com.tt.weatherapp.model.Rain
import com.tt.weatherapp.model.Snow
import com.tt.weatherapp.model.Sys
import com.tt.weatherapp.model.Weather
import com.tt.weatherapp.model.WidgetLocation
import com.tt.weatherapp.model.Wind
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

    @Query("UPDATE location SET lat = :lat, lon = :lon, searchName = :name, clouds = :clouds, cod = :cod, coord = :coord, dt = :dt, id = :id, main = :main, name = :name, rain = :rain, snow = :snow, sys = :sys, timezone = :timezone, visibility = :visibility, weather = :weather, wind = :wind, unit = :unit WHERE type = 'GPS'")
    suspend fun updateGPSLocation(
        lat: Double,
        lon: Double,
        clouds: Clouds,
        cod: Int,
        coord: Coord,
        dt: Long,
        id: Int,
        main: Main,
        name: String,
        rain: Rain?,
        snow: Snow?,
        sys: Sys,
        timezone: Int,
        visibility: Int,
        weather: List<Weather>,
        wind: Wind,
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidgetLocation(widgetLocation: List<WidgetLocation>)

    @Query("DELETE FROM widgetlocation WHERE widgetId IN (:widgetIds)")
    suspend fun deleteWidgetLocation(widgetIds: List<Int>)

    @Query("SELECT * FROM widgetlocation WHERE widgetId = :widgetId")
    suspend fun getWidgetData(widgetId: Int): WidgetLocation?

    @Query("SELECT * FROM widgetlocation")
    suspend fun loadListWidget(): List<WidgetLocation>

    @Query("UPDATE widgetlocation SET lat = :lat, lon = :lon, name = :name WHERE type = 'GPS'")
    suspend fun updateGPSWidgetLocation(
        lat: Double,
        lon: Double,
        name: String
    )
}