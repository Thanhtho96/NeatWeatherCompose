package com.tt.weatherapp.ui.daily

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.tt.weatherapp.R
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.model.HomeWeatherUnit
import com.tt.weatherapp.ui.MainViewModel
import com.tt.weatherapp.utils.DateUtil
import com.tt.weatherapp.utils.DecimalFormat
import kotlin.math.roundToInt

@Composable
fun Daily(viewModel: MainViewModel) {
    val res = LocalContext.current.resources
    val uiState = viewModel.weatherData
    val homeWeatherUnit = HomeWeatherUnit(viewModel.weatherData ?: return)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(vertical = 7.dp))
                .defaultMinSize(minHeight = dimensionResource(id = R.dimen.actionBarSize))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.txt_next_days),
                fontSize = dimensionResource(id = R.dimen.header).value.sp
            )
        }

        LazyColumn(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (uiState == null) return@LazyColumn

            items(uiState.daily) { daily ->
                Row {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(
                                    data = Constant.getWeatherIcon(daily.weather.first().icon)
                                ).apply(block = fun ImageRequest.Builder.() {
                                    crossfade(true)
                                }).build()
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(37.dp)
                    )
                    Column(modifier = Modifier.padding(start = 7.dp)) {
                        Text(
                            text = DateUtil.format(
                                DateUtil.DateFormat.DAY_OF_WEEK_MONTH_DAY,
                                daily.dt * 1000
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = stringResource(
                                homeWeatherUnit.highLowTemp,
                                daily.temp.min.roundToInt().toString(),
                                daily.temp.max.roundToInt().toString()
                            ),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = res.getQuantityString(
                                homeWeatherUnit.windHourly,
                                daily.wind_speed.roundToInt(),
                                daily.wind_speed,
                                stringArrayResource(id = R.array.compass_directions)[((daily.wind_deg % 360) / 22.5).roundToInt()]
                            ),
                            modifier = Modifier.padding(top = 3.dp)
                        )
                        Text(
                            text = res.getString(
                                homeWeatherUnit.dayDescription,
                                DateUtil.format(
                                    DateUtil.DateFormat.HOUR_MINUTE,
                                    daily.sunrise * 1000
                                ),
                                daily.temp.day.roundToInt().toString(),
                                daily.feels_like.day.roundToInt().toString()
                            ),
                            fontSize = 13.sp,
                            modifier = Modifier.alpha(0.7F)
                        )
                        Text(
                            text = res.getString(
                                homeWeatherUnit.nightDescription,
                                DateUtil.format(
                                    DateUtil.DateFormat.HOUR_MINUTE,
                                    daily.sunset * 1000
                                ),
                                daily.temp.night.roundToInt().toString(),
                                daily.feels_like.night.roundToInt().toString()
                            ),
                            fontSize = 13.sp,
                            modifier = Modifier.alpha(0.7F)
                        )
                        Text(
                            text = res.getString(
                                R.string.txt_uvi_description,
                                when {
                                    daily.uvi <= 4 -> {
                                        res.getString(R.string.txt_low)
                                    }
                                    daily.uvi <= 8 -> {
                                        res.getString(R.string.txt_medium)
                                    }
                                    else -> res.getString(R.string.txt_High)
                                }
                            ),
                            fontSize = 13.sp,
                            modifier = Modifier
                                .alpha(0.7F)
                                .padding(top = 3.dp)
                        )
                        daily.rain?.let {
                            Text(
                                text = res.getString(
                                    R.string.txt_rain_volume,
                                    DecimalFormat.format(it)
                                ),
                                fontSize = 13.sp,
                                modifier = Modifier.alpha(0.7F)
                            )
                        }
                        daily.snow?.let {
                            Text(
                                text = res.getString(
                                    R.string.txt_snow_volume,
                                    DecimalFormat.format(it)
                                ),
                                fontSize = 13.sp,
                                modifier = Modifier.alpha(0.7F)
                            )
                        }
                    }
                }
            }
        }
    }
}