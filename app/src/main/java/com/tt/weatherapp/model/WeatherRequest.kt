package com.tt.weatherapp.model

data class WeatherRequest(val isRefresh: Boolean = true, val isForce: Boolean = false)