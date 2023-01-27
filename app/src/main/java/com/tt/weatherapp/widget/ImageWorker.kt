package com.tt.weatherapp.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.*
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.data.local.WeatherDao
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class ImageWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters), KoinComponent {

    private val weatherDao by inject<WeatherDao>()

    companion object {

        private val uniqueWorkName = ImageWorker::class.java.simpleName

        fun enqueue(context: Context, icon: String, glanceId: GlanceId) {
            val manager = WorkManager.getInstance(context)
            val glanceManager = GlanceAppWidgetManager(context)
            val appWidgetId = glanceManager.getAppWidgetId(glanceId)
            val requestBuilder = OneTimeWorkRequestBuilder<ImageWorker>().apply {
                addTag(glanceId.toString())
                setInputData(
                    Data.Builder()
                        .putString("icon", icon)
                        .putInt("appWidgetId", appWidgetId)
                        .build()
                )
            }
            val workPolicy = ExistingWorkPolicy.KEEP

            manager.enqueueUniqueWork(
                uniqueWorkName + icon,
                workPolicy,
                requestBuilder.build()
            )

            // Temporary workaround to avoid WM provider to disable itself and trigger an
            // app widget update
            manager.enqueueUniqueWork(
                "$uniqueWorkName-workaround",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<ImageWorker>().apply {
                    setInitialDelay(365, TimeUnit.DAYS)
                }.build()
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val icon = inputData.getString("icon") ?: return Result.failure()
            val appWidgetId = inputData.getInt("appWidgetId", 0)
            val uri = getRandomImage(icon)
            updateImageWidget(uri, appWidgetId)
            Result.success()
        } catch (e: Exception) {
            Log.e(uniqueWorkName, "Error while loading image", e)
            if (runAttemptCount < 10) {
                // Exponential backoff strategy will avoid the request to repeat
                // too fast in case of failures.
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun updateImageWidget(uri: String, appWidgetId: Int) {
        val glanceManager = GlanceAppWidgetManager(context)
        val glanceId = glanceManager.getGlanceIdBy(appWidgetId)
        val widgetData = weatherDao.getWidgetData(appWidgetId) ?: return

        WidgetUtil.setWidgetState(
            context,
            glanceId,
            WeatherInfo.Available(widgetData.location, uri)
        )
    }

    /**
     * Use Coil to load images into the cache based on the provided icon.
     * This method returns the path of the cached image, which you can send to the widget.
     */
    @OptIn(ExperimentalCoilApi::class)
    private suspend fun getRandomImage(icon: String): String {
        val url = Constant.getWeatherIcon(icon)
        val request = ImageRequest.Builder(context)
            .data(url)
            .build()

        // Request the image to be loaded and throw error if it failed
        with(context.imageLoader) {
            val result = execute(request)
            if (result is ErrorResult) {
                throw result.throwable
            }
        }

        // Get the path of the loaded image from DiskCache.
        val path = context.imageLoader.diskCache?.get(url)?.use { snapshot ->
            snapshot.data.toFile().path
        }
        return requireNotNull(path) {
            "Couldn't find cached file"
        }
    }
}