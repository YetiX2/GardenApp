package com.example.gardenapp.data.weather

// This is where you would use Retrofit annotations, for example:
// import retrofit2.http.GET
// import retrofit2.http.Query

interface WeatherApi {
    /**
     * Fetches weather data from the API.
     * The user would implement this with Retrofit.
     * Example:
     * @GET("forecast.json")
     * suspend fun getWeather(
     *     @Query("key") apiKey: String,
     *     @Query("q") location: String,
     *     @Query("days") days: Int = 3
     * ): WeatherResponse
     */
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse
}
