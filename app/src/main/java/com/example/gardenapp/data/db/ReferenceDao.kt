package com.example.gardenapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM ref_cultures WHERE id = :id")
    fun getCulture(id: String): Flow<ReferenceCultureEntity?>

    @Query("SELECT * FROM ref_varieties")
    fun getAllVarieties(): Flow<List<ReferenceVarietyEntity>>

    @Query("SELECT * FROM ref_varieties")
    suspend fun getAllVarietiesList(): List<ReferenceVarietyEntity>

    @Query("SELECT * FROM ref_varieties WHERE id = :id")
    fun getVariety(id: String): Flow<ReferenceVarietyEntity?>

    @Query("SELECT * FROM ref_cultures WHERE groupId = :groupId ORDER BY title")
    fun getCulturesByGroup(groupId: String): Flow<List<ReferenceCultureEntity>>

    @Query("SELECT * FROM ref_varieties WHERE cultureId = :cultureId ORDER BY title")
    fun getVarietiesByCulture(cultureId: String): Flow<List<ReferenceVarietyEntity>>

    @Query("SELECT * FROM ref_variety_tags WHERE varietyId = :varietyId")
    fun getTagsForVariety(varietyId: String): Flow<List<ReferenceTagEntity>>
}
