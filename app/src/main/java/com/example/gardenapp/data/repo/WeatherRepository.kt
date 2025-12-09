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

            // !!! ВСТАВЬТЕ ВАШ КЛЮЧ ЗДЕСЬ !!!
            val apiKey = "9343d7d615f44fcbab6193901250912"

            val response = weatherApi.getWeather(apiKey = apiKey, location = "$lat,$lon")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
