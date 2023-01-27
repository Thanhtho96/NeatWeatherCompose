package com.tt.weatherapp.widget

import androidx.lifecycle.viewModelScope
import com.tt.weatherapp.common.BaseViewModel
import com.tt.weatherapp.data.local.WeatherDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class WidgetConfigViewModel(weatherDao: WeatherDao) : BaseViewModel() {
    val listLocation = weatherDao.getListLocation().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
}