package com.tt.weatherapp.utils

import android.content.Context
import android.view.WindowManager

object WindowsUtil {
    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val windowMetrics = windowManager.currentWindowMetrics
        return windowMetrics.bounds.height()
    }
}