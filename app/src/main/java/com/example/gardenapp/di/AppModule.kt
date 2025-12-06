package com.example.gardenapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gardenapp.data.db.GardenDatabase
import com.example.gardenapp.data.repo.GardenRepository
import com.example.gardenapp.data.repo.ReferenceDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

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
    fun provideRepo(db: GardenDatabase) = GardenRepository(db)

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
    fun provideReferenceDao(db: GardenDatabase) = db.referenceDao()

    @Provides
    @Singleton
    fun provideReferenceDataRepository(referenceDao: com.example.gardenapp.data.db.ReferenceDao, @ApplicationContext context: Context) = 
        ReferenceDataRepository(referenceDao, context)
}
