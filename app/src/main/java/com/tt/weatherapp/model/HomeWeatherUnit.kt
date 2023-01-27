package com.tt.weatherapp.model

import com.tt.weatherapp.R
import com.tt.weatherapp.common.Constant

class HomeWeatherUnit(weatherData: WeatherData) {
    val currentTemp: Int
    val onlyDegreeSymbol: Int = R.string.txt_temp_without_unit
    val highLowTempWidget: Int = R.string.txt_without_unit_low_high_temp
    val highLowTemp: Int
    val windDescription: Int
    val tempFeelLikeHourly: Int
    val windHourly: Int
    val dayDescription: Int
    val nightDescription: Int
    val feelLike: Int
    val dewPoint: Int

    init {
        when (weatherData.unit) {
            Constant.Unit.METRIC -> {
                currentTemp = R.string.txt_celsius_temp
                highLowTemp = R.string.txt_celsius_low_high_temp
                windDescription = R.plurals.txt_wind_meter_description
                tempFeelLikeHourly = R.string.txt_celsius_temp_and_feel_like
                windHourly = R.plurals.txt_wind_meter_per_sec_compass_direction
                dayDescription = R.string.txt_metric_day_description
                nightDescription = R.string.txt_metric_night_description
                feelLike = R.string.txt_celsius_feel_like
                dewPoint = R.string.txt_celsius_dew_point
            }
            Constant.Unit.IMPERIAL -> {
                currentTemp = R.string.txt_fahrenheit_temp
                highLowTemp = R.string.txt_fahrenheit_low_high_temp
                windDescription = R.plurals.txt_wind_imperial_description
                tempFeelLikeHourly = R.string.txt_fahrenheit_temp_and_feel_like
                windHourly = R.plurals.txt_wind_imperial_per_hour_compass_direction
                dayDescription = R.string.txt_imperial_day_description
                nightDescription = R.string.txt_imperial_night_description
                feelLike = R.string.txt_fahrenheit_feel_like
                dewPoint = R.string.txt_fahrenheit_dew_point
            }
        }
    }
}
