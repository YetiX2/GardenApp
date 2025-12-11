package com.example.gardenapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GardenDao {
    @Query("SELECT * FROM GardenEntity")
    fun observeGardens(): Flow<List<GardenEntity>>

    @Query("SELECT * FROM GardenEntity WHERE id = :id")
    suspend fun getGarden(id: String): GardenEntity?

    @Query("SELECT * FROM GardenEntity WHERE name = :name LIMIT 1")
    suspend fun getGardenByName(name: String): GardenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(g: GardenEntity)

    @Delete
    suspend fun delete(p: GardenEntity)
}
