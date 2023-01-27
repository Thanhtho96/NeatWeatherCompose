package com.tt.weatherapp.di

import com.tt.weatherapp.App
import com.tt.weatherapp.common.Constant.Dispatcher
import com.tt.weatherapp.data.local.DataStoreHelper
import com.tt.weatherapp.data.local.WeatherDatabase
import com.tt.weatherapp.ui.MainViewModel
import com.tt.weatherapp.widget.WidgetConfigViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainViewModel(
            get(),
            get(),
            get(),
            get(named(Dispatcher.IO)),
            get(named(Dispatcher.MAIN))
        )
    }
    viewModel { WidgetConfigViewModel(get()) }
}

val appModule = module {
    single { DataStoreHelper() }
    single { App.appLifeScope }
    single { App.searchEngine }
    single { WeatherDatabase.getDatabase(androidContext()).weatherDao() }
}

val dispatcherModule = module {
    single(named(Dispatcher.MAIN)) { provideMainDispatcher() }
    single(named(Dispatcher.DEFAULT)) { provideDefaultDispatcher() }
    single(named(Dispatcher.IO)) { provideIODispatcher() }
    single(named(Dispatcher.UNCONFINED)) { provideUnconfinedDispatcher() }
}

private fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
private fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
private fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
private fun provideUnconfinedDispatcher(): CoroutineDispatcher = Dispatchers.Unconfined