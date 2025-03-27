package com.tt.weatherapp.ui.home

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.google.accompanist.flowlayout.FlowRow
import com.tt.weatherapp.R
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.model.HomeWeatherUnit
import com.tt.weatherapp.model.Location
import com.tt.weatherapp.model.LocationType
import com.tt.weatherapp.model.WeatherData
import com.tt.weatherapp.ui.MainViewModel
import com.tt.weatherapp.utils.DateUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun Home(
    drawerState: DrawerState,
    viewModel: MainViewModel,
    showSetting: () -> Unit,
    refresh: (Boolean) -> Unit,
    navigateAddPlace: () -> Unit
) {
    val res = LocalContext.current.resources
    val locationState = viewModel.locationData ?: return
    val weatherData = locationState.weatherData ?: return
    val homeWeatherUnit = HomeWeatherUnit(weatherData)
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            scrimColor = MaterialTheme.colorScheme.background,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    DrawerContent(
                        navigateAddPlace,
                        showSetting,
                        refresh,
                        viewModel,
                        res,
                        homeWeatherUnit,
                        scope,
                        drawerState
                    )
                }
            },
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold {
                    // Screen content
                    Box(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = dimensionResource(id = R.dimen.actionBarSize))
                                .padding(horizontal = 17.dp)
                                .zIndex(7F),
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)
                        ) {
                            IconButton(
                                modifier = Modifier.size(46.dp),
                                onClick = { refresh.invoke(true) }) {
                                Icon(
                                    Icons.Default.Refresh,
                                    null
                                )
                            }
                            IconButton(
                                modifier = Modifier.size(46.dp),
                                onClick = {
                                    scope.launch {
                                        drawerState.apply {
                                            if (isClosed) open() else close()
                                        }
                                    }
                                }) {
                                Icon(
                                    Icons.Default.Menu,
                                    null
                                )
                            }
                        }

                        Column(
                            Modifier
                                .align(Alignment.TopStart)
                                .verticalScroll(rememberScrollState())
                        ) {
                            MainInformation(
                                res,
                                weatherData,
                                homeWeatherUnit,
                                locationState,
                                viewModel
                            )
                            Spacer(modifier = Modifier.navigationBarsPadding())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainInformation(
    res: Resources,
    weatherData: WeatherData,
    homeWeatherUnit: HomeWeatherUnit,
    locationState: Location,
    viewModel: MainViewModel
) {
    Column(modifier = Modifier.padding(horizontal = 17.dp)) {
        Text(
            text = res.getString(
                R.string.updated_at, DateUtil.format(
                    weatherData.dt,
                    weatherData.timezone,
                    DateUtil.DateFormat.HOUR_MINUTE
                )
            ),
            fontSize = 14.sp
        )

        Row {
            Text(
                text = res.getString(
                    homeWeatherUnit.currentTemp,
                    weatherData.main.temp.roundToInt().toString()
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 50.sp,
                modifier = Modifier.padding(end = 7.dp)
            )
            Text(
                text = res.getString(
                    homeWeatherUnit.highLowTemp,
                    weatherData.main.tempMin.roundToInt().toString(),
                    weatherData.main.tempMax.roundToInt().toString()
                )
            )
        }

        Text(text = locationState.searchName, fontSize = 27.sp)

        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = Constant.getWeatherIcon(weatherData.weather[0].icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(text = weatherData.weather[0].main, fontSize = 21.sp)
        }

        Text(
            text = res.getString(
                R.string.txt_description_meter,
                weatherData.weather[0].description.capitalize(Locale.current),
                res.getQuantityString(
                    homeWeatherUnit.windDescription,
                    weatherData.wind.speed.roundToInt(),
                    weatherData.wind.speed.roundToInt(),
                    res.getStringArray(R.array.compass_directions)[((weatherData.wind.deg % 360) / 22.5).roundToInt()],
                )
            ),
            fontSize = 21.sp
        )

        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = res.getString(R.string.txt_current_weather),
            fontSize = 23.sp
        )

        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            val spaceBetween = 17
            val itemPerRow = when (LocalConfiguration.current.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> 4
                else -> 2
            }
            val boxDimen =
                ((maxWidth.value - spaceBetween * (itemPerRow - 1)) / itemPerRow).dp - 1.dp // sometime it just need little space

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                FlowRow(
                    mainAxisSpacing = spaceBetween.dp,
                    crossAxisSpacing = spaceBetween.dp
                ) {
                    viewModel.listCurrentWeatherData.forEach { info ->
                        Column(
                            modifier = Modifier
                                .size(boxDimen)
                                .background(
                                    colorResource(id = R.color.home_grid),
                                    RoundedCornerShape(9.dp)
                                )
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Image(
                                modifier = Modifier.size(34.dp),
                                painter = painterResource(id = info.icon),
                                contentDescription = null
                            )
                            Text(text = res.getString(info.name), fontSize = 26.sp)
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
}

@Composable
private fun DrawerContent(
    navigateAddPlace: () -> Unit,
    showSetting: () -> Unit,
    refresh: (Boolean) -> Unit,
    viewModel: MainViewModel,
    res: Resources,
    homeWeatherUnit: HomeWeatherUnit,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1F),
        ) {
            items(
                viewModel.listLocation,
                key = { it.lat + it.lon + it.type.ordinal }) {
                Column(
                    Modifier
                        .animateItem()
                        .clickable {
                            scope.launch {
                                viewModel.changeDisplayLocation(it)

                                if (it.isDisplay) return@launch

                                drawerState.close()
                                refresh.invoke(false)
                            }
                        }
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 17.dp)
                    ) {
                        Spacer(modifier = Modifier.height(9.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (it.isDisplay) {
                                Canvas(
                                    modifier = Modifier.size(10.dp),
                                    onDraw = {
                                        drawCircle(
                                            color = Color.Green,
                                            alpha = 0.7f
                                        )
                                    }
                                )
                            }

                            Text(
                                modifier = Modifier.padding(start = if (it.isDisplay) 5.dp else 0.dp),
                                text = res.getString(
                                    R.string.updated_at,
                                    if (it.weatherData != null) {
                                        DateUtil.format(
                                            it.weatherData.dt,
                                            it.weatherData.timezone,
                                            DateUtil.DateFormat.HOUR_MINUTE
                                        )
                                    } else {
                                        stringResource(id = R.string.null_face)
                                    }
                                ),
                                fontSize = 14.sp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(1F),
                                fontSize = 27.sp,
                                overflow = TextOverflow.Ellipsis,
                                text = it.searchName
                            )
                            if (it.type == LocationType.GPS) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    null,
                                    Modifier.size(24.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.size(24.dp))
                            }
                        }

                        if (it.weatherData == null) {
                            Spacer(modifier = Modifier.height(7.dp))
                            Text(
                                modifier = Modifier.size(37.dp),
                                text = res.getString(R.string.null_face),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = Constant.getWeatherIcon(it.weatherData.weather[0].icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(text = it.weatherData.weather[0].main, fontSize = 21.sp)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val temp = (
                                    it.weatherData?.main?.temp?.roundToInt()
                                        ?: res.getString(R.string.null_face)
                                    ).toString()

                            Text(
                                text = res.getString(
                                    homeWeatherUnit.currentTemp,
                                    temp
                                ),
                                fontSize = 50.sp,
                            )
                            if (it.type != LocationType.GPS) {
                                IconButton(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .align(Alignment.CenterVertically),
                                    onClick = { viewModel.deleteLocation(it) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        Modifier.size(24.dp),
                                        tint = colorResource(id = R.color.yellow)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(13.dp))
                    }
                    HorizontalDivider()
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = dimensionResource(id = R.dimen.actionBarSize))
                .padding(end = 27.dp, top = 20.dp, bottom = 20.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)
        ) {
            IconButton(
                modifier = Modifier.size(46.dp),
                onClick = { navigateAddPlace.invoke() }) {
                Icon(
                    Icons.Default.Add,
                    null,
                    Modifier.size(24.dp)
                )
            }
            IconButton(
                modifier = Modifier.size(46.dp),
                onClick = { showSetting.invoke() }) {
                Icon(
                    Icons.Default.Settings,
                    null,
                    Modifier.size(24.dp)
                )
            }
        }
    }
}
