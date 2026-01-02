package com.example.gardenapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM PlantEntity WHERE gardenId = :gardenId")
    fun observeByGarden(gardenId: String): Flow<List<PlantEntity>>

    @Query("SELECT * FROM PlantEntity WHERE gardenId IN (:gardenIds)")
    fun observeByGardenIds(gardenIds: List<String>): Flow<List<PlantEntity>>

    @Query("SELECT * FROM PlantEntity WHERE id = :id")
    fun observePlant(id: String): Flow<PlantEntity?>

    @Query("SELECT * FROM PlantEntity ORDER BY title ASC")
    fun observeAllPlants(): Flow<List<PlantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(p: PlantEntity)

    @Delete
    suspend fun delete(p: PlantEntity)
    @Query("SELECT * FROM PlantEntity WHERE id = :id")
    suspend fun getPlant(id: String): PlantEntity?
}
