package com.example.gardenapp.data.repo

import com.example.gardenapp.data.db.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class GardenRepository @Inject constructor(private val db: GardenDatabase) {
    fun gardens(): Flow<List<GardenEntity>> = db.gardenDao().observeGardens()
    suspend fun getGarden(id: String): GardenEntity? = db.gardenDao().getGarden(id)
    
    suspend fun upsertGarden(garden: GardenEntity) {
        db.gardenDao().upsert(garden)
    }

    suspend fun deleteGarden(g: GardenEntity) = db.gardenDao().delete(g)

    fun plants(gardenId: String): Flow<List<PlantEntity>> = db.plantDao().observeByGarden(gardenId)
    suspend fun upsertPlant(p: PlantEntity) = db.plantDao().upsert(p)
    suspend fun deletePlant(p: PlantEntity) = db.plantDao().delete(p)

    fun careRules(plantId: String): Flow<List<CareRuleEntity>> = db.ruleDao().observeRulesForPlant(plantId)
    suspend fun addCareRule(plantId: String, type: TaskType, start: LocalDate, everyDays: Int?, everyMonths: Int?) {
        db.ruleDao().upsert(CareRuleEntity(UUID.randomUUID().toString(), plantId, type, start, everyDays, everyMonths))
    }
    suspend fun deleteCareRule(rule: CareRuleEntity) = db.ruleDao().delete(rule)

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

    // Логи
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
