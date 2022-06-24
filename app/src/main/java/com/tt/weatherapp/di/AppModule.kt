package com.tt.weatherapp.di

import androidx.preference.PreferenceManager
import com.tt.weatherapp.App
import com.tt.weatherapp.data.local.SharedPrefHelper
import com.tt.weatherapp.data.local.WeatherDatabase
import com.tt.weatherapp.ui.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel() }
}

val appModule = module {
    single { PreferenceManager.getDefaultSharedPreferences(androidContext()) }
    single { SharedPrefHelper(get()) }
    single { App.appLifeScope }
    single { WeatherDatabase.getDatabase(androidContext()) }
}