package com.tt.weatherapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Sys(
    @SerialName("country")
    val country: String = "",
    @SerialName("sunrise")
    val sunrise: Int = 0,
    @SerialName("sunset")
    val sunset: Int = 0
)