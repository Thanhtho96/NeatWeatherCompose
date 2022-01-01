package com.tt.weatherapp.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.accompanist.flowlayout.FlowRow
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
fun Home(navController: NavController, viewModel: MainViewModel) {
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
                .padding(
                    horizontal = 12.dp
                ), verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = "", onValueChange = {

                }, modifier = Modifier.weight(1F), colors =
                TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.background,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            IconButton(modifier = Modifier.then(Modifier.size(24.dp)),
                onClick = { }) {
                Icon(
                    Icons.Default.Settings,
                    ""
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = 12.dp
            )
        ) {
            item {
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

                Text(text = "Việt Nam", fontSize = 20.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = rememberImagePainter(
                            builder = {
                                crossfade(true)
                            }, data = Constant.getWeatherIcon(current.weather[0].icon)
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

                Text(text = res.getString(R.string.txt_current_weather), fontSize = 19.sp)

                BoxWithConstraints(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp)
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
                                        if (info.unit != null) res.getString(info.unit, info.data)
                                        else info.data,
                                        fontSize = 21.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = res.getString(R.string.txt_daily),
                            fontSize = 19.sp
                        )

                        FlowRow {
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
                        FlowRow {
                            viewModel.listDailyTempInfo.forEach {
                                Column(
                                    modifier = Modifier
                                        .width(this@BoxWithConstraints.maxWidth / 7),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = rememberImagePainter(
                                            builder = {
                                                crossfade(true)
                                            }, data = it.icon
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