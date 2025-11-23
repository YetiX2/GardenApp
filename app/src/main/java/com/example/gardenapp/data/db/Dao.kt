package com.example.gardenapp.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface GardenDao {
    @Query("SELECT * FROM GardenEntity") fun observeGardens(): Flow<List<GardenEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(g: GardenEntity)
    @Delete suspend fun delete(p: GardenEntity)
}

@Dao interface PlantDao {
    @Query("SELECT * FROM PlantEntity WHERE gardenId = :gardenId")
    fun observeByGarden(gardenId: String): Flow<List<PlantEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(p: PlantEntity)
    @Delete suspend fun delete(p: PlantEntity)
}

@Dao interface RuleDao {
    @Query("SELECT * FROM CareRuleEntity WHERE plantId = :plantId")
    fun observeRulesForPlant(plantId: String): Flow<List<CareRuleEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(rule: CareRuleEntity)
    @Delete suspend fun delete(rule: CareRuleEntity)
}

@Dao interface TaskDao {
    @Query("SELECT * FROM TaskInstanceEntity WHERE status = 'PENDING' ORDER BY due ASC")
    fun observePending(): Flow<List<TaskInstanceEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(task: TaskInstanceEntity)
    @Query("UPDATE TaskInstanceEntity SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: TaskStatus)
}

@Dao interface FertilizerLogDao {
    @Query("SELECT * FROM FertilizerLogEntity WHERE plantId = :plantId ORDER BY date DESC")
    fun observe(plantId: String): Flow<List<FertilizerLogEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(log: FertilizerLogEntity)
    @Delete suspend fun delete(log: FertilizerLogEntity)
}

@Dao interface HarvestLogDao {
    @Query("SELECT * FROM HarvestLogEntity WHERE plantId = :plantId ORDER BY date DESC")
    fun observe(plantId: String): Flow<List<HarvestLogEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(log: HarvestLogEntity)
    @Delete suspend fun delete(log: HarvestLogEntity)
}

@Database(
    entities = [
        GardenEntity::class,
        PlantEntity::class,
        CareRuleEntity::class,
        TaskInstanceEntity::class,
        FertilizerLogEntity::class,
        HarvestLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GardenDatabase : RoomDatabase() {
    abstract fun gardenDao(): GardenDao
    abstract fun plantDao(): PlantDao
    abstract fun ruleDao(): RuleDao
    abstract fun taskDao(): TaskDao
    abstract fun fertilizerLogDao(): FertilizerLogDao
    abstract fun harvestLogDao(): HarvestLogDao
}
