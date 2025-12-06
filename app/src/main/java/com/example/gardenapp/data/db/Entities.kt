package com.example.gardenapp.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

// --- ORIGINAL ENTITIES (from your project) ---
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
    val variety: String?,
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

// --- NEW REFERENCE ENTITIES ---
@Entity(tableName = "reference_groups")
data class ReferenceGroupEntity(
    @PrimaryKey val id: String,
    val title: String
)

@Entity(
    tableName = "reference_cultures",
    indices = [Index(value = ["groupId"])],
    foreignKeys = [
        ForeignKey(entity = ReferenceGroupEntity::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class ReferenceCultureEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val title: String
)

@Entity(
    tableName = "reference_varieties",
    indices = [Index(value = ["cultureId"])],
    foreignKeys = [
        ForeignKey(entity = ReferenceCultureEntity::class, parentColumns = ["id"], childColumns = ["cultureId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class ReferenceVarietyEntity(
    @PrimaryKey(autoGenerate = true) val varietyId: Long = 0,
    val cultureId: String,
    val title: String
)

@Entity(
    tableName = "reference_tags",
    indices = [Index(value = ["varietyId"])],
    foreignKeys = [
        ForeignKey(entity = ReferenceVarietyEntity::class, parentColumns = ["varietyId"], childColumns = ["varietyId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class ReferenceTagEntity(
    @PrimaryKey(autoGenerate = true) val tagId: Long = 0,
    val varietyId: Long,
    val key: String,
    val value: String
)
