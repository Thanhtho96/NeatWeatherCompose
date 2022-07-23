package com.tt.weatherapp.navigation

sealed class BottomNav(val route: String) {
    object HomeNav : BottomNav("home_route")
    object HourlyNav : BottomNav("hourly")
    object DailyNav : BottomNav("daily")
}

sealed class HomeRoute(val route: String) {
    object Home : HomeRoute("home")
    object Search : HomeRoute("search")
    object Settings : HomeRoute("settings")
}