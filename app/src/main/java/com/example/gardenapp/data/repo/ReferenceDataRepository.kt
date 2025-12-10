package com.example.gardenapp.data.repo

import android.content.Context
import android.util.Log
import com.example.gardenapp.data.db.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class GroupJson(val id: String, val title: String)

@Serializable
private data class CultureJson(val id: String, val title: String)

@Serializable
private data class VarietyJson(
    val id: String, // UUID
    val title: String,
    val i18n: I18nJson,
    val hardiness_ru: HardinessJson? = null,
    val regions_reco: List<String>? = null,
    val smart_filters: SmartFiltersJson,
    val tags: Map<String, JsonElement>? = null
)

@Serializable
data class I18nJson(val ru: String, val en: String, val kz: String)

@Serializable
data class HardinessJson(val min: Int, val max: Int)

@Serializable
data class SmartFiltersJson(
    val cultivation: List<String>? = null,
    val soil_pH: String? = null,
    val height_cm: Int? = null
)

@Singleton
class ReferenceDataRepository @Inject constructor(
    private val referenceDao: ReferenceDao,
    @ApplicationContext private val context: Context
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    // --- Methods to get reference data for UI ---
    fun getVariety(id: String): Flow<ReferenceVarietyEntity?> = referenceDao.getVariety(id)
    fun getTagsForVariety(varietyId: String): Flow<List<ReferenceTagEntity>> = referenceDao.getTagsForVariety(varietyId)
    fun getCulture(id: String): Flow<ReferenceCultureEntity?> = referenceDao.getCulture(id)

    suspend fun populateDatabaseIfEmpty() {
        if (referenceDao.getGroupsCount() > 0) {
            Log.d("DataPopulation", "Database already populated. Skipping.")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                Log.d("DataPopulation", "Starting data population...")

                val groupsString = context.assets.open("groups.json").bufferedReader().use { it.readText() }
                val culturesString = context.assets.open("cultures.json").bufferedReader().use { it.readText() }
                val varietiesString = context.assets.open("varieties_v3.json").bufferedReader().use { it.readText() }

                val groups = json.decodeFromString<List<GroupJson>>(groupsString)
                referenceDao.insertGroups(groups.map { ReferenceGroupEntity(it.id, it.title) })

                val cultures = json.decodeFromString<Map<String, List<CultureJson>>>(culturesString)
                val cultureEntities = cultures.flatMap { (groupId, cultureList) ->
                    cultureList.map { ReferenceCultureEntity(it.id, groupId, it.title) }
                }
                referenceDao.insertCultures(cultureEntities)

                val varieties = json.decodeFromString<Map<String, List<VarietyJson>>>(varietiesString)
                varieties.forEach { (cultureId, varietyList) ->
                    varietyList.forEach { v ->
                        val varietyEntity = ReferenceVarietyEntity(
                            id = v.id,
                            cultureId = cultureId,
                            title = v.title,
                            i18n = I18nEntity(v.i18n.ru, v.i18n.en, v.i18n.kz),
                            hardiness = v.hardiness_ru?.let { HardinessEntity(it.min, it.max) },
                            smartFilters = SmartFilterEntity(v.smart_filters.soil_pH, v.smart_filters.height_cm)
                        )
                        referenceDao.insertVarieties(listOf(varietyEntity))

                        v.tags?.let {
                            val tagEntities = it.map { (key, value) ->
                                val stringValue = when (value) {
                                    is JsonPrimitive -> value.content
                                    is JsonArray -> value.joinToString { el -> el.jsonPrimitive.content.trim('"') }
                                    else -> value.toString()
                                }
                                ReferenceTagEntity(v.id, key, stringValue)
                            }
                            referenceDao.insertTags(tagEntities)
                        }

                        v.regions_reco?.let {
                            val regionEntities = it.map { region -> ReferenceRegionEntity(v.id, region) }
                            referenceDao.insertRegions(regionEntities)
                        }

                        v.smart_filters.cultivation?.let {
                            val cultivationEntities = it.map { type -> ReferenceCultivationEntity(v.id, type) }
                            referenceDao.insertCultivationTypes(cultivationEntities)
                        }
                    }
                }
                Log.d("DataPopulation", "Data population finished successfully!")

            } catch (e: Exception) {
                Log.e("DataPopulation", "CRITICAL ERROR populating database", e)
            }
        }
    }
}