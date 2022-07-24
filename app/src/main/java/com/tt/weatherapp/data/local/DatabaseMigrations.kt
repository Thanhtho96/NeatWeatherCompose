package com.tt.weatherapp.data.local

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec

class DatabaseMigrations {
    @DeleteTable(
        tableName = "weatherdata"
    )
    class Schema1to2 : AutoMigrationSpec
}
