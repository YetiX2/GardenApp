package com.example.gardenapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("""
        SELECT t.*, p.title as plantName 
        FROM TaskInstanceEntity as t
        INNER JOIN PlantEntity as p ON t.plantId = p.id
        ORDER BY t.due ASC
    """)
    fun observeAllWithPlantInfo(): Flow<List<TaskWithPlantInfo>>

    @Query("""
        SELECT t.*, p.title as plantName 
        FROM TaskInstanceEntity as t
        INNER JOIN PlantEntity as p ON t.plantId = p.id
        WHERE t.plantId = :plantId
        ORDER BY t.due ASC
    """)
    fun observeTasksForPlant(plantId: String): Flow<List<TaskWithPlantInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskInstanceEntity)

    @Query("UPDATE TaskInstanceEntity SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: TaskStatus)
}

@Dao
interface RuleDao {
    @Query("SELECT * FROM CareRuleEntity WHERE plantId = :plantId")
    fun observeRulesForPlant(plantId: String): Flow<List<CareRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: CareRuleEntity)

    @Delete
    suspend fun delete(rule: CareRuleEntity)
}
