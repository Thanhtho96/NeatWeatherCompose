package com.tt.weatherapp.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.tt.weatherapp.R
import com.tt.weatherapp.common.BaseActivity
import com.tt.weatherapp.ui.home.Home
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : BaseActivity<MainViewModel>() {
    override fun viewModelClass() = getViewModel<MainViewModel>()

    @Composable
    override fun InitView() {
        MainView(viewModel)
    }
}

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
        NavHost(navController, startDestination = BottomNav.HomeNav.route) {
            homeGraph(navController)
            composable(BottomNav.HourlyNav.route) { Hourly() }
            composable(BottomNav.DailyNav.route) { Daily() }
        }
    }
}

fun NavGraphBuilder.homeGraph(navController: NavController) {
    navigation(startDestination = HomeRoute.Home.route, route = BottomNav.HomeNav.route) {
        composable(HomeRoute.Home.route) { Home(navController) }
        composable(HomeRoute.Search.route) { }
        composable(HomeRoute.Settings.route) { }
    }
}


@Composable
fun Hourly() {
    /*...*/
    Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "Hourly", modifier = Modifier.align(Alignment.Center))
    }
    /*...*/
}

@Composable
fun Daily() {
    /*...*/
    Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "Daily", modifier = Modifier.align(Alignment.Center))
    }
    /*...*/
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