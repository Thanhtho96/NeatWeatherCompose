package com.tt.weatherapp.data.local

import android.content.SharedPreferences
import com.tt.weatherapp.common.Constant

class SharedPrefHelper constructor(private val sharedPref: SharedPreferences) {

    fun setChosenUnit(chosenUnit: Constant.Unit) =
        sharedPref.edit().putString(UNIT, chosenUnit.value).commit()

    fun getChosenUnit() =
        Constant.Unit.fromString(sharedPref.getString(UNIT, Constant.Unit.METRIC.value) ?: "")

    companion object {
        private const val UNIT = "UNIT"
    }
}