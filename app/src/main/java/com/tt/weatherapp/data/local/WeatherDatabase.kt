package com.tt.weatherapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tt.weatherapp.model.Location
import com.tt.weatherapp.model.WidgetLocation

@Database(
    entities = [
        Location::class,
        WidgetLocation::class
    ],
    version = 1,
    autoMigrations = [
    ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao

    companion object {
        private var INSTANCE: WeatherDatabase? = null
        fun getDatabase(context: Context): WeatherDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        WeatherDatabase::class.java,
                        "weather_database"
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }
}