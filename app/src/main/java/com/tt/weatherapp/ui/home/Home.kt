package com.tt.weatherapp.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.flowlayout.FlowRow
import com.tt.weatherapp.R
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.model.HomeWeatherUnit
import com.tt.weatherapp.ui.MainViewModel
import com.tt.weatherapp.utils.DateUtil
import com.tt.weatherapp.utils.StringUtils.capitalize
import kotlin.math.roundToInt

@ExperimentalFoundationApi
@Composable
fun Home(navController: NavController, viewModel: MainViewModel, showSetting: () -> Unit) {
    val res = LocalContext.current.resources
    val uiState = viewModel.weatherData ?: return
    val current = uiState.current
    val daily = uiState.daily
    val homeWeatherUnit = HomeWeatherUnit(uiState)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = dimensionResource(id = R.dimen.actionBarSize))
                .padding(horizontal = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(modifier = Modifier.then(Modifier.size(24.dp)),
                onClick = { showSetting.invoke() }) {
                Icon(
                    Icons.Default.Settings,
                    ""
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(
                        text = res.getString(
                            R.string.updated_at, DateUtil.format(
                                DateUtil.DateFormat.HOUR_MINUTE,
                                current.dt * 1000
                            )
                        ),
                        fontSize = 14.sp
                    )

                    Row {
                        Text(
                            text = res.getString(
                                homeWeatherUnit.currentTemp,
                                current.temp.roundToInt().toString()
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 33.sp,
                            modifier = Modifier.padding(end = 7.dp)
                        )
                        Text(
                            text = res.getString(
                                homeWeatherUnit.highLowTemp,
                                daily[0].temp.min.roundToInt().toString(),
                                daily[0].temp.max.roundToInt().toString()
                            )
                        )
                    }

                    Text(text = uiState.location, fontSize = 20.sp)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(
                                        data = Constant.getWeatherIcon(current.weather[0].icon)
                                    ).apply(block = fun ImageRequest.Builder.() {
                                        crossfade(true)
                                    }).build()
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(37.dp)
                        )

                        Text(text = current.weather[0].main)
                    }

                    Text(
                        text = res.getString(
                            homeWeatherUnit.weatherDescription,
                            current.weather[0].description.capitalize(),
                            daily[0].temp.max.roundToInt().toString(),
                            res.getQuantityString(
                                homeWeatherUnit.windDescription,
                                current.wind_speed.roundToInt(),
                                current.wind_speed.roundToInt(),
                                res.getStringArray(R.array.compass_directions)[((current.wind_deg % 360) / 22.5).roundToInt()],
                            )
                        )
                    )

                    Text(
                        modifier = Modifier.padding(top = 10.dp),
                        text = res.getString(R.string.txt_current_weather),
                        fontSize = 19.sp
                    )

                    BoxWithConstraints(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp)
                    ) {
                        val boxDimen = ((maxWidth.value - 12 * 2) / 3).dp

                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            FlowRow(
                                mainAxisSpacing = 12.dp,
                                crossAxisSpacing = 12.dp
                            ) {
                                viewModel.listCurrentWeatherData.forEach { info ->
                                    Column(
                                        modifier = Modifier
                                            .size(boxDimen)
                                            .background(Color.DarkGray, RoundedCornerShape(9.dp))
                                            .padding(vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Image(
                                            modifier = Modifier
                                                .height(23.dp)
                                                .width(23.dp),
                                            painter = painterResource(id = info.icon),
                                            contentDescription = null
                                        )
                                        Text(text = res.getString(info.name))
                                        Text(
                                            text =
                                            if (info.unit != null) res.getString(
                                                info.unit,
                                                info.data
                                            )
                                            else info.data,
                                            fontSize = 21.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    modifier = Modifier.padding(
                        top = 10.dp,
                        bottom = 7.dp,
                        start = 12.dp,
                        end = 12.dp
                    ),
                    text = res.getString(R.string.txt_hourly),
                    fontSize = 19.sp
                )

                LazyRow(
                    contentPadding = PaddingValues(
                        horizontal = 12.dp
                    ), horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    items(viewModel.hourly) { hourly ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 5.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    homeWeatherUnit.currentTemp,
                                    hourly.temp.roundToInt().toString(),
                                ),
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            val rain = if (hourly.pop != null && hourly.pop > 0) {
                                stringResource(
                                    R.string.txt_percentage,
                                    (hourly.pop * 100).roundToInt(),
                                )
                            } else {
                                ""
                            }
                            Text(
                                text = rain,
                                color = colorResource(id = R.color.blue),
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(
                                        LocalContext.current
                                    ).data(
                                        data = Constant.getWeatherIcon(hourly.weather[0].icon)
                                    ).apply(block = fun ImageRequest.Builder.() {
                                        crossfade(true)
                                    }).build()
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(37.dp)
                            )
                            Text(
                                text = DateUtil.format(
                                    DateUtil.DateFormat.HOUR_MINUTE,
                                    hourly.dt * 1000
                                ), fontSize = 17.sp
                            )
                        }
                    }
                }

                BoxWithConstraints(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp, horizontal = 12.dp)
                ) {
                    Column {
                        Text(
                            modifier = Modifier.padding(top = 10.dp, bottom = 7.dp),
                            text = res.getString(R.string.txt_daily),
                            fontSize = 19.sp
                        )

                        Row {
                            viewModel.listDailyTempInfo.forEach {
                                Column(
                                    modifier = Modifier
                                        .width(this@BoxWithConstraints.maxWidth / 7),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(Dp(it.topMargin)))
                                    Text(text = it.maxTemp)
                                    Spacer(
                                        modifier = Modifier
                                            .background(
                                                shape = RoundedCornerShape(3.dp),
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0xFFFF3300),
                                                        Color(0xFFF35625),
                                                        Color(0xFFFFC32B),
                                                    )
                                                )
                                            )
                                            .height(Dp(it.height))
                                            .width(5.dp)
                                    )
                                    Text(text = it.minTemp)
                                }
                            }
                        }
                        Row {
                            viewModel.listDailyTempInfo.forEach {
                                Column(
                                    modifier = Modifier
                                        .width(this@BoxWithConstraints.maxWidth / 7),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            ImageRequest.Builder(
                                                LocalContext.current
                                            ).data(
                                                data = it.icon
                                            ).apply(block = fun ImageRequest.Builder.() {
                                                crossfade(true)
                                            }).build()
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(37.dp)
                                    )
                                    Text(text = it.dayOfWeek)
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
