package com.tt.weatherapp.widget

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.tt.weatherapp.R
import com.tt.weatherapp.model.HomeWeatherUnit
import com.tt.weatherapp.model.LocationType
import com.tt.weatherapp.ui.MainActivity
import kotlin.math.roundToInt

class SmallWeatherAppWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = WeatherGlanceWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WeatherWorker.enqueue(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WeatherWorker.cancel(context)
    }
}

class MediumWeatherAppWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = WeatherGlanceWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WeatherWorker.enqueue(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WeatherWorker.cancel(context)
    }
}

class WeatherGlanceWidget : GlanceAppWidget() {

    companion object {
        private val smallMode = DpSize(140.dp, 80.dp)
        private val mediumMode = DpSize(300.dp, 80.dp)
    }

    override val stateDefinition = WeatherInfoStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(smallMode, mediumMode)
    )

    @Composable
    override fun Content() {
        val weatherInfo = currentState<WeatherInfo>()
        val size = LocalSize.current
        val context = LocalContext.current

        GlanceTheme {
            when (weatherInfo) {
                WeatherInfo.Loading -> {
                    AppWidgetBox(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is WeatherInfo.Available -> {
                    val location = weatherInfo.location
                    val weatherData = location.weatherData
                    val weatherIcon = weatherData?.current?.weather?.first()?.icon ?: ""
                    val imagePath = weatherInfo.imagePath

                    when (size) {
                        smallMode, mediumMode -> {
                            AppWidgetColumn(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = GlanceModifier
                                    .appWidgetBackground()
                                    .padding(10.dp)
                                    .clickable(
                                        actionRunCallback<UpdateWeatherAction>(
                                            actionParametersOf(IsGlobalWidgetAction to true)
                                        )
                                    )
                            ) {
                                Row(modifier = GlanceModifier.fillMaxWidth()) {
                                    Text(
                                        text = location.name,
                                        modifier = GlanceModifier.defaultWeight(),
                                        maxLines = 1,
                                    )
                                    if (location.type == LocationType.SEARCH) return@Row
                                    Spacer(modifier = GlanceModifier.size(7.dp))
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_location_on),
                                        contentDescription = null,
                                        modifier = GlanceModifier.size(20.dp),
                                    )
                                }

                                if (weatherData != null) {
                                    val homeWeatherUnit = HomeWeatherUnit(weatherData)

                                    Text(
                                        text = context.getString(
                                            homeWeatherUnit.onlyDegreeSymbol,
                                            weatherData.current.temp.roundToInt()
                                        ),
                                        style = TextStyle(
                                            fontSize = 40.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (imagePath != null) {
                                            Image(
                                                provider = getImageProvider(imagePath),
                                                contentDescription = null,
                                            )
                                        } else {
                                            val glanceId = LocalGlanceId.current
                                            SideEffect {
                                                ImageWorker.enqueue(context, weatherIcon, glanceId)
                                            }
                                        }

                                        Text(
                                            text = weatherData.current.weather[0].main,
                                            style = TextStyle(
                                                fontSize = 12.sp
                                            )
                                        )
                                    }

                                    Text(
                                        text = context.getString(
                                            homeWeatherUnit.highLowTempWidget,
                                            weatherData.daily[0].temp.min.roundToInt().toString(),
                                            weatherData.daily[0].temp.max.roundToInt().toString()
                                        ),
                                        style = TextStyle(
                                            fontSize = 12.sp
                                        )
                                    )
                                } else {
                                    Text(
                                        text = context.getString(R.string.null_face),
                                        style = TextStyle(
                                            fontSize = 40.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                is WeatherInfo.Unavailable -> {
                    AppWidgetColumn(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(context.getString(weatherInfo.message))
                        Button(
                            context.getString(R.string.refresh),
                            actionRunCallback<UpdateWeatherAction>(
                                actionParametersOf(IsGlobalWidgetAction to false)
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Get the bitmap from the cache file
     *
     * Note: Because it's a single image resized to the available space, you
     * probably won't reach the memory limit. If you do reach the memory limit,
     * you'll need to generate a URI granting permissions to the launcher.
     *
     * More info:
     * https://developer.android.com/training/secure-file-sharing/share-file#GrantPermissions
     */
    private fun getImageProvider(path: String): ImageProvider {
        val bitmap = BitmapFactory.decodeFile(path)
        return ImageProvider(bitmap)
    }
}

private val IsGlobalWidgetAction = ActionParameters.Key<Boolean>("is_global_action")

class UpdateWeatherAction : ActionCallback {
    @OptIn(
        ExperimentalPermissionsApi::class,
        ExperimentalFoundationApi::class,
        ExperimentalMaterialApi::class
    )
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        if (parameters[IsGlobalWidgetAction] == true) {
            context.startActivity(
                Intent(context, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return
        }
        // Force the worker to refresh
        WeatherWorker.enqueue(context)
    }
}