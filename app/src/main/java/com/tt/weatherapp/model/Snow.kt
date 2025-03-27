package com.tt.weatherapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Snow(
    @SerialName("1h")
    val h: Double = 0.0
)