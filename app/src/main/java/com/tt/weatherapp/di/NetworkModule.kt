package com.tt.weatherapp.di

import android.content.Context
import com.tt.weatherapp.common.Constant
import com.tt.weatherapp.common.network.NetworkEvent
import com.tt.weatherapp.common.network.NetworkInterceptor
import com.tt.weatherapp.data.remotes.ApiService
import com.tt.weatherapp.data.repositories.AppRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { provideHttpClient(androidContext(), get()) }
    single { provideRetrofit(get()) }
    single { provideApiService(get()) }
    single { AppRepository(androidContext(), get(), get(), get()) }
    single { NetworkEvent(get()) }
}

private val interceptor: Interceptor = Interceptor { chain ->
    var countRetry = 0
    var response: Response? = null
    val request = chain.request()
    var ioException: Exception? = null
    while (countRetry < 1 && (response == null || response.isSuccessful.not())) {
        try {
            val newRequest = request.newBuilder()
            newRequest.headers(request.headers)
            newRequest.addHeader("Accept", "application/json")
            newRequest.addHeader("Content-Type", "application/json")
            newRequest.method(request.method, request.body)
            response = chain.proceed(newRequest.build())
        } catch (iox: Exception) {
            ioException = iox
        } finally {
            countRetry = countRetry.inc()
        }
    }
    if (ioException != null) throw ioException
    response!!
}

private fun provideRetrofit(client: OkHttpClient): Retrofit {
    return Retrofit.Builder().baseUrl(Constant.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

private fun provideApiService(retrofit: Retrofit): ApiService {
    return retrofit.create(ApiService::class.java)
}

private fun provideHttpClient(context: Context, networkEvent: NetworkEvent): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor()
    val networkInterceptor = NetworkInterceptor(context, networkEvent)
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(networkInterceptor)
        .addInterceptor(interceptor)
        .connectTimeout(180, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
}