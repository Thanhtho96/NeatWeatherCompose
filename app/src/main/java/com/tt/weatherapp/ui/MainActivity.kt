package com.tt.weatherapp.ui

import android.app.AlarmManager
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.*
import com.google.accompanist.insets.ui.BottomNavigation
import com.google.accompanist.permissions.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.android.gms.location.*
import com.tt.weatherapp.R
import com.tt.weatherapp.common.BaseActivity
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.data.local.SharedPrefHelper
import com.tt.weatherapp.ui.daily.Daily
import com.tt.weatherapp.ui.home.Home
import com.tt.weatherapp.ui.hourly.Hourly
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

@ExperimentalPermissionsApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
class MainActivity : BaseActivity<MainViewModel>() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun viewModelClass() = getViewModel<MainViewModel>()

    @Composable
    override fun InitView() {
        SwipeRefresh(
            state = rememberSwipeRefreshState(viewModel.isRefreshing),
            onRefresh = {
                forceRefreshWeather()
            },
        ) {
            MainView(viewModel) {
                forceRefreshWeather()
            }
        }

        FeatureThatRequiresLocationPermission {
            DisposableEffect(LocalLifecycleOwner.current) {
                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                initLocationCallBack()
                createLocationRequest()

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location -> // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            viewModel.getWeatherInfo(location.latitude, location.longitude)
                        } else {
                            startLocationService()
                        }
                    }

                onDispose {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }

            LaunchedEffect(true) {
                while (true) {
                    delay(AlarmManager.INTERVAL_FIFTEEN_MINUTES)
                    forceRefreshWeather()
                }
            }
        }
    }

    private fun forceRefreshWeather() {
        viewModel.refresh()
        startLocationService()
    }

    private fun initLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach {
                    viewModel.getWeatherInfo(it.latitude, it.longitude)
                    return@forEach
                }

                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 1000000
            fastestInterval = 500000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun startLocationService() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun MainView(viewModel: MainViewModel, refresh: () -> Unit) {
    val sharedPrefHelper by inject<SharedPrefHelper>()
    val navController = rememberNavController()
    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetContent = {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(top = 20.dp, bottom = 40.dp, start = 25.dp, end = 25.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.txt_unit),
                    modifier = Modifier.padding(bottom = 7.dp)
                )

                Row(
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            color = colorResource(id = R.color.yellow),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .height(48.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val selectString = when (sharedPrefHelper.getChosenUnit()) {
                        Constant.Unit.METRIC -> R.string.txt_metric
                        Constant.Unit.IMPERIAL -> R.string.txt_fahrenheit
                    }
                    listOf(R.string.txt_metric, R.string.txt_fahrenheit).forEach {
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight()
                                .background(color = colorResource(id = if (it == selectString) R.color.yellow else R.color.transparent))
                                .clickable {
                                    scope.launch {
                                        val isChangeUnit = selectString != it
                                        if (isChangeUnit) {
                                            val unit = when (it) {
                                                R.string.txt_metric -> Constant.Unit.METRIC
                                                else -> Constant.Unit.IMPERIAL
                                            }
                                            sharedPrefHelper.setChosenUnit(unit)
                                            viewModel.getWeatherInfo(null, null, true)
                                        }
                                        modalBottomSheetState.hide()
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(id = it),
                                color = colorResource(id = if (it == selectString) R.color.background else R.color.text_unit_unselect)
                            )
                        }
                    }
                }
            }
        },
        sheetState = modalBottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        val items = listOf(
            BottomNav.HomeNav,
            BottomNav.HourlyNav,
            BottomNav.DailyNav
        )

        Scaffold(
            bottomBar = {
                BottomNavigation(
                    contentPadding = WindowInsets.navigationBars.asPaddingValues()
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        BottomNavigationItem(
                            icon = {
                                Icon(
                                    painterResource(screen.icon),
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(id = screen.resourceId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // re-selecting the same item
                                    launchSingleTop = true
                                    // Restore state when re-selecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) {
            Box(Modifier.padding(it)) {
                NavHost(navController, startDestination = BottomNav.HomeNav.route) {
                    homeGraph(
                        viewModel,
                        { scope.launch { modalBottomSheetState.show() } }
                    ) { refresh.invoke() }
                    composable(BottomNav.HourlyNav.route) { Hourly(viewModel) }
                    composable(BottomNav.DailyNav.route) { Daily(viewModel) }
                }
            }
        }
    }
}

fun NavGraphBuilder.homeGraph(
    viewModel: MainViewModel,
    showSetting: () -> Unit,
    refresh: () -> Unit
) {
    navigation(startDestination = HomeRoute.Home.route, route = BottomNav.HomeNav.route) {
        composable(HomeRoute.Home.route) { Home(viewModel, showSetting, refresh) }
        composable(HomeRoute.Search.route) { }
        composable(HomeRoute.Settings.route) { }
    }
}

sealed class BottomNav(
    val route: String,
    @StringRes val resourceId: Int,
    @DrawableRes val icon: Int
) {
    object HomeNav : BottomNav("home_route", R.string.home, R.drawable.ic_home)
    object HourlyNav : BottomNav("hourly", R.string.hourly, R.drawable.ic_clock)
    object DailyNav : BottomNav("daily", R.string.daily, R.drawable.ic_calendar)
}

sealed class HomeRoute(val route: String) {
    object Home : HomeRoute("home")
    object Search : HomeRoute("search")
    object Settings : HomeRoute("settings")
}

@ExperimentalPermissionsApi
@Composable
private fun FeatureThatRequiresLocationPermission(
    hasPermission: @Composable () -> Unit
) {
    val res = LocalContext.current.resources

    // Location permission state
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val fineLocationPermissionState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    when (locationPermissionState.status) {
        // If the location permission is granted, then show screen with the feature enabled
        PermissionStatus.Granted -> {
            hasPermission()
        }
        // If the user denied the permission but a rationale should be shown, or the user sees
        // the permission for the first time, explain why the feature is needed by the app and allow
        // the user to be presented with the permission again or to not see the rationale any more.
        is PermissionStatus.Denied -> {
            val permissionTitle = if (locationPermissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                res.getString(R.string.need_location_permission)
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                res.getString(R.string.need_location_permission)
            }

            AlertDialog(
                onDismissRequest = { },
                title = { Text(text = permissionTitle) },
                confirmButton = {
                    Button(onClick = { fineLocationPermissionState.launchMultiplePermissionRequest() }) {
                        Text(res.getString(R.string.ok))
                    }
                },
                dismissButton = {
                    Button(onClick = { }) {
                        Text(res.getString(R.string.cancel))
                    }
                })
        }
    }
}
