package com.tt.weatherapp.ui

import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.*
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.BottomNavigation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.tt.weatherapp.R
import com.tt.weatherapp.common.BaseActivity
import com.tt.weatherapp.ui.daily.Daily
import com.tt.weatherapp.ui.home.Home
import com.tt.weatherapp.ui.hourly.Hourly
import com.tt.weatherapp.utils.Utils
import org.koin.androidx.viewmodel.ext.android.getViewModel

@ExperimentalPermissionsApi
@ExperimentalFoundationApi
class MainActivity : BaseActivity<MainViewModel>() {
    override fun viewModelClass() = getViewModel<MainViewModel>()
    private val startSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    @Composable
    override fun InitView() {
        MainView(viewModel)

        FeatureThatRequiresLocationPermission(
            {
                Utils.openSettings(this, startSettingsLauncher)
            }, {
                // Todo Call API
            })
    }
}

@ExperimentalFoundationApi
@Composable
fun MainView(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNav.HomeNav,
        BottomNav.HourlyNav,
        BottomNav.DailyNav
    )

    Scaffold(
        bottomBar = {
            BottomNavigation(
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.navigationBars
                )
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    BottomNavigationItem(
                        icon = { Icon(painterResource(screen.icon), contentDescription = null) },
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
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
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
                homeGraph(navController, viewModel)
                composable(BottomNav.HourlyNav.route) { Hourly(viewModel) }
                composable(BottomNav.DailyNav.route) { Daily(viewModel) }
            }
        }
    }
}

@ExperimentalFoundationApi
fun NavGraphBuilder.homeGraph(navController: NavController, viewModel: MainViewModel) {
    navigation(startDestination = HomeRoute.Home.route, route = BottomNav.HomeNav.route) {
        composable(HomeRoute.Home.route) { Home(navController, viewModel) }
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
    navigateToSettingsScreen: () -> Unit,
    hasPermission: () -> Unit
) {
    val res = LocalContext.current.resources
    // Track if the user doesn't want to see the rationale any more.
    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }

    // Camera permission state
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    when {
        // If the location permission is granted, then show screen with the feature enabled
        cameraPermissionState.hasPermission -> {
            hasPermission()
        }
        // If the user denied the permission but a rationale should be shown, or the user sees
        // the permission for the first time, explain why the feature is needed by the app and allow
        // the user to be presented with the permission again or to not see the rationale any more.
        cameraPermissionState.shouldShowRationale ||
                !cameraPermissionState.permissionRequested -> {
            if (!doNotShowRationale) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text(text = res.getString(R.string.need_location_permission)) },
                    confirmButton = {
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text(res.getString(R.string.ok))
                        }
                    },
                    dismissButton = {
                        Button(onClick = { doNotShowRationale = true }) {
                            Text(res.getString(R.string.cancel))
                        }
                    })
            }
        }
        // If the criteria above hasn't been met, the user denied the permission. Let's present
        // the user with a FAQ in case they want to know more and send them to the Settings screen
        // to enable it the future there if they want to.
        else -> {
            var isShowDialog by remember { mutableStateOf(true) }
            if (isShowDialog.not()) return

            AlertDialog(
                onDismissRequest = { },
                title = { Text(text = res.getString(R.string.location_permission_is_deny)) },
                confirmButton = {
                    Button(onClick = { navigateToSettingsScreen() }) {
                        Text(res.getString(R.string.txt_open_app_settings))
                    }
                },
                dismissButton = {
                    Button(onClick = { isShowDialog = false }) {
                        Text(res.getString(R.string.cancel))
                    }
                })
        }
    }
}