package com.tt.weatherapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Weather(
    @SerialName("description")
    val description: String = "",
    @SerialName("icon")
    val icon: String = "",
    @SerialName("id")
    val id: Int = 0,
    @SerialName("main")
    val main: String = ""
)