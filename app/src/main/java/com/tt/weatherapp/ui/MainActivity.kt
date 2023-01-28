package com.tt.weatherapp.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.*
import com.tt.weatherapp.R
import com.tt.weatherapp.common.BaseActivity
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.model.LocationSuggestion
import com.tt.weatherapp.service.LocationService
import com.tt.weatherapp.service.WeatherState
import com.tt.weatherapp.ui.navigation.BuildAppNavHost
import com.tt.weatherapp.ui.navigation.HomeRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.androidx.viewmodel.ext.android.getViewModel

@ExperimentalPermissionsApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
class MainActivity : BaseActivity<MainViewModel>() {
    private var mService: LocationService? = null
    override fun viewModelClass() = getViewModel<MainViewModel>()
    private var connection: ServiceConnection? = null

    private suspend fun bindWeatherService() = suspendCancellableCoroutine {
        connection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                val binder = service as LocationService.LocalBinder
                mService = binder.getService()
                mService?.weatherState = object : WeatherState {
                    override fun onComplete() {
                        viewModel.setIsForceRefresh(isRefresh = false, isForce = true)
                    }

                    override fun onLoading(isLoading: Boolean) {
                        viewModel.setRefresh(isLoading)
                    }
                }
                it.resumeWith(Result.success(Unit))
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                mService = null
            }
        }

        Intent(this, LocationService::class.java).also { intent ->
            bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
        }
    }

    @Composable
    override fun InitView() {
        val lifecycleOwner = LocalLifecycleOwner.current
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberScaffoldState()
        val navController = rememberNavController()
        val modalBottomSheetState =
            rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val selectUnit = viewModel.selectUnit
        val pullRefreshState =
            rememberPullRefreshState(
                refreshing = viewModel.isRefreshing,
                onRefresh = { forceRefreshWeather() },
                refreshingOffset = PullRefreshDefaults.RefreshingOffset * 2
            )

        BackHandler(scaffoldState.drawerState.isOpen || modalBottomSheetState.isVisible) {
            scope.launch {
                if (modalBottomSheetState.isVisible) {
                    modalBottomSheetState.hide()
                    return@launch
                }
                scaffoldState.drawerState.apply {
                    if (isOpen) close()
                }
            }
        }

        Box(Modifier.pullRefresh(pullRefreshState)) {
            MainView(
                scaffoldState,
                selectUnit,
                viewModel,
                scope,
                modalBottomSheetState,
                navController,
                refresh = { forceRefreshWeather(it) },
                onClickSuggestion = { selectSuggestLocation(it) },
                toggleUnit = { toggleUnit(it) }
            )

            if (scaffoldState.drawerState.isClosed && currentRoute != HomeRoute.Search.route) {
                PullRefreshIndicator(
                    refreshing = viewModel.isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    scale = true
                )
            }
        }

        FeatureThatRequiresLocationPermission {
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.setIsForceRefresh(isRefresh = true, isForce = false)
                    }
                    if (event == Lifecycle.Event.ON_STOP) {
                        connection?.let { unbindService(it) }
                    }
                }

                // Add the observer to the lifecycle
                lifecycleOwner.lifecycle.addObserver(observer)

                scope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        bindWeatherService()
                        viewModel.isForceRefresh.filter { it.isRefresh }.collect {
                            forceRefreshWeather(it.isForce)
                        }
                    }
                }

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    mService = null
                }
            }
        }
    }

    private fun forceRefreshWeather(isForceRefresh: Boolean = true) {
        viewModel.setRefresh(isForceRefresh)
        mService?.getWeatherData(isForceRefresh)
    }

    private fun selectSuggestLocation(locationSuggestion: LocationSuggestion) {
        mService?.chooseSuggestLocation(locationSuggestion)
    }

    private fun toggleUnit(@StringRes unitId: Int) {
        mService?.toggleUnit(unitId)
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun MainView(
    scaffoldState: ScaffoldState,
    selectUnit: Constant.Unit,
    viewModel: MainViewModel,
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState,
    navController: NavHostController,
    refresh: (Boolean) -> Unit,
    onClickSuggestion: (LocationSuggestion) -> Unit,
    toggleUnit: (Int) -> Unit
) {
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
                    val selectString = when (selectUnit) {
                        Constant.Unit.METRIC -> R.string.txt_metric
                        Constant.Unit.IMPERIAL -> R.string.txt_imperial
                    }
                    listOf(R.string.txt_metric, R.string.txt_imperial).forEach {
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight()
                                .background(color = colorResource(id = if (it == selectString) R.color.yellow else R.color.transparent))
                                .clickable {
                                    scope.launch {
                                        modalBottomSheetState.hide()
                                        delay(50)
                                        toggleUnit.invoke(it)
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
        Scaffold(
            content = { padding ->
                BuildAppNavHost(
                    navController = navController,
                    padding = padding,
                    scaffoldState = scaffoldState,
                    viewModel = viewModel,
                    scope = scope,
                    modalBottomSheetState = modalBottomSheetState,
                    refresh = refresh,
                    onClickSuggestion = onClickSuggestion
                )
            }
        )
    }
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
                }
            )
        }
    }
}
