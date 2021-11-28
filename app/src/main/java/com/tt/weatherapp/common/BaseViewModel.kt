package com.tt.weatherapp.common

import androidx.lifecycle.AndroidViewModel
import com.tt.weatherapp.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel : AndroidViewModel(App.instance) {

    protected var TAG = this.javaClass.simpleName
    protected val mApplication by lazy { getApplication<App>() }

    private val _apiState = MutableStateFlow<ViewStatus?>(null)
    val apiState = _apiState.asStateFlow()

    fun onClickBackHandler() {
        sendViewStatus(ViewStatus.ClickBack)
    }

    fun sendViewStatus(viewStatus: ViewStatus?) {
        _apiState.value = viewStatus
    }

    fun showLoading(isLoading: Boolean) {
        sendViewStatus(ViewStatus.ShowLoading(isLoading))
    }
}