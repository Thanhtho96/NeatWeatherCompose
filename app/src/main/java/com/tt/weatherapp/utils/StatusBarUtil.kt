package com.tt.weatherapp.utils

import android.app.Activity
import androidx.core.view.WindowCompat

object StatusBarUtil {
    fun setTransparentStatusBar(activity: Activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    }
}