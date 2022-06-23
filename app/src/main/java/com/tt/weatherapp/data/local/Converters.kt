package com.tt.weatherapp.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.model.Current
import com.tt.weatherapp.model.Daily
import com.tt.weatherapp.model.Hourly

class Converters {
    @TypeConverter
    fun fromListHourlyToString(list: List<Hourly>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromStringToListHourly(value: String): List<Hourly> {
        val listType = object : TypeToken<List<Hourly>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromListDailyToString(list: List<Daily>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromStringToListDaily(value: String): List<Daily> {
        val listType = object : TypeToken<List<Daily>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromUnitToString(unit: Constant.Unit): String {
        return Gson().toJson(unit)
    }

    @TypeConverter
    fun fromStringToUnit(value: String): Constant.Unit {
        val listType = object : TypeToken<Constant.Unit>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromCurrentToString(current: Current): String {
        return Gson().toJson(current)
    }

    @TypeConverter
    fun fromStringToCurrent(value: String): Current {
        val listType = object : TypeToken<Current>() {}.type
        return Gson().fromJson(value, listType)
    }
}