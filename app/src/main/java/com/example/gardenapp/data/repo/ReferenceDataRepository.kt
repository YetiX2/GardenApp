package com.example.gardenapp.data.repo

import android.content.Context
import android.util.Log
import com.example.gardenapp.data.db.*
import com.example.gardenapp.data.settings.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

private const val LATEST_DATA_VERSION = 6 // BUMPED VERSION

// --- JSON Deserialization Classes ---

@Serializable
private data class GroupJson(val id: String, val title: String)

@Serializable
private data class CultureJson(val id: String, val title: String)

@Serializable
private data class VarietyJson(
    val id: String,
    val title: String,
    val i18n: I18nJson = I18nJson("", "", ""),
    val hardiness_ru: HardinessJson? = null,
    val regions_reco: List<String>? = null,
    val smart_filters: SmartFiltersJson = SmartFiltersJson(),
    val planting_window: PlantingWindowJson? = null, // ADDED
    val harvest_window: MonthRangeJson? = null, // ADDED
    val bloom_window: MonthRangeJson? = null, // ADDED
    val tags: Map<String, JsonElement>? = null
)

@Serializable
data class PlantingWindowJson(
    val spring: MonthRangeJson? = null,
    val autumn: MonthRangeJson? = null,
    val seedling: MonthRangeJson? = null,
    val transplant_greenhouse: MonthRangeJson? = null,
    val transplant_og: MonthRangeJson? = null
)

@Serializable
data class MonthRangeJson(val start: Int?, val end: Int?)

@Serializable
data class I18nJson(val ru: String, val en: String, val kz: String)

@Serializable
data class HardinessJson(val min: Int?, val max: Int?)

@Serializable
data class SmartFiltersJson(
    val cultivation: List<String>? = null,
    val soil_pH: String? = null,
    val height_cm: Int? = null
)

@Singleton
class ReferenceDataRepository @Inject constructor(
    private val referenceDao: ReferenceDao,
    private val settingsManager: SettingsManager,
    @ApplicationContext private val context: Context
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    suspend fun checkAndUpdate() {
        try {
            val currentVersion = settingsManager.dataVersion.first()
            if (currentVersion < LATEST_DATA_VERSION) {
                Log.d("DataUpdate", "Current data version ($currentVersion) is older than latest ($LATEST_DATA_VERSION). Updating.")
                forceUpdateFromAssets()
                settingsManager.setDataVersion(LATEST_DATA_VERSION)
            }
        } catch (e: Exception) {
            Log.e("DataUpdate", "CRITICAL ERROR updating database from assets", e)
            throw e
        }
    }

    private suspend fun forceUpdateFromAssets() {
        withContext(Dispatchers.IO) {
            try {
                Log.d("DataUpdate", "Starting data update from assets...")

                val groupsString = context.assets.open("groups.json").bufferedReader().use { it.readText() }
                val culturesString = context.assets.open("cultures.json").bufferedReader().use { it.readText() }
                val varietiesString = context.assets.open("varieties_v5.json").bufferedReader().use { it.readText() }

                val groups = json.decodeFromString<List<GroupJson>>(groupsString)
                val cultures = json.decodeFromString<Map<String, List<CultureJson>>>(culturesString)
                val varieties = json.decodeFromString<Map<String, List<VarietyJson>>>(varietiesString)

                val groupEntities = groups.map { ReferenceGroupEntity(it.id, it.title) }
                val cultureEntities = cultures.flatMap { (groupId, cultureList) -> cultureList.map { ReferenceCultureEntity(it.id, groupId, it.title) } }

                val varietyEntities = mutableListOf<ReferenceVarietyEntity>()
                val tagEntities = mutableListOf<ReferenceTagEntity>()
                val regionEntities = mutableListOf<ReferenceRegionEntity>()
                val cultivationEntities = mutableListOf<ReferenceCultivationEntity>()

                varieties.forEach { (cultureId, varietyList) ->
                    varietyList.forEach { v ->
                        varietyEntities.add(ReferenceVarietyEntity(
                            id = v.id,
                            cultureId = cultureId,
                            title = v.title,
                            i18n = I18nEntity(v.i18n.ru, v.i18n.en, v.i18n.kz),
                            hardiness = v.hardiness_ru?.let { HardinessEntity(it.min, it.max) },
                            smartFilters = SmartFilterEntity(v.smart_filters.soil_pH, v.smart_filters.height_cm),
                            plantingWindow = v.planting_window?.let {
                                PlantingWindowEntity(
                                    spring = it.spring?.let { r -> MonthRangeEntity(r.start, r.end) },
                                    autumn = it.autumn?.let { r -> MonthRangeEntity(r.start, r.end) },
                                    seedling = it.seedling?.let { r -> MonthRangeEntity(r.start, r.end) },
                                    transplantGreenhouse = it.transplant_greenhouse?.let { r -> MonthRangeEntity(r.start, r.end) },
                                    transplantOg = it.transplant_og?.let { r -> MonthRangeEntity(r.start, r.end) }
                                )
                            },
                            harvestWindow = v.harvest_window?.let { MonthRangeEntity(it.start, it.end) },
                            bloomWindow = v.bloom_window?.let { MonthRangeEntity(it.start, it.end) }
                        ))

                        v.tags?.let { tags ->
                            val mappedTags = tags.map { (key, value) ->
                                val stringValue = if (value is JsonPrimitive) value.content else value.toString().trim('"')
                                ReferenceTagEntity(v.id, key, stringValue)
                            }
                            tagEntities.addAll(mappedTags)
                        }
                        v.regions_reco?.let { regions -> regionEntities.addAll(regions.map { ReferenceRegionEntity(v.id, it) }) }
                        v.smart_filters.cultivation?.let { cultivation -> cultivationEntities.addAll(cultivation.map { ReferenceCultivationEntity(v.id, it) }) }
                    }
                }

                referenceDao.updateAllReferenceData(groupEntities, cultureEntities, varietyEntities, tagEntities, regionEntities, cultivationEntities)
                Log.d("DataUpdate", "Data update finished successfully!")
            } catch (e: Exception) {
                Log.e("DataUpdate", "CRITICAL ERROR updating database from assets", e)
                throw e
            }
        }
    }

    // --- Getters for UI ---
    fun getVariety(id: String): Flow<ReferenceVarietyEntity?> = referenceDao.getVariety(id)
    fun getTagsForVariety(varietyId: String): Flow<List<ReferenceTagEntity>> = referenceDao.getTagsForVariety(varietyId)
    fun getCulture(id: String): Flow<ReferenceCultureEntity?> = referenceDao.getCulture(id)
    fun getGroups(): Flow<List<ReferenceGroupEntity>> = referenceDao.getGroups()
    fun getAllCultures(): Flow<List<ReferenceCultureEntity>> = referenceDao.getAllCultures()
    fun getAllVarieties(): Flow<List<ReferenceVarietyEntity>> = referenceDao.getAllVarieties()
    fun getCulturesByGroup(groupId: String): Flow<List<ReferenceCultureEntity>> = referenceDao.getCulturesByGroup(groupId)
    fun getVarietiesByCulture(cultureId: String): Flow<List<ReferenceVarietyEntity>> = referenceDao.getVarietiesByCulture(cultureId)
}
