package com.tt.weatherapp.widget

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import com.google.gson.JsonSyntaxException
import com.tt.weatherapp.R
import com.tt.weatherapp.model.Location
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object WeatherInfoStateDefinition : GlanceStateDefinition<WeatherInfo> {
    private const val DATA_STORE_FILENAME = "weatherInfo"
    private val Context.datastore by dataStore(DATA_STORE_FILENAME, WeatherInfoSerializer)

    override suspend fun getDataStore(context: Context, fileKey: String) = context.datastore

    override fun getLocation(context: Context, fileKey: String) =
        context.dataStoreFile(DATA_STORE_FILENAME)

    object WeatherInfoSerializer : Serializer<WeatherInfo> {
        override val defaultValue = WeatherInfo.Unavailable()

        override suspend fun readFrom(input: InputStream) = try {
            Json.decodeFromString(
                WeatherInfo.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (exception: JsonSyntaxException) {
            throw CorruptionException("Could not read weather data: ${exception.message}")
        }

        override suspend fun writeTo(t: WeatherInfo, output: OutputStream) {
            output.use {
                it.write(
                    Json.encodeToString(WeatherInfo.serializer(), t).encodeToByteArray()
                )
            }
        }
    }
}

@Serializable
sealed interface WeatherInfo {
    @Serializable
    object Loading : WeatherInfo

    @Serializable
    data class Available(val location: Location, val imagePath: String?) : WeatherInfo

    @Serializable
    data class Unavailable(@StringRes val message: Int = R.string.no_place_found) : WeatherInfo
}
