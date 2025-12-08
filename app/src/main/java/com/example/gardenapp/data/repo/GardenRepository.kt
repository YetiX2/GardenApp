package com.example.gardenapp.data.repo

import com.example.gardenapp.data.db.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class GardenRepository @Inject constructor(
    private val db: GardenDatabase,
    private val referenceDao: ReferenceDao // Added dependency
) {
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

    fun pendingTasks(): Flow<List<TaskWithPlantInfo>> = db.taskDao().observePendingWithPlantInfo()

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

    // --- Test Data Population ---
    suspend fun populateWithTestData() {
        // Check if test data already exists
        if (db.gardenDao().getGardenByName("Участок") != null) {
            return // Test data already exists, do nothing
        }

        val allVarieties = referenceDao.getAllVarietiesList()
        if (allVarieties.isEmpty()) return // Can't do anything without reference data

        // 1. Create the 20x20m plot
        val plotId = UUID.randomUUID().toString()
        val plot = GardenEntity(plotId, "Участок", 2000, 2000, 50, 3)
        db.gardenDao().upsert(plot)

        // Add 5 random plants to the plot
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
        val greenhouse = GardenEntity(greenhouseId, "Теплица", 300, 600, 50, null)
        db.gardenDao().upsert(greenhouse)
        
        val tomatoVarieties = allVarieties.filter { it.cultureId == "tomato" }.shuffled().take(2)
        val cucumberVarieties = allVarieties.filter { it.cultureId == "cucumber" }.shuffled().take(2)
        val greenhouseVarieties = tomatoVarieties + cucumberVarieties

        val greenhousePlants = greenhouseVarieties.mapIndexed { i, variety ->
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
        greenhousePlants.forEach { db.plantDao().upsert(it) }

        // 3. Create test tasks
        val beerPlantId = UUID.randomUUID().toString()
        val beerPlant = PlantEntity(beerPlantId, plotId, "Пиво", null, null, 200f, 200f, 20f, LocalDate.now())
        db.plantDao().upsert(beerPlant)

        val tasks = listOf(
            TaskInstanceEntity(UUID.randomUUID().toString(), null, beerPlantId, TaskType.OTHER, LocalDateTime.now(), true, TaskStatus.PENDING),
            TaskInstanceEntity(UUID.randomUUID().toString(), null, plotPlants[0].id, TaskType.WATER, LocalDateTime.now().plusHours(1), true, TaskStatus.PENDING),
            TaskInstanceEntity(UUID.randomUUID().toString(), null, greenhousePlants[0].id, TaskType.FERTILIZE, LocalDateTime.now().plusHours(2), true, TaskStatus.PENDING)
        )
        tasks.forEach { db.taskDao().upsert(it) }
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
