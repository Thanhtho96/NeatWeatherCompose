package com.tt.weatherapp.widget

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import com.tt.weatherapp.R
import com.tt.weatherapp.model.Location
import com.tt.weatherapp.utils.JsonUtil
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
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
            JsonUtil.json.decodeFromString<WeatherInfo>(
                input.readBytes().decodeToString()
            )
        } catch (exception: IllegalArgumentException) {
            throw CorruptionException("Could not read weather data: ${exception.message}")
        }

        override suspend fun writeTo(t: WeatherInfo, output: OutputStream) {
            output.use {
                it.write(
                    JsonUtil.json.encodeToString(t).encodeToByteArray()
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
    data class Available(val location: Location) : WeatherInfo

    @Serializable
    data class Unavailable(@StringRes val message: Int = R.string.no_place_found) : WeatherInfo
}
