package com.example.gardenapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gardenapp.data.db.GardenDatabase
import com.example.gardenapp.data.db.ReferenceDao
import com.example.gardenapp.data.repo.GardenRepository
import com.example.gardenapp.data.repo.ReferenceDataRepository
import com.example.gardenapp.data.repo.WeatherRepository
import com.example.gardenapp.data.weather.Condition
import com.example.gardenapp.data.weather.CurrentWeather
import com.example.gardenapp.data.weather.Day
import com.example.gardenapp.data.weather.Forecast
import com.example.gardenapp.data.weather.ForecastDay
import com.example.gardenapp.data.weather.WeatherApi
import com.example.gardenapp.data.weather.WeatherResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

// Fake Weather API implementation for demonstration purposes
class FakeWeatherApi : WeatherApi {
    override suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse {
        delay(1000) // Simulate network delay
        return WeatherResponse(
            current = CurrentWeather(
                tempC = 23.0f,
                condition = Condition(text = "Солнечно", iconUrl = "//cdn.weatherapi.com/weather/64x64/day/113.png")
            ),
            forecast = Forecast(
                forecastDay = listOf(
                    ForecastDay(
                        date = "2023-08-01",
                        day = Day(maxTempC = 25.0f, minTempC = 15.0f, condition = Condition(text = "", iconUrl = ""))
                    ),
                    ForecastDay(
                        date = "2023-08-02",
                        day = Day(maxTempC = 21.0f, minTempC = 14.0f, condition = Condition(text = "", iconUrl = ""))
                    ),
                    ForecastDay(
                        date = "2023-08-03",
                        day = Day(maxTempC = 16.0f, minTempC = 10.0f, condition = Condition(text = "", iconUrl = ""))
                    )
                )
            )
        )
    }
}


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDb(
        @ApplicationContext ctx: Context,
        repoProvider: Provider<ReferenceDataRepository>
    ): GardenDatabase {
        return Room.databaseBuilder(ctx, GardenDatabase::class.java, "garden.db")
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        repoProvider.get().populateDatabaseIfEmpty()
                    }
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApi(): WeatherApi = FakeWeatherApi()

    @Provides
    @Singleton
    fun provideWeatherRepository(weatherApi: WeatherApi) = WeatherRepository(weatherApi)

    @Provides
    @Singleton
    fun provideRepo(db: GardenDatabase, referenceDao: ReferenceDao) = GardenRepository(db, referenceDao)

    @Provides
    fun provideGardenDao(db: GardenDatabase) = db.gardenDao()

    @Provides
    fun providePlantDao(db: GardenDatabase) = db.plantDao()

    @Provides
    fun provideRuleDao(db: GardenDatabase) = db.ruleDao()

    @Provides
    fun provideTaskDao(db: GardenDatabase) = db.taskDao()

    @Provides
    fun provideFertilizerLogDao(db: GardenDatabase) = db.fertilizerLogDao()

    @Provides
    fun provideHarvestLogDao(db: GardenDatabase) = db.harvestLogDao()

    @Provides
    fun provideReferenceDao(db: GardenDatabase): ReferenceDao = db.referenceDao()

    @Provides
    @Singleton
    fun provideReferenceDataRepository(referenceDao: ReferenceDao, @ApplicationContext context: Context) = 
        ReferenceDataRepository(referenceDao, context)
}
