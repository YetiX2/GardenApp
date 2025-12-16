package com.example.gardenapp.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gardenapp.data.db.GardenDatabase
import com.example.gardenapp.data.db.ReferenceDao
import com.example.gardenapp.data.db.TestDataGenerator
import com.example.gardenapp.data.location.LocationTracker
import com.example.gardenapp.data.repo.GardenRepository
import com.example.gardenapp.data.repo.ReferenceDataRepository
import com.example.gardenapp.data.repo.WeatherRepository
import com.example.gardenapp.data.settings.SettingsManager
import com.example.gardenapp.data.weather.WeatherApi
import com.google.android.gms.location.LocationServices
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Provider
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

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

    // --- Weather API Dependencies ---
    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.weatherapi.com/v1/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideTestDataGenerator(db: GardenDatabase, referenceDao: ReferenceDao): TestDataGenerator {
        return TestDataGenerator(db, referenceDao)
    }
    @Provides
    @Singleton
    fun provideRepo(db: GardenDatabase, referenceDao: ReferenceDao, locationTracker: LocationTracker): GardenRepository {
        return GardenRepository(db, referenceDao)
    }

    @Provides
    @Singleton
    fun provideWeatherApi(retrofit: Retrofit): WeatherApi {
        return retrofit.create(WeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: Application): com.google.android.gms.location.FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }
    @Provides
    @Singleton
    fun provideLocationTracker(
        locationClient: com.google.android.gms.location.FusedLocationProviderClient,
        app: Application
    ): LocationTracker {
        return LocationTracker(locationClient, app)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(weatherApi: WeatherApi, locationTracker: LocationTracker) = WeatherRepository(weatherApi, locationTracker)

    // ... остальные DAO ...
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
