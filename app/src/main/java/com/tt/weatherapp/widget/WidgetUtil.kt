package com.tt.weatherapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.updateAppWidgetState

object WidgetUtil {
    suspend fun setWidgetState(context: Context, map: Map<GlanceId?, WeatherInfo>) {
        map.filter { it.key != null }.forEach { info ->
            updateAppWidgetState(
                context = context,
                definition = WeatherInfoStateDefinition,
                glanceId = info.key!!,
                updateState = { info.value }
            )
            WeatherGlanceWidget().update(context, info.key!!)
        }
    }

    suspend fun setWidgetState(context: Context, glanceId: GlanceId, newState: WeatherInfo) {
        updateAppWidgetState(
            context = context,
            definition = WeatherInfoStateDefinition,
            glanceId = glanceId,
            updateState = { newState }
        )
        WeatherGlanceWidget().update(context, glanceId)
    }
}