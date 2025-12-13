package com.example.gardenapp.data.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

// Sealed interface for recent activity feed on the dashboard
sealed interface RecentActivity {
    val date: LocalDate
    data class Fertilizer(val data: FertilizerLogWithPlant) : RecentActivity {
        override val date: LocalDate get() = data.log.date
    }
    data class Harvest(val data: HarvestLogWithPlant) : RecentActivity {
        override val date: LocalDate get() = data.log.date
    }
}


enum class GardenType { PLOT, GREENHOUSE, BED , BUILDING } // ADDED


@Entity
data class GardenEntity(
    @PrimaryKey val id: String,
    val name: String,
    val widthCm: Int,
    val heightCm: Int,
    val gridStepCm: Int,
    @ColumnInfo(defaultValue = "PLOT") val type: GardenType = GardenType.PLOT,
    val parentId: String? = null,
    val x: Int? = null, // ADDED
    val y: Int? = null, // ADDED
    val climateZone: Int? = null
)

@Entity
data class PlantEntity(
    @PrimaryKey val id: String,
    val gardenId: String,
    val title: String,
    val variety: String?,
    val varietyId: String?,
    val x: Float,
    val y: Float,
    val radius: Float,
    val plantedAt: LocalDate
)

@Entity
data class CareRuleEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    val type: TaskType,
    val start: LocalDate,
    val everyDays: Int?,
    val everyMonths: Int? = null
)

enum class TaskType { FERTILIZE, PRUNE, TREAT, WATER, OTHER }
enum class TaskStatus { PENDING, DONE, SNOOZED, REJECTED }

@Entity(tableName = "TaskInstanceEntity")
data class TaskInstanceEntity(
    @PrimaryKey val id: String,
    val ruleId: String?,
    val plantId: String,
    val type: TaskType,
    val due: LocalDateTime,
    val exact: Boolean,
    val status: TaskStatus,
    val notes: String? = null // ADDED THIS
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

// --- NEW REFERENCE ENTITIES (Rebuilt for new JSON structure) ---

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
    val varietyId: String,
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
