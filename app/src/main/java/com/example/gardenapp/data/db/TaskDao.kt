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

    @Query("""
        SELECT t.* 
        FROM TaskInstanceEntity as t
        INNER JOIN PlantEntity as p ON t.plantId = p.id
        WHERE p.gardenId = :gardenId AND t.status = 'PENDING'
    """)
    fun observePendingTasksForGarden(gardenId: String): Flow<List<TaskInstanceEntity>>

    @Query("""
        SELECT t.* 
        FROM TaskInstanceEntity as t
        INNER JOIN PlantEntity as p ON t.plantId = p.id
        WHERE p.gardenId IN (:gardenIds) AND t.status = 'PENDING'
    """)
    fun observePendingTasksForGardens(gardenIds: List<String>): Flow<List<TaskInstanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskInstanceEntity)

    @Query("SELECT * FROM TaskInstanceEntity WHERE id = :id")
    suspend fun getTask(id: String): TaskInstanceEntity?

    @Query("UPDATE TaskInstanceEntity SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: TaskStatus)

    @Query("SELECT * FROM TaskInstanceEntity WHERE ruleId = :ruleId ORDER BY due DESC LIMIT 1")
    suspend fun getLatestTaskForRule(ruleId: String): TaskInstanceEntity?
}

@Dao
interface RuleDao {
    @Query("SELECT * FROM CareRuleEntity")
    fun getAllCareRules(): List<CareRuleEntity>

    @Query("SELECT * FROM CareRuleEntity WHERE id = :id")
    suspend fun getRule(id: String): CareRuleEntity? // ADDED

    @Query("SELECT * FROM CareRuleEntity WHERE plantId = :plantId")
    fun observeRulesForPlant(plantId: String): Flow<List<CareRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: CareRuleEntity)

    @Delete
    suspend fun delete(rule: CareRuleEntity)
}
