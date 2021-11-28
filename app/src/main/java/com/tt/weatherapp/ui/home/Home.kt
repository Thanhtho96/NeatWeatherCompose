package com.tt.weatherapp.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.tt.weatherapp.ui.MainViewModel

@Composable
fun Home(navController: NavController, viewModel: MainViewModel) {
    val uiState = viewModel.weatherData.collectAsState().value
}