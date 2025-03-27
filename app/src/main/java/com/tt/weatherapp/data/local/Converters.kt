package com.tt.weatherapp.data.local

import androidx.room.TypeConverter
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.model.Clouds
import com.tt.weatherapp.model.Coord
import com.tt.weatherapp.model.Main
import com.tt.weatherapp.model.Rain
import com.tt.weatherapp.model.Snow
import com.tt.weatherapp.model.Sys
import com.tt.weatherapp.model.Weather
import com.tt.weatherapp.model.Wind
import com.tt.weatherapp.utils.JsonUtil
import kotlinx.serialization.encodeToString

class Converters {
    @TypeConverter
    fun fromCloudsToString(clouds: Clouds): String {
        return JsonUtil.json.encodeToString(clouds)
    }

    @TypeConverter
    fun fromStringToClouds(value: String): Clouds {
        return JsonUtil.json.decodeFromString(value)
    }

    @TypeConverter
    fun fromCoordToString(coord: Coord): String {
        return JsonUtil.json.encodeToString(coord)
    }

    @TypeConverter
    fun fromStringToCoord(value: String): Coord {
        return JsonUtil.json.decodeFromString(value)
    }

    @TypeConverter
    fun fromMainToString(main: Main): String {
        return JsonUtil.json.encodeToString(main)
    }

    @TypeConverter
    fun fromStringToMain(value: String): Main {
        return JsonUtil.json.decodeFromString(value)
    }

    @TypeConverter
    fun fromRainToString(rain: Rain): String {
        return JsonUtil.json.encodeToString(rain)
    }

    @TypeConverter
    fun fromStringToRain(value: String): Rain {
        return JsonUtil.json.decodeFromString(value)
    }

    @TypeConverter
    fun fromSnowToString(snow: Snow): String {
        return JsonUtil.json.encodeToString(snow)
    }

    @TypeConverter
    fun fromStringToSnow(value: String): Snow {
        return JsonUtil.json.decodeFromString(value)
    }

    @TypeConverter
    fun fromSysToString(sys: Sys): String {
        return JsonUtil.json.encodeToString(sys)
    }

    @TypeConverter
    fun fromStringToSys(value: String): Sys {
        return JsonUtil.json.decodeFromString(value)
    }

    @TypeConverter
    fun fromWeatherListToString(weather: List<Weather>): String {
        return JsonUtil.json.encodeToString(weather)
    }

    @TypeConverter
    fun fromStringToWeatherList(value: String): List<Weather> {
        return JsonUtil.json.decodeFromString(value)
    }

    @TypeConverter
    fun fromWindToString(wind: Wind): String {
        return JsonUtil.json.encodeToString(wind)
    }

    @TypeConverter
    fun fromStringToWind(value: String): Wind {
        return JsonUtil.json.decodeFromString(value)
    }

    @TypeConverter
    fun fromUnitToString(unit: Constant.Unit): String {
        return JsonUtil.json.encodeToString(unit)
    }

    @TypeConverter
    fun fromStringToUnit(value: String): Constant.Unit {
        return JsonUtil.json.decodeFromString(value)
    }
}
