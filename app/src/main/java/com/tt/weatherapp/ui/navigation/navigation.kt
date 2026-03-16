package com.tt.weatherapp.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.tt.weatherapp.ui.MainViewModel
import com.tt.weatherapp.ui.home.Home

sealed class BottomNav(val route: String) {
    object HomeNav : BottomNav("home_route")
}

sealed class HomeRoute(val route: String) {
    object Home : HomeRoute("home")
}

fun NavGraphBuilder.homeGraph(
    drawerState: DrawerState,
    viewModel: MainViewModel,
    showSetting: () -> Unit,
    refresh: (Boolean) -> Unit,
) {
    navigation(startDestination = HomeRoute.Home.route, route = BottomNav.HomeNav.route) {
        composable(HomeRoute.Home.route) {
            Home(
                drawerState = drawerState,
                viewModel = viewModel,
                showSetting = showSetting,
                refresh = refresh
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun BuildAppNavHost(
    navController: NavHostController,
    padding: PaddingValues,
    drawerState: DrawerState,
    viewModel: MainViewModel,
    refresh: (Boolean) -> Unit,
    showSetting: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = BottomNav.HomeNav.route,
        modifier = Modifier.padding(padding)
    ) {
        homeGraph(
            drawerState = drawerState,
            viewModel = viewModel,
            showSetting = { showSetting.invoke() },
            refresh = { refresh.invoke(it) }
        )
    }
}
