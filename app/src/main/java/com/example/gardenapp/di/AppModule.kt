package com.example.gardenapp.di

import android.content.Context
import androidx.room.Room
import com.example.gardenapp.data.db.GardenDatabase
import com.example.gardenapp.data.repo.GardenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): GardenDatabase =
        Room.databaseBuilder(ctx, GardenDatabase::class.java, "garden.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideRepo(db: GardenDatabase) = GardenRepository(db)
}
