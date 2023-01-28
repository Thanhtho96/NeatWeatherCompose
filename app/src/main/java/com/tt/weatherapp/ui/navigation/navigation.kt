package com.tt.weatherapp.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.tt.weatherapp.model.LocationSuggestion
import com.tt.weatherapp.ui.MainViewModel
import com.tt.weatherapp.ui.daily.Daily
import com.tt.weatherapp.ui.home.Home
import com.tt.weatherapp.ui.home.SearchPlace
import com.tt.weatherapp.ui.hourly.Hourly
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class BottomNav(val route: String) {
    object HomeNav : BottomNav("home_route")
    object HourlyNav : BottomNav("hourly_route")
    object DailyNav : BottomNav("daily_route")
}

sealed class HomeRoute(val route: String) {
    object Home : HomeRoute("home")
    object Search : HomeRoute("search")
}

@ExperimentalFoundationApi
fun NavGraphBuilder.homeGraph(
    scaffoldState: ScaffoldState,
    navController: NavController,
    viewModel: MainViewModel,
    showSetting: () -> Unit,
    refresh: (Boolean) -> Unit,
    onClickSuggestion: (LocationSuggestion) -> Unit
) {
    navigation(startDestination = HomeRoute.Home.route, route = BottomNav.HomeNav.route) {
        composable(HomeRoute.Home.route) {
            Home(
                scaffoldState = scaffoldState,
                viewModel = viewModel,
                showSetting = showSetting,
                refresh = refresh,
                navigateHourly = { navController.navigate(BottomNav.HourlyNav.route) },
                navigateDaily = { navController.navigate(BottomNav.DailyNav.route) },
                navigateAddPlace = { navController.navigate(HomeRoute.Search.route) }
            )
        }
        composable(HomeRoute.Search.route) {
            SearchPlace(
                navController = navController,
                viewModel = viewModel,
                onClickSuggestion = { locationSuggestion ->
                    onClickSuggestion(locationSuggestion)
                }
            )
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun BuildAppNavHost(
    navController: NavHostController,
    padding: PaddingValues,
    scaffoldState: ScaffoldState,
    viewModel: MainViewModel,
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState,
    refresh: (Boolean) -> Unit,
    onClickSuggestion: (LocationSuggestion) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = BottomNav.HomeNav.route,
        modifier = Modifier.padding(padding)
    ) {
        homeGraph(
            scaffoldState = scaffoldState,
            navController = navController,
            viewModel = viewModel,
            showSetting = { scope.launch { modalBottomSheetState.show() } },
            refresh = { refresh.invoke(it) },
            onClickSuggestion = { onClickSuggestion.invoke(it) }
        )
        composable(BottomNav.HourlyNav.route) {
            Hourly(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(BottomNav.DailyNav.route) {
            Daily(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}
