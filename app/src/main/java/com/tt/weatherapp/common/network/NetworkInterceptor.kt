package com.tt.weatherapp.common.network

import android.content.Context
import com.tt.weatherapp.utils.isInternetAvailable
import okhttp3.Interceptor
import okhttp3.Response

class NetworkInterceptor(
    private val context: Context,
    private val networkEvent: NetworkEvent
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        /*
        * We check if there is internet
        * available in the device. If not, pass
        * the networkState as NO_INTERNET.
        * */
        if (!isInternetAvailable(context)) {
            networkEvent.publish(
                NetworkError(
                    NetworkState.NO_INTERNET,
                    NetworkState.NO_INTERNET.message
                )
            )
        }
        val request = chain.request()
        val response = chain.proceed(request)

        when (response.code) {
            400 -> networkEvent.publish(
                NetworkError(
                    NetworkState.BAD_REQUEST,
                    NetworkState.BAD_REQUEST.message
                )
            )

            401 -> networkEvent.publish(
                NetworkError(
                    NetworkState.UNAUTHORISED,
                    NetworkState.UNAUTHORISED.message
                )
            )

            403 -> networkEvent.publish(
                NetworkError(
                    NetworkState.FORBIDDEN,
                    NetworkState.FORBIDDEN.message
                )
            )

            404 -> networkEvent.publish(
                NetworkError(
                    NetworkState.NOT_FOUND,
                    NetworkState.NOT_FOUND.message
                )
            )
        }
        return response
    }
}
