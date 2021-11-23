package com.tt.weatherapp.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun Home(navController: NavController) {
    /*...*/

    Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "Home", modifier = Modifier.align(Alignment.Center))
    }
    /*...*/
}