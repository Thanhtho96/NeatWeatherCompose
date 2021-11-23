package com.tt.weatherapp.utils

import android.content.Context
import android.net.ConnectivityManager

fun isInternetAvailable(context: Context): Boolean {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val connection = manager.activeNetworkInfo
    return connection != null && connection.isConnectedOrConnecting
}