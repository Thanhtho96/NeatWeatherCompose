package com.tt.weatherapp.ui.home

import android.content.res.Resources
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.flowlayout.FlowRow
import com.tt.weatherapp.R
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.model.*
import com.tt.weatherapp.ui.MainViewModel
import com.tt.weatherapp.utils.DateUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@ExperimentalFoundationApi
@Composable
fun Home(
    scaffoldState: ScaffoldState,
    viewModel: MainViewModel,
    showSetting: () -> Unit,
    refresh: (Boolean) -> Unit,
    navigateHourly: () -> Unit,
    navigateDaily: () -> Unit,
    navigateAddPlace: () -> Unit
) {
    val res = LocalContext.current.resources
    val locationState = viewModel.locationData ?: return
    val weatherData = locationState.weatherData ?: return
    val current = weatherData.current
    val daily = weatherData.daily
    val homeWeatherUnit = HomeWeatherUnit(weatherData)
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            scaffoldState = scaffoldState,
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
                        scaffoldState
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = dimensionResource(id = R.dimen.actionBarSize))
                            .padding(horizontal = 17.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.End)
                    ) {
                        IconButton(modifier = Modifier.size(24.dp),
                            onClick = { refresh.invoke(true) }) {
                            Icon(
                                Icons.Default.Refresh,
                                null
                            )
                        }
                        IconButton(
                            modifier = Modifier.size(24.dp),
                            onClick = {
                                scope.launch {
                                    scaffoldState.drawerState.apply {
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

                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        MainInformation(
                            res,
                            current,
                            homeWeatherUnit,
                            daily,
                            locationState,
                            viewModel
                        )
                        Hourly(navigateHourly, res, viewModel, homeWeatherUnit)
                        Daily(navigateDaily, res, viewModel)
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}

@Composable
private fun MainInformation(
    res: Resources,
    current: Current,
    homeWeatherUnit: HomeWeatherUnit,
    daily: List<Daily>,
    locationState: Location,
    viewModel: MainViewModel
) {
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
                fontSize = 37.sp,
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

        Text(text = locationState.name, fontSize = 20.sp)

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
                R.string.txt_description_meter,
                current.weather[0].description.capitalize(Locale.current),
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
            val boxDimen = ((maxWidth.value - 12 * 2) / 3).dp - 1.dp

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                FlowRow(
                    mainAxisSpacing = 12.dp,
                    crossAxisSpacing = 12.dp
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
}

@Composable
private fun Hourly(
    navigateHourly: () -> Unit,
    res: Resources,
    viewModel: MainViewModel,
    homeWeatherUnit: HomeWeatherUnit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable { navigateHourly.invoke() }) {
        Column {
            Row(
                Modifier.padding(
                    top = 10.dp,
                    bottom = 7.dp,
                    start = 12.dp,
                    end = 12.dp
                )
            ) {
                Text(
                    modifier = Modifier.weight(1F),
                    text = res.getString(R.string.txt_hourly),
                    fontSize = 19.sp
                )
                Image(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null
                )
            }

            LazyRow(
                contentPadding = PaddingValues(
                    horizontal = 12.dp
                ), horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                items(viewModel.hourly.take(25)) { hourly ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    ) {
                        Text(
                            text = stringResource(
                                homeWeatherUnit.onlyDegreeSymbol,
                                hourly.temp.roundToInt().toString(),
                            ),
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        val rain =
                            if (hourly.pop != null && hourly.pop > 0) {
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
                                    data = Constant.getWeatherIcon(
                                        hourly.weather[0].icon
                                    )
                                )
                                    .apply(block = fun ImageRequest.Builder.() {
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
                            ).replace(":00", "h"),
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Daily(
    navigateDaily: () -> Unit,
    res: Resources,
    viewModel: MainViewModel
) {
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .clickable {
                navigateDaily.invoke()
            }
    ) {
        val widthCol = (maxWidth - 12.dp * 2) / 7

        Column(Modifier.padding(vertical = 15.dp, horizontal = 12.dp)) {
            Row {
                Text(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 7.dp)
                        .weight(1F),
                    text = res.getString(R.string.txt_daily),
                    fontSize = 19.sp
                )
                Image(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null
                )
            }

            Row {
                viewModel.listDailyTempInfo.forEach {
                    Column(
                        modifier = Modifier
                            .width(widthCol),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(Dp(it.topMargin)))
                        if (it.maxTemp == it.minTemp) {
                            Text(text = it.maxTemp)
                        } else {
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
            }
            Row {
                viewModel.listDailyTempInfo.forEach {
                    Column(
                        modifier = Modifier
                            .width(widthCol),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(
                                    LocalContext.current
                                ).data(
                                    data = it.icon
                                )
                                    .apply(block = fun ImageRequest.Builder.() {
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

@ExperimentalFoundationApi
@Composable
private fun DrawerContent(
    navigateAddPlace: () -> Unit,
    showSetting: () -> Unit,
    refresh: (Boolean) -> Unit,
    viewModel: MainViewModel,
    res: Resources,
    homeWeatherUnit: HomeWeatherUnit,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState
) {
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1F),
            contentPadding = WindowInsets.statusBars.asPaddingValues()
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(
                viewModel.listLocation,
                key = { it.lat + it.lon + it.type.ordinal }) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()
                        .clickable {
                            scope.launch {
                                viewModel.changeDisplayLocation(it)

                                if (it.isDisplay) return@launch

                                scaffoldState.drawerState.close()
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
                                            DateUtil.DateFormat.HOUR_MINUTE,
                                            it.weatherData.current.dt * 1000
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
                                fontSize = 17.sp,
                                overflow = TextOverflow.Ellipsis,
                                text = it.name
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
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(LocalContext.current)
                                            .data(
                                                data = Constant.getWeatherIcon(it.weatherData.current.weather[0].icon)
                                            )
                                            .apply(block = fun ImageRequest.Builder.() {
                                                crossfade(true)
                                            }).build()
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(37.dp)
                                )
                                Text(text = it.weatherData.current.weather[0].main)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val temp = (
                                    it.weatherData?.current?.temp?.roundToInt()
                                        ?: res.getString(R.string.null_face)
                                    ).toString()

                            Text(
                                text = res.getString(
                                    homeWeatherUnit.currentTemp,
                                    temp
                                ),
                                fontSize = 47.sp,
                            )
                            if (it.type != LocationType.GPS) {
                                IconButton(modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.CenterVertically),
                                    onClick = { viewModel.deleteLocation(it) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = colorResource(id = R.color.yellow)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(13.dp))
                    }
                    Divider()
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
            horizontalArrangement = Arrangement.spacedBy(27.dp, Alignment.End)
        ) {
            IconButton(modifier = Modifier.size(24.dp),
                onClick = { navigateAddPlace.invoke() }) {
                Icon(
                    Icons.Default.Add,
                    null
                )
            }
            IconButton(modifier = Modifier.size(24.dp),
                onClick = { showSetting.invoke() }) {
                Icon(
                    Icons.Default.Settings,
                    null
                )
            }
        }
    }
}
