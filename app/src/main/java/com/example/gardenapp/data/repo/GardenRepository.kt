package com.example.gardenapp.data.repo

import com.example.gardenapp.data.db.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class GardenRepository @Inject constructor(private val db: GardenDatabase) {
    fun gardens(): Flow<List<GardenEntity>> = db.gardenDao().observeGardens()
    suspend fun upsertGarden(name: String, width: Int, height: Int, step: Int) {
        db.gardenDao().upsert(GardenEntity(UUID.randomUUID().toString(), name, width, height, step))
    }
    suspend fun deleteGarden(g: GardenEntity) = db.gardenDao().delete(g)

    fun plants(gardenId: String): Flow<List<PlantEntity>> = db.plantDao().observeByGarden(gardenId)
    suspend fun upsertPlant(p: PlantEntity) = db.plantDao().upsert(p)
    suspend fun deletePlant(p: PlantEntity) = db.plantDao().delete(p)

    suspend fun addRule(plantId: String, type: TaskType, start: LocalDate, everyDays: Int?) {
        db.ruleDao().upsert(CareRuleEntity(UUID.randomUUID().toString(), plantId, type, start, everyDays, null))
    }

    fun pendingTasks(): Flow<List<TaskInstanceEntity>> = db.taskDao().observePending()

    suspend fun createTaskFromRule(rule: CareRuleEntity, due: LocalDateTime) {
        db.taskDao().upsert(
            TaskInstanceEntity(
                id = UUID.randomUUID().toString(),
                ruleId = rule.id,
                plantId = rule.plantId,
                due = due,
                exact = true,
                status = TaskStatus.PENDING
            )
        )
    }

    // Logs
    fun fertilizerLogs(plantId: String): Flow<List<FertilizerLogEntity>> = db.fertilizerLogDao().observe(plantId)
    suspend fun addFertilizerLog(plantId: String, date: LocalDate, amountGrams: Float, note: String?) {
        db.fertilizerLogDao().upsert(
            FertilizerLogEntity(UUID.randomUUID().toString(), plantId, date, amountGrams, note)
        )
    }
    suspend fun deleteFertilizerLog(item: FertilizerLogEntity) = db.fertilizerLogDao().delete(item)

    fun harvestLogs(plantId: String): Flow<List<HarvestLogEntity>> = db.harvestLogDao().observe(plantId)
    suspend fun addHarvestLog(plantId: String, date: LocalDate, weightKg: Float, note: String?) {
        db.harvestLogDao().upsert(
            HarvestLogEntity(UUID.randomUUID().toString(), plantId, date, weightKg, note)
        )
    }
    suspend fun deleteHarvestLog(item: HarvestLogEntity) = db.harvestLogDao().delete(item)
}
