package com.example.gardenapp.data.db

import androidx.room.Embedded

// Plain Old Java Objects for combined queries

data class TaskWithPlantInfo(
    @Embedded val task: TaskInstanceEntity,
    val plantName: String
)

data class FertilizerLogWithPlant(
    @Embedded val log: FertilizerLogEntity,
    val plantName: String
)

data class HarvestLogWithPlant(
    @Embedded val log: HarvestLogEntity,
    val plantName: String
)
