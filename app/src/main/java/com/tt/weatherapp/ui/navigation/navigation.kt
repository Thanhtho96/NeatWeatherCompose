package com.tt.weatherapp.ui.navigation

sealed class BottomNav(val route: String) {
    object HomeNav : BottomNav("home_route")
    object HourlyNav : BottomNav("hourly_route")
    object DailyNav : BottomNav("daily_route")
}

sealed class HomeRoute(val route: String) {
    object Home : HomeRoute("home")
    object Search : HomeRoute("search")
    object Settings : HomeRoute("settings")
}