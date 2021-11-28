package com.tt.weatherapp.ui.daily

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.tt.weatherapp.ui.MainViewModel

@Composable
fun Daily(viewModel: MainViewModel) {
    val uiState = viewModel.dailyCustom.collectAsState().value
}