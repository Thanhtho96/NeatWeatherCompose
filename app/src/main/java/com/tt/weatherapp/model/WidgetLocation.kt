package com.tt.weatherapp.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WidgetLocation(
    @PrimaryKey val widgetId: Int,
    @Embedded val location: Location
)
