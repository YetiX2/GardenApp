package com.example.gardenapp.di

import android.content.Context
import android.util.Log
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
        // Use a Provider to lazily get the repo and avoid circular dependency during db creation
        repoProvider: Provider<ReferenceDataRepository>
    ): GardenDatabase {
        return Room.databaseBuilder(ctx, GardenDatabase::class.java, "garden.db")
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                // Using onOpen because onCreate is only called once when the file is created.
                // onOpen is called every time the DB is opened.
                override fun onOpen(db: SupportSQLiteDatabase) {
                    Log.d("GardenDatabase", "onOpen called")
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

    // DAOs
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
    fun provideReferenceDataRepository(db: GardenDatabase, @ApplicationContext context: Context) = 
        ReferenceDataRepository(db.referenceDao(), context)
}
