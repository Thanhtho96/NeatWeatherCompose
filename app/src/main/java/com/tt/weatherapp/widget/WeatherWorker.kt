package com.tt.weatherapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.*
import com.tt.weatherapp.data.local.WeatherDao
import com.tt.weatherapp.data.repositories.AppRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class WeatherWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), KoinComponent {

    private val weatherDao by inject<WeatherDao>()
    private val appRepository by inject<AppRepository>()

    companion object {
        private val uniqueWorkName = WeatherWorker::class.java.simpleName

        /**
         * Enqueues a new worker to refresh weather data only if not enqueued already
         *
         * Note: if you would like to have different workers per widget instance you could provide
         * the unique name based on some criteria (e.g selected weather location).
         */
        fun enqueue(context: Context) {
            val manager = WorkManager.getInstance(context)
            val requestBuilder = PeriodicWorkRequestBuilder<WeatherWorker>(
                15,
                TimeUnit.MINUTES
            )

            // Replace any enqueued work and expedite the request
            val workPolicy = ExistingPeriodicWorkPolicy.KEEP

            manager.enqueueUniquePeriodicWork(
                uniqueWorkName,
                workPolicy,
                requestBuilder.build()
            )
        }

        /**
         * Cancel any ongoing worker
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
        }
    }

    override suspend fun doWork(): Result {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(WeatherGlanceWidget::class.java)
        return try {
            Result.success()
        } catch (e: Exception) {
            WidgetUtil.setWidgetState(
                context,
                glanceIds,
                WeatherInfo.Unavailable()
            )
            if (runAttemptCount < 10) {
                // Exponential backoff strategy will avoid the request to repeat
                // too fast in case of failures.
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}