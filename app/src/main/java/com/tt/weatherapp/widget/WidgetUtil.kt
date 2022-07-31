package com.tt.weatherapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll

object WidgetUtil {
    suspend fun setWidgetState(context: Context, glanceIds: List<GlanceId>, newState: WeatherInfo) {
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(
                context = context,
                definition = WeatherInfoStateDefinition,
                glanceId = glanceId,
                updateState = { newState }
            )
        }
        WeatherGlanceWidget().updateAll(context)
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