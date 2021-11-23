package com.tt.weatherapp.common.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class NetworkEvent(private val scope: CoroutineScope) {

    private val _networkState = MutableSharedFlow<NetworkError>()
    val networkState = _networkState.asSharedFlow()

    fun publish(networkState: NetworkError) {
        scope.launch {
            _networkState.emit(networkState)
        }
    }
}