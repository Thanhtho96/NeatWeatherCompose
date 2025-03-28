package com.tt.weatherapp.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import coil3.BitmapImage
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.tt.weatherapp.R
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.data.local.WeatherDao
import com.tt.weatherapp.model.HomeWeatherUnit
import com.tt.weatherapp.model.LocationType
import com.tt.weatherapp.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.roundToInt

class SmallWeatherAppWidgetProvider : GlanceAppWidgetReceiver(), KoinComponent {
    override val glanceAppWidget = WeatherGlanceWidget()
    private val weatherDao by inject<WeatherDao>()
    private val scope by inject<CoroutineScope>()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WeatherWorker.enqueue(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        val manager = GlanceAppWidgetManager(context)
        scope.launch {
            val glanceIds = manager.getGlanceIds(WeatherGlanceWidget::class.java)
            if (glanceIds.isEmpty()) WeatherWorker.cancel(context)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        scope.launch {
            weatherDao.deleteWidgetLocation(appWidgetIds.toList())
        }
    }
}

class MediumWeatherAppWidgetProvider : GlanceAppWidgetReceiver(), KoinComponent {
    override val glanceAppWidget = WeatherGlanceWidget()
    private val weatherDao by inject<WeatherDao>()
    private val scope by inject<CoroutineScope>()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WeatherWorker.enqueue(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        val manager = GlanceAppWidgetManager(context)
        scope.launch {
            val glanceIds = manager.getGlanceIds(WeatherGlanceWidget::class.java)
            if (glanceIds.isEmpty()) WeatherWorker.cancel(context)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        scope.launch {
            weatherDao.deleteWidgetLocation(appWidgetIds.toList())
        }
    }
}

class WeatherGlanceWidget : GlanceAppWidget() {

    companion object {
        private val smallMode = DpSize(140.dp, 80.dp)
        private val mediumMode = DpSize(300.dp, 80.dp)
    }

    override val stateDefinition = WeatherInfoStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(smallMode, mediumMode)
    )

    @Composable
    private fun Content() {
        val weatherInfo = currentState<WeatherInfo>()
        val size = LocalSize.current
        val context = LocalContext.current

        when (weatherInfo) {
            WeatherInfo.Loading -> {
                AppWidgetBox(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is WeatherInfo.Available -> {
                val location = weatherInfo.location
                val weatherData = location.weatherData
                val weatherIcon = weatherData?.weather?.first()?.icon ?: ""
                val imagePath = Constant.getWeatherIcon(weatherIcon)
                var image by remember(imagePath) { mutableStateOf<Bitmap?>(null) }

                when (size) {
                    smallMode, mediumMode -> {
                        AppWidgetColumn(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = GlanceModifier
                                .appWidgetBackground()
                                .padding(
                                    vertical = 10.dp,
                                    horizontal = if (size == smallMode) 10.dp else 20.dp
                                )
                                .clickable(
                                    actionRunCallback<UpdateWeatherAction>(
                                        actionParametersOf(isGlobalWidgetAction to true)
                                    )
                                )
                        ) {
                            Row(modifier = GlanceModifier.fillMaxWidth()) {
                                Text(
                                    text = location.searchName,
                                    modifier = GlanceModifier.defaultWeight(),
                                    maxLines = 1,
                                    style = TextStyle(
                                        fontSize = if (size == smallMode) 15.sp else 17.sp,
                                        color = ColorProvider(
                                            color = Color.White
                                        )
                                    )
                                )
                                if (location.type == LocationType.SEARCH) return@Row
                                Spacer(modifier = GlanceModifier.size(7.dp))
                                Image(
                                    provider = ImageProvider(R.drawable.ic_location_on),
                                    contentDescription = null,
                                    modifier = GlanceModifier.size(if (size == smallMode) 20.dp else 23.dp),
                                )
                            }

                            if (weatherData != null) {
                                val homeWeatherUnit = HomeWeatherUnit(weatherData)

                                Text(
                                    text = context.getString(
                                        if (size == smallMode) homeWeatherUnit.onlyDegreeSymbol else homeWeatherUnit.currentTemp,
                                        weatherData.main.temp.roundToInt()
                                    ),
                                    style = TextStyle(
                                        fontSize = if (size == smallMode) 40.sp else 50.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorProvider(
                                            color = Color.White
                                        )
                                    )
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LaunchedEffect(imagePath) {
                                        image = context.loadImage(imagePath)
                                    }

                                    image?.let {
                                        Image(
                                            provider = ImageProvider(it),
                                            contentDescription = null,
                                            modifier = GlanceModifier.size(if (size == smallMode) 27.dp else 32.dp),
                                            contentScale = ContentScale.FillBounds
                                        )
                                    } ?: @Composable {
                                        CircularProgressIndicator()
                                    }

                                    Spacer(modifier = GlanceModifier.size(4.dp))

                                    Text(
                                        text = weatherData.weather[0].main,
                                        style = TextStyle(
                                            fontSize = if (size == smallMode) 13.sp else 15.sp,
                                            color = ColorProvider(
                                                color = Color.White
                                            )
                                        )
                                    )
                                }

                                Text(
                                    text = "${
                                        context.getString(
                                            if (size == smallMode) homeWeatherUnit.highLowTempWidget else homeWeatherUnit.highLowFullTempWidget,
                                            weatherData.main.tempMin.roundToInt().toString(),
                                            weatherData.main.tempMax.roundToInt().toString()
                                        )
                                    }  ${
                                        if (size != smallMode) {
                                            context.getString(
                                                homeWeatherUnit.feelLikeFull,
                                                weatherData.main.feelsLike.roundToInt().toString(),
                                                weatherData.main.feelsLike.roundToInt().toString()
                                            )
                                        } else ""
                                    }",
                                    style = TextStyle(
                                        fontSize = if (size == smallMode) 12.sp else 13.sp,
                                        color = ColorProvider(
                                            color = Color.White
                                        )
                                    )
                                )
                            } else {
                                Text(
                                    text = context.getString(R.string.null_face),
                                    style = TextStyle(
                                        fontSize = if (size == smallMode) 40.sp else 50.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorProvider(
                                            color = Color.White
                                        )
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
                    Text(
                        text = context.getString(weatherInfo.message),
                        style = TextStyle(
                            color = ColorProvider(color = Color.White)
                        )
                    )
                    Spacer(modifier = GlanceModifier.size(5.dp))
                    Button(
                        context.getString(R.string.refresh),
                        actionRunCallback<UpdateWeatherAction>(
                            actionParametersOf(isGlobalWidgetAction to false)
                        )
                    )
                }
            }
        }
    }

    private suspend fun Context.loadImage(url: String): Bitmap? {
        val request = ImageRequest.Builder(this).data(url).build()

        // Request the image to be loaded and throw error if it failed
        return when (val result = imageLoader.execute(request)) {
            is ErrorResult -> null
            is SuccessResult -> (result.image as BitmapImage).bitmap
        }
    }
}

private val isGlobalWidgetAction = ActionParameters.Key<Boolean>("is_global_action")

class UpdateWeatherAction : ActionCallback {
    @OptIn(
        ExperimentalPermissionsApi::class,
        ExperimentalFoundationApi::class,
    )
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        if (parameters[isGlobalWidgetAction] == true) {
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