package com.tt.weatherapp.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object JsonUtil {
    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        explicitNulls = false
    }
}
