package com.example.gardenapp.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

// --- POJOs for combined queries ---
data class TaskWithPlantInfo(
    @Embedded val task: TaskInstanceEntity,
    val plantName: String
)

data class FertilizerLogWithPlant(
    @Embedded val log: FertilizerLogEntity,
    val plantName: String
)

data class HarvestLogWithPlant(
    @Embedded val log: HarvestLogEntity,
    val plantName: String
)

// --- DAOs ---
@Dao
interface GardenDao {
    @Query("SELECT * FROM GardenEntity") fun observeGardens(): Flow<List<GardenEntity>>
    @Query("SELECT * FROM GardenEntity WHERE id = :id") suspend fun getGarden(id: String): GardenEntity?
    @Query("SELECT * FROM GardenEntity WHERE name = :name LIMIT 1") suspend fun getGardenByName(name: String): GardenEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(g: GardenEntity)
    @Delete suspend fun delete(p: GardenEntity)
}

@Dao
interface PlantDao {
    @Query("SELECT * FROM PlantEntity WHERE gardenId = :gardenId")
    fun observeByGarden(gardenId: String): Flow<List<PlantEntity>>
    @Query("SELECT * FROM PlantEntity ORDER BY title ASC")
    fun observeAllPlants(): Flow<List<PlantEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(p: PlantEntity)
    @Delete suspend fun delete(p: PlantEntity)
}

@Dao
interface RuleDao {
    @Query("SELECT * FROM CareRuleEntity WHERE plantId = :plantId")
    fun observeRulesForPlant(plantId: String): Flow<List<CareRuleEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(rule: CareRuleEntity)
    @Delete suspend fun delete(rule: CareRuleEntity)
}

@Dao
interface TaskDao {
    @Query("""
        SELECT t.*, p.title as plantName 
        FROM TaskInstanceEntity as t
        INNER JOIN PlantEntity as p ON t.plantId = p.id
        ORDER BY t.due ASC
    """)
    fun observeAllWithPlantInfo(): Flow<List<TaskWithPlantInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(task: TaskInstanceEntity)
    @Query("UPDATE TaskInstanceEntity SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: TaskStatus)
}

@Dao
interface FertilizerLogDao {
    @Query("SELECT * FROM FertilizerLogEntity WHERE plantId = :plantId ORDER BY date DESC")
    fun observe(plantId: String): Flow<List<FertilizerLogEntity>>
    
    @Query("""
        SELECT l.*, p.title as plantName 
        FROM FertilizerLogEntity as l 
        INNER JOIN PlantEntity as p ON l.plantId = p.id 
        ORDER BY l.date DESC LIMIT :limit
    """)
    fun observeLatestWithPlant(limit: Int): Flow<List<FertilizerLogWithPlant>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(log: FertilizerLogEntity)
    @Delete suspend fun delete(log: FertilizerLogEntity)
}

@Dao
interface HarvestLogDao {
    @Query("SELECT * FROM HarvestLogEntity WHERE plantId = :plantId ORDER BY date DESC")
    fun observe(plantId: String): Flow<List<HarvestLogEntity>>

    @Query("""
        SELECT l.*, p.title as plantName 
        FROM HarvestLogEntity as l 
        INNER JOIN PlantEntity as p ON l.plantId = p.id 
        ORDER BY l.date DESC LIMIT :limit
    """)
    fun observeLatestWithPlant(limit: Int): Flow<List<HarvestLogWithPlant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(log: HarvestLogEntity)
    @Delete suspend fun delete(log: HarvestLogEntity)
}

@Dao
interface ReferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<ReferenceGroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCultures(cultures: List<ReferenceCultureEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVarieties(varieties: List<ReferenceVarietyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<ReferenceTagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegions(regions: List<ReferenceRegionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCultivationTypes(types: List<ReferenceCultivationEntity>)

    @Query("SELECT COUNT(*) FROM ref_groups")
    suspend fun getGroupsCount(): Int

    @Query("SELECT * FROM ref_groups ORDER BY title")
    fun getGroups(): Flow<List<ReferenceGroupEntity>>

    @Query("SELECT * FROM ref_cultures")
    fun getAllCultures(): Flow<List<ReferenceCultureEntity>>

    @Query("SELECT * FROM ref_varieties")
    fun getAllVarieties(): Flow<List<ReferenceVarietyEntity>>

    @Query("SELECT * FROM ref_varieties")
    suspend fun getAllVarietiesList(): List<ReferenceVarietyEntity>

    @Query("SELECT * FROM ref_cultures WHERE groupId = :groupId ORDER BY title")
    fun getCulturesByGroup(groupId: String): Flow<List<ReferenceCultureEntity>>

    @Query("SELECT * FROM ref_varieties WHERE cultureId = :cultureId ORDER BY title")
    fun getVarietiesByCulture(cultureId: String): Flow<List<ReferenceVarietyEntity>>

    @Query("SELECT * FROM ref_variety_tags WHERE varietyId = :varietyId")
    fun getTagsForVariety(varietyId: String): Flow<List<ReferenceTagEntity>>
}

// --- Converters ---
class Converters {
    @TypeConverter fun fromEpochDay(v: Long?): LocalDate? = v?.let(LocalDate::ofEpochDay)
    @TypeConverter fun toEpochDay(d: LocalDate?): Long? = d?.toEpochDay()

    @TypeConverter fun fromEpochMillis(v: Long?): LocalDateTime? =
        v?.let { LocalDateTime.ofEpochSecond(it / 1000, ((it % 1000) * 1_000_000).toInt(), java.time.ZoneOffset.UTC) }
    @TypeConverter fun toEpochMillis(dt: LocalDateTime?): Long? =
        dt?.toInstant(java.time.ZoneOffset.UTC)?.toEpochMilli()
}

// --- Main Database Class ---
@Database(
    entities = [
        GardenEntity::class, PlantEntity::class, CareRuleEntity::class, TaskInstanceEntity::class, 
        FertilizerLogEntity::class, HarvestLogEntity::class,
        ReferenceGroupEntity::class, ReferenceCultureEntity::class, ReferenceVarietyEntity::class, 
        ReferenceTagEntity::class, ReferenceRegionEntity::class, ReferenceCultivationEntity::class
    ],
    version = 13, 
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
    abstract fun referenceDao(): ReferenceDao
}
