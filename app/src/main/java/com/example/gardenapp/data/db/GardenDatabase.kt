package com.example.gardenapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        GardenEntity::class, PlantEntity::class, CareRuleEntity::class, TaskInstanceEntity::class, 
        FertilizerLogEntity::class, HarvestLogEntity::class,
        ReferenceGroupEntity::class, ReferenceCultureEntity::class, ReferenceVarietyEntity::class, 
        ReferenceTagEntity::class, ReferenceRegionEntity::class, ReferenceCultivationEntity::class
    ],
    version = 1, // BUMPED VERSION
    exportSchema = true, // Set to true to export schema for auto-migration
    autoMigrations = [
        //AutoMigration(from = 0, to = 0) // ADDED THIS
    ]
)
@TypeConverters(Converters::class)
abstract class GardenDatabase : RoomDatabase() {
    abstract fun gardenDao(): GardenDao
    abstract fun plantDao(): PlantDao
    abstract fun ruleDao(): RuleDao
    abstract fun taskDao(): TaskDao
    abstract fun fertilizerLogDao(): FertilizerLogDao
    abstract fun harvestLogDao(): HarvestLogDao
    abstract fun referenceDao(): ReferenceDao

    // COMPANION OBJECT REMOVED
}
