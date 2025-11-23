package com.example.gardenapp.data.db

import androidx.room.*
import java.time.LocalDate
import java.time.LocalDateTime

// --- Type Converters for java.time ---
class Converters {
    @TypeConverter fun fromEpochDay(v: Long?): LocalDate? = v?.let(LocalDate::ofEpochDay)
    @TypeConverter fun toEpochDay(d: LocalDate?): Long? = d?.toEpochDay()

    @TypeConverter fun fromEpochMillis(v: Long?): LocalDateTime? =
        v?.let { LocalDateTime.ofEpochSecond(it / 1000, ((it % 1000) * 1_000_000).toInt(), java.time.ZoneOffset.UTC) }
    @TypeConverter fun toEpochMillis(dt: LocalDateTime?): Long? =
        dt?.toInstant(java.time.ZoneOffset.UTC)?.toEpochMilli()
}

@Entity data class GardenEntity(
    @PrimaryKey val id: String,
    val name: String,
    val widthCm: Int,
    val heightCm: Int,
    val gridStepCm: Int
)

@Entity data class PlantEntity(
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

@Entity data class CareRuleEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    val type: TaskType,
    val start: LocalDate,
    val everyDays: Int? = null,
    val everyMonths: Int? = null
)

@Entity data class TaskInstanceEntity(
    @PrimaryKey val id: String,
    val ruleId: String?,
    val plantId: String,
    val due: LocalDateTime,
    val exact: Boolean,
    val status: TaskStatus
)

// --- Logs ---
@Entity(indices = [Index("plantId")])
data class FertilizerLogEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    val date: LocalDate,
    val amountGrams: Float, // количество удобрения
    val note: String? = null
)

@Entity(indices = [Index("plantId")])
data class HarvestLogEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    val date: LocalDate,
    val weightKg: Float, // собранный урожай
    val note: String? = null
)
