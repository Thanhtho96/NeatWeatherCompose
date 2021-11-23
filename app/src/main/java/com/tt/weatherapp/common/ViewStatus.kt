package com.tt.weatherapp.common

sealed class ViewStatus {
    data class ShowLoading(val isShow: Boolean) : ViewStatus()

    data class Error(val errorMessage: String?) : ViewStatus()

    object ClickBack : ViewStatus()
}
