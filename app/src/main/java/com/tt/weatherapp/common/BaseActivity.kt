package com.tt.weatherapp.common

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tt.weatherapp.common.network.NetworkEvent
import com.tt.weatherapp.common.network.NetworkState
import com.tt.weatherapp.ui.theme.NeatWeatherComposeTheme
import com.tt.weatherapp.utils.CustomOkDialog
import com.tt.weatherapp.utils.LoadingDialog
import com.tt.weatherapp.utils.StatusBarUtil
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

abstract class BaseActivity<M : BaseViewModel> : ComponentActivity() {
    protected val TAG: String = this.javaClass.simpleName

    protected lateinit var viewModel: M
    private val networkEvent by inject<NetworkEvent>()
    var isNavigating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StatusBarUtil.setTransparentStatusBar(this)

        viewModel = viewModelClass()

        setContent {
            val messageError = remember { mutableStateOf<String?>(null) }
            val isShowLoading = remember { mutableStateOf(false) }

            val systemUiController = rememberSystemUiController()
            val useDarkIcons = isSystemInDarkTheme().not()

            SideEffect {
                // Update all of the system bar colors to be transparent, and use
                // dark icons if we're in light theme
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons
                )

                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        networkEvent.networkState.collect {
                            when (it.networkState) {
                                NetworkState.NO_INTERNET -> viewModel.sendViewStatus(
                                    ViewStatus.Error(
                                        it.message
                                    )
                                )
                                NetworkState.BAD_REQUEST -> viewModel.sendViewStatus(
                                    ViewStatus.Error(
                                        it.message
                                    )
                                )
                                NetworkState.NOT_FOUND -> viewModel.sendViewStatus(
                                    ViewStatus.Error(
                                        it.message
                                    )
                                )
                                NetworkState.FORBIDDEN -> viewModel.sendViewStatus(
                                    ViewStatus.Error(
                                        it.message
                                    )
                                )
                                NetworkState.UNAUTHORISED -> Unit
                            }
                        }
                    }
                }
            }

            NeatWeatherComposeTheme {
                ProvideWindowInsets {
                    InitView()
                }
            }

            when (val uiState = viewModel.uiState.collectAsState().value) {
                is ViewStatus.Error -> {
                    messageError.value = uiState.errorMessage
                }
                is ViewStatus.ShowLoading -> {
                    isShowLoading.value = uiState.isShow
                }
                ViewStatus.ClickBack -> {
                    /*Do nothing*/
                }
                null -> Unit
            }

            CustomOkDialog(
                message = messageError.value,
            ) {
                viewModel.sendViewStatus(ViewStatus.Error(null))
            }
            LoadingDialog(isShow = isShowLoading.value)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (isNavigating) {
            true
        } else {
            super.dispatchTouchEvent(ev)
        }
    }

    protected fun toast(@StringRes id: Int) {
        toast(getString(id))
    }

    fun toast(message: String) {
        if (message.isNotEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } else {
            Log.e(TAG, "Message is null")
        }
    }

    @Composable
    protected abstract fun InitView()

    protected abstract fun viewModelClass(): M
}