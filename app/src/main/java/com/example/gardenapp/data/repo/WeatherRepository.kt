package com.example.gardenapp.data.repo

import com.example.gardenapp.data.weather.WeatherApi
import com.example.gardenapp.data.weather.WeatherResponse
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val weatherApi: WeatherApi) {

    suspend fun getWeatherData(): Result<WeatherResponse> {
        return try {
            // TODO: Replace with real user location
            val lat = 55.75
            val lon = 37.61
            val response = weatherApi.getWeather(lat, lon)
            Result.success(response)
        } catch (e: Exception) {
            // In a real app, you would log this error and distinguish between network, server, etc. errors
            Result.failure(e)
        }
    }
}
