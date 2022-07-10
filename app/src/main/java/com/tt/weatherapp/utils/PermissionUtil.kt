package com.tt.weatherapp.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtil {
    private fun isPermissionGrant(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun isLocationPermissionGranted(context: Context) =
        isPermissionGrant(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
}