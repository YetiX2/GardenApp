package com.example.gardenapp.data.db

import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class TestDataGenerator @Inject constructor(
    private val db: GardenDatabase,
    private val referenceDao: ReferenceDao
) {
    suspend fun seed() {
        val gardenDao = db.gardenDao()
        val plantDao = db.plantDao()
        val ruleDao = db.ruleDao()
        val fertilizerLogDao = db.fertilizerLogDao()
        val harvestLogDao = db.harvestLogDao()

        if (gardenDao.getGardenByName("Участок") != null) {
            return // Test data already exists, do nothing
        }

        val allVarieties = referenceDao.getAllVarietiesList()
        if (allVarieties.isEmpty()) return

        // 1. Create Gardens and Plants
        val plotId = UUID.randomUUID().toString()
        gardenDao.upsert(GardenEntity(plotId, "Участок", 2000, 2000, 50, GardenType.PLOT, null, 2))
        val plotPlants = allVarieties.shuffled().take(5).map { v ->
            PlantEntity(
                UUID.randomUUID().toString(), plotId, v.title, v.title, v.id,
                Random.nextInt(50, 1950).toFloat(), Random.nextInt(50, 1950).toFloat(),
                Random.nextInt(20, 50).toFloat(), LocalDate.now().minusDays(Random.nextLong(10, 365))
            )
        }
        plotPlants.forEach { plantDao.upsert(it) }

        val greenhouseId = UUID.randomUUID().toString()
        gardenDao.upsert(GardenEntity(greenhouseId, "Теплица", 600, 300, 50, GardenType.GREENHOUSE, plotId, 4))
        val greenhousePlants = (allVarieties.filter { it.cultureId == "tomato" }.shuffled().take(2) +
                allVarieties.filter { it.cultureId == "cucumber" }.shuffled().take(2))
            .mapIndexed { i, v ->
                PlantEntity(
                    UUID.randomUUID().toString(), greenhouseId, v.title, v.title, v.id,
                    (100 + i * 100).toFloat(), 150f, 40f, LocalDate.now().minusDays(Random.nextLong(10, 90))
                )
            }
        greenhousePlants.forEach { plantDao.upsert(it) }

        // ADDED: House
        val houseId = UUID.randomUUID().toString()
        gardenDao.upsert(GardenEntity(houseId, "Дом", 600, 600, 50, GardenType.BUILDING, plotId, x=1300, y=1300, climateZone=null))

        // ADDED: Mayskiy Bed
        val mayskiyBedId = UUID.randomUUID().toString()
        gardenDao.upsert(GardenEntity(mayskiyBedId, "Грядка", 400, 100, 50, GardenType.BED, plotId, x = 100, y = 800, climateZone = null))
        val mayskiyBedPlants = (1..20).map {
            val v = allVarieties.random()
            PlantEntity(
                UUID.randomUUID().toString(), mayskiyBedId, v.title, v.title, v.id,
                Random.nextInt(10, 400 - 10).toFloat(), // x RELATIVE to the bed
                Random.nextInt(10, 100 - 10).toFloat(),
                Random.nextInt(5, 10).toFloat(),
                LocalDate.now().minusDays(Random.nextLong(1, 45))
            )
        }
        mayskiyBedPlants.forEach { plantDao.upsert(it) }

        // ADDED: Apple Tree
        val appleVariety = allVarieties.find { it.title.contains("Антоновка", ignoreCase = true) }
        if (appleVariety != null) {
            plantDao.upsert(
                PlantEntity(
                    UUID.randomUUID().toString(), plotId, "Антоновка", appleVariety.title, appleVariety.id,
                    800f, 800f, // coordinates
                    120f, // large radius
                    LocalDate.now().minusYears(3)
                )
            )
        }

        val allTestPlants = plotPlants + greenhousePlants

        // 2. For EACH plant, create logs and rules
        val taskTypes = TaskType.values()
        allTestPlants.forEach { plant ->
            repeat(10) {
                fertilizerLogDao.upsert(
                    FertilizerLogEntity(
                        UUID.randomUUID().toString(), plant.id, LocalDate.now().minusDays(Random.nextLong(1, 365)),
                        Random.nextFloat() * 20 + 5, null
                    )
                )
            }
            repeat(10) {
                harvestLogDao.upsert(
                    HarvestLogEntity(
                        UUID.randomUUID().toString(), plant.id, LocalDate.now().minusDays(Random.nextLong(1, 365)),
                        Random.nextFloat() * 5 + 0.5f, null
                    )
                )
            }
            ruleDao.upsert(
                CareRuleEntity(
                    UUID.randomUUID().toString(), plant.id, taskTypes.random(), LocalDate.now().minusWeeks(2),
                    Random.nextInt(3, 30), null
                )
            )
        }
    }
}
