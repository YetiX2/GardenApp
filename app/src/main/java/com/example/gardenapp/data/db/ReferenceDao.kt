package com.example.gardenapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ReferenceDao {

    @Transaction
    suspend fun clearAllReferenceTables() {
        _clearGroups()
        _clearCultures()
        _clearVarieties()
        _clearTags()
        _clearRegions()
        _clearCultivationTypes()
    }

    @Query("DELETE FROM ref_groups")
    suspend fun _clearGroups()

    @Query("DELETE FROM ref_cultures")
    suspend fun _clearCultures()

    @Query("DELETE FROM ref_varieties")
    suspend fun _clearVarieties()

    @Query("DELETE FROM ref_variety_tags")
    suspend fun _clearTags()

    @Query("DELETE FROM ref_variety_regions")
    suspend fun _clearRegions()

    @Query("DELETE FROM ref_variety_cultivation")
    suspend fun _clearCultivationTypes()

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
    fun getGroupsCount(): Int

    @Query("SELECT * FROM ref_groups ORDER BY title")
    fun getGroups(): Flow<List<ReferenceGroupEntity>>

    @Query("SELECT * FROM ref_cultures")
    fun getAllCultures(): Flow<List<ReferenceCultureEntity>>

    @Query("SELECT * FROM ref_cultures WHERE id = :id")
    fun getCulture(id: String): Flow<ReferenceCultureEntity?>

    @Query("SELECT * FROM ref_varieties")
    fun getAllVarieties(): Flow<List<ReferenceVarietyEntity>>

    @Query("SELECT * FROM ref_varieties")
    fun getAllVarietiesList(): List<ReferenceVarietyEntity>

    @Query("SELECT * FROM ref_varieties WHERE id = :id")
    fun getVariety(id: String): Flow<ReferenceVarietyEntity?>

    @Query("SELECT * FROM ref_cultures WHERE groupId = :groupId ORDER BY title")
    fun getCulturesByGroup(groupId: String): Flow<List<ReferenceCultureEntity>>

    @Query("SELECT * FROM ref_varieties WHERE cultureId = :cultureId ORDER BY title")
    fun getVarietiesByCulture(cultureId: String): Flow<List<ReferenceVarietyEntity>>

    @Query("SELECT * FROM ref_variety_tags WHERE varietyId = :varietyId")
    fun getTagsForVariety(varietyId: String): Flow<List<ReferenceTagEntity>>

    @Transaction
    suspend fun updateAllReferenceData(
        groups: List<ReferenceGroupEntity>,
        cultures: List<ReferenceCultureEntity>,
        varieties: List<ReferenceVarietyEntity>,
        tags: List<ReferenceTagEntity>,
        regions: List<ReferenceRegionEntity>,
        cultivationTypes: List<ReferenceCultivationEntity>
    ) {
        clearAllReferenceTables()
        insertGroups(groups)
        insertCultures(cultures)
        insertVarieties(varieties)
        insertTags(tags)
        insertRegions(regions)
        insertCultivationTypes(cultivationTypes)
    }
}
