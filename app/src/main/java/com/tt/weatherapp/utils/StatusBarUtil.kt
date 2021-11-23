package com.tt.weatherapp.utils

import android.app.Activity
import android.view.View
import androidx.core.view.WindowCompat

object StatusBarUtil {
    fun setTransparentStatusBar(activity: Activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    }

    @Suppress("DEPRECATION")
    fun setTransparentStatusBarHideNavigation(activity: Activity) {
        activity.window.decorView.apply {
            systemUiVisibility =
                systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }
}