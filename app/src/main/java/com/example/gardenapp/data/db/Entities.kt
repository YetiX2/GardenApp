package com.example.gardenapp.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
data class GardenEntity(
    @PrimaryKey val id: String,
    val name: String,
    val widthCm: Int,
    val heightCm: Int,
    val gridStepCm: Int
)

@Entity
data class PlantEntity(
    @PrimaryKey val id: String,
    val gardenId: String,
    val title: String,
    val variety: String?,      // User-facing variety title, e.g., "Антоновка"
    val varietyId: String?,   // The stable UUID of the variety from the reference table
    val x: Float,
    val y: Float,
    val radius: Float,
    val plantedAt: LocalDate
)

enum class TaskType { FERTILIZE, PRUNE, TREAT, WATER, OTHER }
enum class TaskStatus { PENDING, DONE, SNOOZED }

@Entity
data class CareRuleEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    val type: TaskType,
    val start: LocalDate,
    val everyDays: Int? = null,
    val everyMonths: Int? = null
)

@Entity
data class TaskInstanceEntity(
    @PrimaryKey val id: String,
    val ruleId: String?,
    val plantId: String,
    val due: LocalDateTime,
    val exact: Boolean,
    val status: TaskStatus
)

@Entity(indices = [Index("plantId")])
data class FertilizerLogEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    val date: LocalDate,
    val amountGrams: Float,
    val note: String? = null
)

@Entity(indices = [Index("plantId")])
data class HarvestLogEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    val date: LocalDate,
    val weightKg: Float,
    val note: String? = null
)

@Entity(tableName = "ref_groups")
data class ReferenceGroupEntity(
    @PrimaryKey val id: String,
    val title: String
)

@Entity(
    tableName = "ref_cultures",
    indices = [Index(value = ["groupId"])],
    foreignKeys = [ForeignKey(entity = ReferenceGroupEntity::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE)]
)
data class ReferenceCultureEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val title: String
)

@Entity(
    tableName = "ref_varieties",
    indices = [Index(value = ["cultureId"])],
    foreignKeys = [ForeignKey(entity = ReferenceCultureEntity::class, parentColumns = ["id"], childColumns = ["cultureId"], onDelete = ForeignKey.CASCADE)]
)
data class ReferenceVarietyEntity(
    @PrimaryKey val id: String, // UUID from JSON
    val cultureId: String,
    val title: String,
    @Embedded(prefix = "i18n_") val i18n: I18nEntity,
    @Embedded(prefix = "hardiness_") val hardiness: HardinessEntity?,
    @Embedded(prefix = "filter_") val smartFilters: SmartFilterEntity
)

data class I18nEntity(val ru: String, val en: String, val kz: String)
data class HardinessEntity(val min: Int, val max: Int)
data class SmartFilterEntity(val soil_pH: String?, val height_cm: Int?)

@Entity(
    tableName = "ref_variety_tags",
    primaryKeys = ["varietyId", "key"],
    indices = [Index(value = ["varietyId"])],
    foreignKeys = [ForeignKey(entity = ReferenceVarietyEntity::class, parentColumns = ["id"], childColumns = ["varietyId"], onDelete = ForeignKey.CASCADE)]
)
data class ReferenceTagEntity(
    val varietyId: String, // Corrected to String to match Variety's UUID
    val key: String,
    val value: String
)

@Entity(
    tableName = "ref_variety_regions",
    primaryKeys = ["varietyId", "region"],
    indices = [Index(value = ["varietyId"])],
    foreignKeys = [ForeignKey(entity = ReferenceVarietyEntity::class, parentColumns = ["id"], childColumns = ["varietyId"], onDelete = ForeignKey.CASCADE)]
)
data class ReferenceRegionEntity(
    val varietyId: String,
    val region: String
)

@Entity(
    tableName = "ref_variety_cultivation",
    primaryKeys = ["varietyId", "cultivationType"],
    indices = [Index(value = ["varietyId"])],
    foreignKeys = [ForeignKey(entity = ReferenceVarietyEntity::class, parentColumns = ["id"], childColumns = ["varietyId"], onDelete = ForeignKey.CASCADE)]
)
data class ReferenceCultivationEntity(
    val varietyId: String,
    val cultivationType: String
)
