package com.tt.weatherapp.di

import com.tt.weatherapp.App
import com.tt.weatherapp.ui.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
}

val appModule = module {
    single { App.appLifeScope }
}