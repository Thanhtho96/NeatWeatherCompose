package com.tt.weatherapp.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher

object Utils {
    fun openSettings(activity: Activity, startSettingsLauncher: ActivityResultLauncher<Intent>) {
        val uri = Uri.fromParts("package", activity.packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        startSettingsLauncher.launch(intent)
    }
}