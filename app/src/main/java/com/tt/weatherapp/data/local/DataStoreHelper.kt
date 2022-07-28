package com.tt.weatherapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tt.weatherapp.App.Companion.dataStore
import com.tt.weatherapp.common.Constant.Unit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreHelper {
    suspend fun setChosenUnit(context: Context, chosenUnit: Unit) {
        context.dataStore.edit { settings ->
            settings[UNIT] = chosenUnit.value
        }
    }

    fun listenChosenUnit(context: Context) = context.dataStore.data
        .map { preferences ->
            Unit.fromString(preferences[UNIT] ?: Unit.METRIC.value)
        }

    suspend fun getChosenUnit(context: Context) =
        Unit.fromString(context.dataStore.data.first()[UNIT] ?: Unit.METRIC.value)

    companion object {
        private val UNIT = stringPreferencesKey("UNIT")
    }
}