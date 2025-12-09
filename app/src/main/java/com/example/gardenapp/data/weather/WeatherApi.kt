package com.example.gardenapp.data.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("forecast.json")
    suspend fun getWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String, // e.g., "55.75,37.61"
        @Query("days") days: Int = 3,
        @Query("aqi") aqi: String = "no",
        @Query("alerts") alerts: String = "no"
    ): WeatherResponse
}