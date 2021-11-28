package com.tt.weatherapp.ui.hourly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.tt.weatherapp.ui.MainViewModel

@Composable
fun Hourly(viewModel: MainViewModel) {
    val uiState = viewModel.hourlyCustom.collectAsState().value
}