package com.tt.weatherapp.ui.hourly

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
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
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.statusBarsPadding
import com.tt.weatherapp.R
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.model.HomeWeatherUnit
import com.tt.weatherapp.ui.MainViewModel
import com.tt.weatherapp.utils.DateUtil
import com.tt.weatherapp.utils.StringUtils.capitalize
import kotlin.math.roundToInt

@ExperimentalFoundationApi
@Composable
fun Hourly(viewModel: MainViewModel) {
    val res = LocalContext.current.resources
    val uiState = viewModel.hourlyCustom
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
                text = stringResource(id = R.string.txt_next_hours),
                fontSize = dimensionResource(id = R.dimen.header).value.sp
            )
        }

        LazyColumn(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 10.dp),
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
                            painter = rememberImagePainter(
                                builder = {
                                    crossfade(true)
                                }, data = Constant.getWeatherIcon(hourly.weather.first().icon)
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
                                    hourly.wind_speed.roundToInt(),
                                    stringArrayResource(id = R.array.compass_directions)[((hourly.wind_deg % 360) / 22.5).roundToInt()]
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