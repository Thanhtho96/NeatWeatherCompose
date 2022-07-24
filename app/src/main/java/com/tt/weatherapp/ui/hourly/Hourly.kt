package com.tt.weatherapp.ui.hourly

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.tt.weatherapp.R
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.model.HomeWeatherUnit
import com.tt.weatherapp.ui.MainViewModel
import com.tt.weatherapp.utils.DateUtil
import com.tt.weatherapp.utils.DecimalFormat
import com.tt.weatherapp.utils.StringUtils.capitalize
import kotlin.math.roundToInt

@ExperimentalFoundationApi
@Composable
fun Hourly(navController: NavController, viewModel: MainViewModel) {
    val res = LocalContext.current.resources
    val uiState = viewModel.hourly.groupBy { it.dtHeader }
    val homeWeatherUnit = HomeWeatherUnit(viewModel.locationData?.weatherData ?: return)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(vertical = 7.dp, horizontal = 12.dp))
                .defaultMinSize(minHeight = dimensionResource(id = R.dimen.actionBarSize)),
            contentAlignment = Alignment.CenterStart,
        ) {
            IconButton(modifier = Modifier.size(24.dp),
                onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    null
                )
            }
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(id = R.string.txt_next_hours),
                fontSize = 23.sp,
            )
        }

        LazyColumn(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding() + 17.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            uiState.forEach { (day, dayHourly) ->
                stickyHeader {
                    Text(
                        modifier = Modifier
                            .background(color = MaterialTheme.colors.background)
                            .fillMaxWidth()
                            .padding(bottom = 7.dp),
                        text =
                        if (DateUtils.isToday(day))
                            stringResource(id = R.string.txt_today)
                        else
                            DateUtil.format(DateUtil.DateFormat.DAY_OF_WEEK_MONTH_DAY, day),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                items(dayHourly) { hourly ->
                    Row {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(
                                        data = Constant.getWeatherIcon(hourly.weather.first().icon)
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
                                    DateUtil.DateFormat.HOUR_MINUTE,
                                    hourly.dt * 1000
                                ),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(
                                    homeWeatherUnit.tempFeelLikeHourly,
                                    hourly.temp.roundToInt().toString(),
                                    hourly.feels_like.roundToInt().toString()
                                ),
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = hourly.weather[0].description.capitalize())
                            Text(
                                text = res.getQuantityString(
                                    homeWeatherUnit.windHourly,
                                    hourly.wind_speed.roundToInt(),
                                    hourly.wind_speed,
                                    stringArrayResource(id = R.array.compass_directions)[((hourly.wind_deg % 360) / 22.5).roundToInt()]
                                ),
                                modifier = Modifier
                                    .alpha(0.7F)
                                    .padding(top = 3.dp)
                            )
                            hourly.rain?.oneHour?.let {
                                Text(
                                    text = res.getString(
                                        R.string.txt_rain_volume,
                                        DecimalFormat.format(it)
                                    ),
                                    modifier = Modifier.alpha(0.7F)
                                )
                            }
                            hourly.snow?.oneHour?.let {
                                Text(
                                    text = res.getString(
                                        R.string.txt_snow_volume,
                                        DecimalFormat.format(it)
                                    ),
                                    modifier = Modifier.alpha(0.7F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}