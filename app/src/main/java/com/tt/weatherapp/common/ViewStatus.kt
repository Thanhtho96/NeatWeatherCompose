package com.tt.weatherapp.common

sealed class ViewStatus {
    data class Error(val errorMessage: String?) : ViewStatus()

    object ClickBack : ViewStatus()

    data class ShowLoading(val isLoading: Boolean) : ViewStatus()

    data class Toast(val message: String) : ViewStatus()
}