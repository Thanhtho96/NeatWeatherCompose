package com.tt.weatherapp.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.Locale

object LocationUtil {
    suspend fun getLocationName(context: Context, latitude: Double, longitude: Double) =
        suspendCancellableCoroutine { continuation ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Geocoder(context, Locale.getDefault()).getFromLocation(
                        latitude,
                        longitude,
                        1
                    ) {
                        continuation.resumeWith(Result.success(getLocationNameFromAddress(it)))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val address =
                        Geocoder(context, Locale.getDefault()).getFromLocation(
                            latitude,
                            longitude,
                            1
                        ) ?: emptyList()

                    continuation.resumeWith(Result.success(getLocationNameFromAddress(address)))
                }
            } catch (e: IOException) {
                continuation.resumeWith(Result.success(""))
            }
        }

    private fun getLocationNameFromAddress(address: List<Address>): String {
        val commaJoin = ", "

        val builder = StringBuilder()
        val subAdminArea = address.firstOrNull()?.subAdminArea
        val adminArea = address.firstOrNull()?.adminArea
        subAdminArea?.let {
            builder.append(it)
            builder.append(commaJoin)
        }
        adminArea?.let {
            builder.append(it)
            builder.append(commaJoin)
        }

        return builder.removeSuffix(commaJoin).toString()
    }
}