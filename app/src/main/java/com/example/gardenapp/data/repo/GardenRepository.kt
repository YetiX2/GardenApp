package com.example.gardenapp.data.repo

import com.example.gardenapp.data.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random
import kotlin.toString

class GardenRepository @Inject constructor(
    private val db: GardenDatabase,
    private val referenceDao: ReferenceDao 
) {
    fun gardens(): Flow<List<GardenEntity>> = db.gardenDao().observeGardens()
    suspend fun getGarden(id: String): GardenEntity? = db.gardenDao().getGarden(id)

    fun getChildGardens(parentId: String): Flow<List<GardenEntity>> = db.gardenDao().getChildGardens(parentId)
    fun observeGardenById(id: String): Flow<GardenEntity?> = db.gardenDao().observeGardenById(id)
    suspend fun getGardenByName(name: String): GardenEntity? = db.gardenDao().getGardenByName(name)

    suspend fun upsertGarden(garden: GardenEntity) {
        db.gardenDao().upsert(garden)
    }

    suspend fun deleteGarden(g: GardenEntity) = db.gardenDao().delete(g)

    fun plantsForGardens(gardenId: String): Flow<List<PlantEntity>> {
        return getChildGardens(gardenId).flatMapLatest { childGardens ->
            val gardenIds = listOf(gardenId) + childGardens.map { it.id }
            db.plantDao().observeByGardenIds(gardenIds)
        }
    }
    fun observeAllPlants(): Flow<List<PlantEntity>> = db.plantDao().observeAllPlants()
    fun observePlant(id: String): Flow<PlantEntity?> = db.plantDao().observePlant(id)
    suspend fun upsertPlant(p: PlantEntity) = db.plantDao().upsert(p)
    suspend fun deletePlant(p: PlantEntity) = db.plantDao().delete(p)

    fun careRules(plantId: String): Flow<List<CareRuleEntity>> = db.ruleDao().observeRulesForPlant(plantId)

    suspend fun getAllCareRules(): List<CareRuleEntity> = withContext(Dispatchers.IO) { // MODIFIED
        db.ruleDao().getAllCareRules()
    }

    suspend fun addCareRule(plantId: String, type: TaskType, start: LocalDate, everyDays: Int?, everyMonths: Int?, note: String? = null) {
        db.ruleDao().upsert(CareRuleEntity(UUID.randomUUID().toString(), plantId, type, start, everyDays, everyMonths, note))
    }

    suspend fun updateCareRule(rule: CareRuleEntity) { // FIXED
        db.ruleDao().upsert(rule)
    }
    suspend fun deleteCareRule(rule: CareRuleEntity) = db.ruleDao().delete(rule)

    fun allTasksWithPlantInfo(): Flow<List<TaskWithPlantInfo>> = db.taskDao().observeAllWithPlantInfo()
    fun observeTasksForPlant(plantId: String): Flow<List<TaskWithPlantInfo>> = db.taskDao().observeTasksForPlant(plantId)
    suspend fun getLatestTaskForRule(ruleId: String): TaskInstanceEntity? = db.taskDao().getLatestTaskForRule(ruleId)
    suspend fun setTaskStatus(taskId: String, newStatus: TaskStatus) {
        db.taskDao().setStatus(taskId, newStatus)
        if (newStatus == TaskStatus.DONE) {
            val task = db.taskDao().getTask(taskId)
            if (task != null) {
                when (task.type) {
                    TaskType.FERTILIZE -> addFertilizerLog(task.plantId, LocalDate.now(), 0f, "Автоматически из задачи")
                    TaskType.HARVEST -> addHarvestLog(task.plantId, LocalDate.now(), 0f, "Автоматически из задачи")
                    else -> Unit
                }
            }
        }
    }
    fun getPendingTasksForGardens(gardenId: String): Flow<List<TaskInstanceEntity>> { // ADDED
        return getChildGardens(gardenId).flatMapLatest { childGardens ->
            val gardenIds = listOf(gardenId) + childGardens.map { it.id }
            db.taskDao().observePendingTasksForGardens(gardenIds)
        }
    }


    suspend fun addTask(plant: PlantEntity, type: TaskType, due: LocalDateTime, notes: String? = null) { // MODIFIED
        db.taskDao().upsert(
            TaskInstanceEntity(
                id = UUID.randomUUID().toString(),
                ruleId = null,
                plantId = plant.id,
                type = type,
                due = due,
                exact = true,
                status = TaskStatus.PENDING,
                notes = notes // ADDED
            )
        )
    }

    suspend fun createTaskFromRule(rule: CareRuleEntity, due: LocalDateTime) { // MODIFIED - removed notes param
        db.taskDao().upsert(
            TaskInstanceEntity(
                id = UUID.randomUUID().toString(),
                ruleId = rule.id,
                plantId = rule.plantId,
                type = rule.type,
                due = due,
                exact = true,
                status = TaskStatus.PENDING,
                notes = rule.note // FIXED - Use note from the rule
            )
        )
    }

    fun getRecentActivity(): Flow<List<RecentActivity>> {
        val fertilizerFlow = db.fertilizerLogDao().observeLatestWithPlant(5).map { it.map { logWithPlant -> RecentActivity.Fertilizer(logWithPlant) } }
        val harvestFlow = db.harvestLogDao().observeLatestWithPlant(5).map { it.map { logWithPlant -> RecentActivity.Harvest(logWithPlant) } }

        return combine(fertilizerFlow, harvestFlow) { fertilizers, harvests ->
            (fertilizers + harvests).sortedByDescending { it.date }.take(3)
        }
    }

    // --- Test Data Population ---
    suspend fun populateWithTestData() {
        if (db.gardenDao().getGardenByName("Участок") != null) {
            return // Test data already exists, do nothing
        }

        val allVarieties = referenceDao.getAllVarietiesList()
        if (allVarieties.isEmpty()) return

        // 1. Create Gardens and Plants
        val plotId = UUID.randomUUID().toString()
        db.gardenDao().upsert(GardenEntity(plotId, "Участок", 2000, 2000, 50, GardenType.PLOT, null,2))
        val plotPlants = allVarieties.shuffled().take(5).map { v ->
            PlantEntity(UUID.randomUUID().toString(), plotId, v.title, v.title, v.id,
                Random.nextInt(50, 1950).toFloat(), Random.nextInt(50, 1950).toFloat(),
                Random.nextInt(20, 50).toFloat(), LocalDate.now().minusDays(Random.nextLong(10, 365)))
        }
        plotPlants.forEach { db.plantDao().upsert(it) }

        val greenhouseId = UUID.randomUUID().toString()
        db.gardenDao().upsert(GardenEntity(greenhouseId, "Теплица", 300, 600, 50, GardenType.GREENHOUSE,plotId, 4))
        val greenhousePlants = (allVarieties.filter { it.cultureId == "tomato" }.shuffled().take(2) +
                allVarieties.filter { it.cultureId == "cucumber" }.shuffled().take(2))
            .mapIndexed { i, v ->
                PlantEntity(UUID.randomUUID().toString(), greenhouseId, v.title, v.title, v.id,
                    (100 + i * 100).toFloat(), 150f, 40f, LocalDate.now().minusDays(Random.nextLong(10, 90)))
            }
        greenhousePlants.forEach { db.plantDao().upsert(it) }

        val allTestPlants = plotPlants + greenhousePlants

        // 2. For EACH plant, create logs and rules
        val taskTypes = TaskType.values()
        allTestPlants.forEach {
            plant ->
            // 10 Fertilizer Logs
            repeat(10) {
                addFertilizerLog(
                    plantId = plant.id,
                    date = LocalDate.now().minusDays(Random.nextLong(1, 365)),
                    amountGrams = Random.nextFloat() * 20 + 5, // 5 to 25g
                    note = null
                )
            }

            // 10 Harvest Logs
            repeat(10) {
                addHarvestLog(
                    plantId = plant.id,
                    date = LocalDate.now().minusDays(Random.nextLong(1, 365)),
                    weightKg = Random.nextFloat() * 5 + 0.5f, // 0.5 to 5.5kg
                    note = null
                )
            }

            // 1 Care Rule
            addCareRule(
                plantId = plant.id,
                type = taskTypes.random(),
                start = LocalDate.now().minusWeeks(2),
                everyDays = Random.nextInt(3, 30),
                everyMonths = null
            )
        }
    }

    // --- Logs ---
    fun fertilizerLogs(plantId: String): Flow<List<FertilizerLogEntity>> = db.fertilizerLogDao().observe(plantId)
    fun observeAllFertilizerLogs(): Flow<List<FertilizerLogEntity>> = db.fertilizerLogDao().observeAll()
    suspend fun addFertilizerLog(plantId: String, date: LocalDate, amountGrams: Float, note: String?) {
        db.fertilizerLogDao().upsert(
            FertilizerLogEntity(UUID.randomUUID().toString(), plantId, date, amountGrams, note)
        )
    }
    suspend fun deleteFertilizerLog(item: FertilizerLogEntity) = db.fertilizerLogDao().delete(item)

    fun harvestLogs(plantId: String): Flow<List<HarvestLogEntity>> = db.harvestLogDao().observe(plantId)
    fun observeAllHarvests(): Flow<List<HarvestLogEntity>> = db.harvestLogDao().observeAll()
    suspend fun addHarvestLog(plantId: String, date: LocalDate, weightKg: Float, note: String?) {
        db.harvestLogDao().upsert(
            HarvestLogEntity(UUID.randomUUID().toString(), plantId, date, weightKg, note)
        )
    }
    suspend fun deleteHarvestLog(item: HarvestLogEntity) = db.harvestLogDao().delete(item)
}
