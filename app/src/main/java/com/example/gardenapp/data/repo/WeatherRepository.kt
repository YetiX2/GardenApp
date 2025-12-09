package com.example.gardenapp.data.repo

import com.example.gardenapp.data.location.LocationTracker
import com.example.gardenapp.data.weather.WeatherApi
import com.example.gardenapp.data.weather.WeatherResponse
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApi,
    private val locationTracker: LocationTracker
) {

    suspend fun getWeatherData(): Result<WeatherResponse> {
        return try {
            val location = locationTracker.getCurrentLocation()
            if (location == null) {
                return Result.failure(Exception("Не удалось получить геолокацию. Убедитесь, что разрешение предоставлено и GPS включен."))
            }
            
            // !!! ВСТАВЬТЕ ВАШ КЛЮЧ ЗДЕСЬ !!!
            val apiKey = "9343d7d615f44fcbab6193901250912"

            val response = weatherApi.getWeather(
                apiKey = apiKey, 
                location = "${location.latitude},${location.longitude}"
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
