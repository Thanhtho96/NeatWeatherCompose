package com.tt.weatherapp.common

sealed class ViewStatus {
    data class Error(val errorMessage: String?) : ViewStatus()

    object ClickBack : ViewStatus()

    data class ShowLoading(val isLoading: Boolean) : ViewStatus()

    data class Toast(val message: String) : ViewStatus()
}

sealed class Resource<out T> {
    data class Success<out T>(val value: T) : Resource<T>()
    class Loading(val isLoading: Boolean) : Resource<Nothing>()
}