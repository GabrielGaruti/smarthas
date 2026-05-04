package com.smarthas.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface SmartHasApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("measurements")
    suspend fun getMeasurements(@Header("Authorization") token: String): List<MeasurementResponse>

    @POST("measurements")
    suspend fun createMeasurement(
        @Header("Authorization") token: String,
        @Body request: MeasurementRequest
    ): MeasurementResponse

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8000/"

        fun create(tokenProvider: () -> String?): SmartHasApi {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val token = tokenProvider()
                val newRequest = if (token != null) {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    originalRequest
                }
                chain.proceed(newRequest)
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SmartHasApi::class.java)
        }
    }
}
