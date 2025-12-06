package com.example.gardenapp.data.repo

import android.content.Context
import android.util.Log
import com.example.gardenapp.data.db.ReferenceCultureEntity
import com.example.gardenapp.data.db.ReferenceDao
import com.example.gardenapp.data.db.ReferenceGroupEntity
import com.example.gardenapp.data.db.ReferenceTagEntity
import com.example.gardenapp.data.db.ReferenceVarietyEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class GroupJson(val id: String, val title: String)

@Serializable
private data class CultureJson(val id: String, val title: String)

@Serializable
private data class VarietyJson(val title: String, val tags: Map<String, JsonElement>? = null)

@Singleton
class ReferenceDataRepository @Inject constructor(
    private val referenceDao: ReferenceDao,
    @ApplicationContext private val context: Context
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

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
                val varietiesString = context.assets.open("varieties.json").bufferedReader().use { it.readText() }

                val groups = json.decodeFromString<List<GroupJson>>(groupsString)
                referenceDao.insertGroups(groups.map { ReferenceGroupEntity(it.id, it.title) })

                val cultures = json.decodeFromString<Map<String, List<CultureJson>>>(culturesString)
                val cultureEntities = cultures.flatMap { (groupId, cultureList) ->
                    cultureList.map { ReferenceCultureEntity(it.id, groupId, it.title) }
                }
                referenceDao.insertCultures(cultureEntities)

                val varieties = json.decodeFromString<Map<String, List<VarietyJson>>>(varietiesString)
                varieties.forEach { (cultureId, varietyList) ->
                    varietyList.forEach { varietyJson ->
                        val newVariety = ReferenceVarietyEntity(cultureId = cultureId, title = varietyJson.title)
                        val generatedIds = referenceDao.insertVarieties(listOf(newVariety))
                        if (generatedIds.isNotEmpty()) {
                            val generatedId = generatedIds.first()
                            varietyJson.tags?.let { tags ->
                                val tagEntities = tags.map { (key, value) ->
                                    val stringValue = when (value) {
                                        is JsonPrimitive -> value.content
                                        is JsonArray -> value.joinToString { it.jsonPrimitive.content }
                                        else -> ""
                                    }
                                    ReferenceTagEntity(varietyId = generatedId, key = key, value = stringValue)
                                }
                                referenceDao.insertTags(tagEntities)
                            }
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