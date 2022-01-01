package com.tt.weatherapp.model

data class DailyTempInfo(
    val maxTemp: String,
    val minTemp: String,
    val icon: String,
    val dayOfWeek: String,
    val topMargin: Float,
    val height: Float
)
