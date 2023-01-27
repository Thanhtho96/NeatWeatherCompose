package com.tt.weatherapp.data.local

import android.content.Context
import androidx.room.*
import com.tt.weatherapp.model.Location
import com.tt.weatherapp.model.WidgetLocation

@Database(
    entities = [
        Location::class,
        WidgetLocation::class
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = DatabaseMigrations.Schema1to2::class),
        AutoMigration(from = 2, to = 3)
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