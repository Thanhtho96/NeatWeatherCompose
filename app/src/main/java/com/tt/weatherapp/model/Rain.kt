package com.tt.weatherapp.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Rain(
    @SerializedName("1h")
    @Expose
    val oneHour: Double
)