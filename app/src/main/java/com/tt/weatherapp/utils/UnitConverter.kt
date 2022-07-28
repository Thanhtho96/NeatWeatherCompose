package com.tt.weatherapp.utils

import com.tt.weatherapp.common.Constant

fun Double.convertTemperature(newUnit: Constant.Unit): Double {
    return when (newUnit) {
        Constant.Unit.METRIC -> fromFahrenheitToCelsius()
        Constant.Unit.IMPERIAL -> fromCelsiusToFahrenheit()
    }
}

fun Double.convertSpeed(newUnit: Constant.Unit): Double {
    return when (newUnit) {
        Constant.Unit.METRIC -> fromImperialPerHourToMeterPerSec()
        Constant.Unit.IMPERIAL -> fromMeterPerSecToImperialPerHour()
    }
}

private fun Double.fromCelsiusToFahrenheit() = (this * 9F / 5) + 32

private fun Double.fromMeterPerSecToImperialPerHour() = this * 2.237

private fun Double.fromFahrenheitToCelsius() = (this - 32) * 5F / 9

private fun Double.fromImperialPerHourToMeterPerSec() = this / 2.237