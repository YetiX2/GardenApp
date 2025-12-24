package com.example.gardenapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerLogDao {
    @Query("SELECT * FROM FertilizerLogEntity WHERE plantId = :plantId ORDER BY date DESC")
    fun observe(plantId: String): Flow<List<FertilizerLogEntity>>

    @Query("SELECT * FROM FertilizerLogEntity ORDER BY date DESC")
    fun observeAll(): Flow<List<FertilizerLogEntity>>

    @Transaction
    @Query("""
        SELECT f.*, p.title as plantName
        FROM FertilizerLogEntity as f
        INNER JOIN PlantEntity as p ON f.plantId = p.id
        ORDER BY f.date DESC
        LIMIT :limit
    """)
    fun observeLatestWithPlant(limit: Int): Flow<List<FertilizerLogWithPlant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: FertilizerLogEntity)

    @Delete
    suspend fun delete(log: FertilizerLogEntity)
}

@Dao
interface HarvestLogDao {
    @Query("SELECT * FROM HarvestLogEntity WHERE plantId = :plantId ORDER BY date DESC")
    fun observe(plantId: String): Flow<List<HarvestLogEntity>>

    @Query("SELECT * FROM HarvestLogEntity ORDER BY date DESC")
    fun observeAll(): Flow<List<HarvestLogEntity>>

    @Transaction
    @Query("""
        SELECT h.*, p.title as plantName
        FROM HarvestLogEntity as h
        INNER JOIN PlantEntity as p ON h.plantId = p.id
        ORDER BY h.date DESC
        LIMIT :limit
    """)
    fun observeLatestWithPlant(limit: Int): Flow<List<HarvestLogWithPlant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: HarvestLogEntity)

    @Delete
    suspend fun delete(log: HarvestLogEntity)
}
