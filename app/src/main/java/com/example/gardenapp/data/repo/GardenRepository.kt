package com.example.gardenapp.data.repo

import com.example.gardenapp.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class GardenRepository @Inject constructor(
    private val db: GardenDatabase,
    private val referenceDao: ReferenceDao 
) {
    fun gardens(): Flow<List<GardenEntity>> = db.gardenDao().observeGardens()
    suspend fun getGarden(id: String): GardenEntity? = db.gardenDao().getGarden(id)
    
    suspend fun upsertGarden(garden: GardenEntity) {
        db.gardenDao().upsert(garden)
    }

    suspend fun deleteGarden(g: GardenEntity) = db.gardenDao().delete(g)

    fun plants(gardenId: String): Flow<List<PlantEntity>> = db.plantDao().observeByGarden(gardenId)
    fun observeAllPlants(): Flow<List<PlantEntity>> = db.plantDao().observeAllPlants()
    suspend fun upsertPlant(p: PlantEntity) = db.plantDao().upsert(p)
    suspend fun deletePlant(p: PlantEntity) = db.plantDao().delete(p)

    fun careRules(plantId: String): Flow<List<CareRuleEntity>> = db.ruleDao().observeRulesForPlant(plantId)
    suspend fun addCareRule(plantId: String, type: TaskType, start: LocalDate, everyDays: Int?, everyMonths: Int?) {
        db.ruleDao().upsert(CareRuleEntity(UUID.randomUUID().toString(), plantId, type, start, everyDays, everyMonths))
    }
    suspend fun deleteCareRule(rule: CareRuleEntity) = db.ruleDao().delete(rule)

    fun allTasksWithPlantInfo(): Flow<List<TaskWithPlantInfo>> = db.taskDao().observeAllWithPlantInfo()
    suspend fun setTaskStatus(taskId: String, newStatus: TaskStatus) = db.taskDao().setStatus(taskId, newStatus)
    suspend fun addTask(plant: PlantEntity, type: TaskType, due: LocalDateTime) {
        db.taskDao().upsert(
            TaskInstanceEntity(
                id = UUID.randomUUID().toString(),
                ruleId = null,
                plantId = plant.id,
                type = type,
                due = due,
                exact = true,
                status = TaskStatus.PENDING
            )
        )
    }

    suspend fun createTaskFromRule(rule: CareRuleEntity, due: LocalDateTime) {
        db.taskDao().upsert(
            TaskInstanceEntity(
                id = UUID.randomUUID().toString(),
                ruleId = rule.id,
                plantId = rule.plantId,
                type = rule.type,
                due = due,
                exact = true,
                status = TaskStatus.PENDING
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

        // 1. Create the 20x20m plot
        val plotId = UUID.randomUUID().toString()
        val plot = GardenEntity(plotId, "Участок", 2000, 2000, 50, 2)
        db.gardenDao().upsert(plot)

        val plotPlants = allVarieties.shuffled().take(5).map {
            PlantEntity(
                id = UUID.randomUUID().toString(),
                gardenId = plotId,
                title = it.title,
                variety = it.title,
                varietyId = it.id,
                x = Random.nextInt(50, 1950).toFloat(),
                y = Random.nextInt(50, 1950).toFloat(),
                radius = Random.nextInt(20, 50).toFloat(),
                plantedAt = LocalDate.now().minusDays(Random.nextLong(10, 365))
            )
        }
        plotPlants.forEach { db.plantDao().upsert(it) }

        // 2. Create the 3x6m greenhouse
        val greenhouseId = UUID.randomUUID().toString()
        val greenhouse = GardenEntity(greenhouseId, "Теплица", 300, 600, 50, 4)
        db.gardenDao().upsert(greenhouse)
        
        val tomatoVarieties = allVarieties.filter { it.cultureId == "tomato" }.shuffled().take(2)
        val cucumberVarieties = allVarieties.filter { it.cultureId == "cucumber" }.shuffled().take(2)
        val greenhousePlants = tomatoVarieties + cucumberVarieties
        val greenhouseVarieties = tomatoVarieties + cucumberVarieties
        val greenhousePlantsEntities = greenhouseVarieties.mapIndexed { i, variety ->
             PlantEntity(
                id = UUID.randomUUID().toString(),
                gardenId = greenhouseId,
                title = variety.title,
                variety = variety.title,
                varietyId = variety.id,
                x = (100 + i * 100).toFloat(),
                y = 150f,
                radius = 40f,
                plantedAt = LocalDate.now().minusDays(Random.nextLong(10, 90))
            )
        }
        greenhousePlantsEntities.forEach { db.plantDao().upsert(it) }

        // 3. Create test tasks
        val beerPlantId = UUID.randomUUID().toString()
        val beerPlant = PlantEntity(beerPlantId, plotId, "Пиво", null, null, 200f, 200f, 20f, LocalDate.now())
        db.plantDao().upsert(beerPlant)

        val tasks = listOf(
            TaskInstanceEntity(UUID.randomUUID().toString(), null, beerPlantId, TaskType.OTHER, LocalDateTime.now(), true, TaskStatus.PENDING),
            TaskInstanceEntity(UUID.randomUUID().toString(), null, plotPlants[0].id, TaskType.WATER, LocalDateTime.now().plusHours(1), true, TaskStatus.PENDING),
            TaskInstanceEntity(UUID.randomUUID().toString(), null, greenhousePlantsEntities[0].id, TaskType.FERTILIZE, LocalDateTime.now().plusHours(2), true, TaskStatus.PENDING)
        )
        tasks.forEach { db.taskDao().upsert(it) }

        // 4. Create test log entries
        addFertilizerLog(plotPlants[1].id, LocalDate.now().minusDays(5), 15f, "Первая подкормка")
        addHarvestLog(greenhousePlantsEntities[0].id, LocalDate.now().minusDays(2), 2.5f, "Собрали первые огурцы")
        addFertilizerLog(greenhousePlantsEntities[1].id, LocalDate.now().minusDays(1), 10f, null)
        addHarvestLog(plotPlants[0].id, LocalDate.now(), 5.1f, "Клубника пошла!")
    }

    // --- Logs ---
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
