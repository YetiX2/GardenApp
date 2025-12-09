package com.example.gardenapp.data.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    @SerialName("current") val current: CurrentWeather,
    @SerialName("forecast") val forecast: Forecast
)

@Serializable
data class CurrentWeather(
    @SerialName("temp_c") val tempC: Float,
    @SerialName("condition") val condition: Condition
)

@Serializable
data class Condition(
    @SerialName("text") val text: String,
    @SerialName("icon") val iconUrl: String
)

@Serializable
data class Forecast(
    @SerialName("forecastday") val forecastDay: List<ForecastDay>
)

@Serializable
data class ForecastDay(
    @SerialName("date") val date: String,
    @SerialName("day") val day: Day
)

@Serializable
data class Day(
    @SerialName("maxtemp_c") val maxTempC: Float,
    @SerialName("mintemp_c") val minTempC: Float,
    @SerialName("condition") val condition: Condition
)
