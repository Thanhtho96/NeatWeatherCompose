package com.tt.weatherapp.common

import androidx.lifecycle.AndroidViewModel
import com.tt.weatherapp.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel : AndroidViewModel(App.instance) {

    protected var TAG = this.javaClass.simpleName
    protected val mApplication by lazy { getApplication<App>() }

    private val _uiState = MutableStateFlow<ViewStatus?>(null)
    val uiState = _uiState.asStateFlow()

    fun onClickBackHandler() {
        sendViewStatus(ViewStatus.ClickBack)
    }

    fun sendViewStatus(viewStatus: ViewStatus?) {
        _uiState.value = viewStatus
    }

    protected fun showLoadingIndicator(isShow: Boolean) {
        _uiState.value = ViewStatus.ShowLoading(isShow)
    }
}