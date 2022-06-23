package com.tt.weatherapp.common

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tt.weatherapp.common.network.NetworkEvent
import com.tt.weatherapp.ui.theme.NeatWeatherComposeTheme
import com.tt.weatherapp.utils.CustomOkDialog
import com.tt.weatherapp.utils.LoadingDialog
import com.tt.weatherapp.utils.StatusBarUtil
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
                            ViewStatus.Error(it.message)
                        }
                    }
                }
            }

            NeatWeatherComposeTheme {
                InitView()
            }

            when (val uiState = viewModel.apiState.collectAsState().value) {
                is ViewStatus.Error -> {
                    ShowOkDialog(uiState)
                }
                ViewStatus.ClickBack -> {
                    /*Do nothing*/
                }
                is ViewStatus.Toast -> toast(uiState.message)
                is ViewStatus.ShowLoading -> ShowLoadingDialog(uiState.isLoading)
                null -> Unit
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (isNavigating) {
            true
        } else {
            super.dispatchTouchEvent(ev)
        }
    }

    @Composable
    private fun ShowOkDialog(uiState: ViewStatus.Error) {
        CustomOkDialog(
            message = uiState.errorMessage,
        ) {
            viewModel.sendViewStatus(ViewStatus.Error(null))
        }
    }

    @Composable
    private fun ShowLoadingDialog(isLoading: Boolean) {
        LoadingDialog(isShow = isLoading)
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