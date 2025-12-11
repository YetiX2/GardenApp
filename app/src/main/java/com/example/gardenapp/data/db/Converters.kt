package com.example.gardenapp.data.db

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun fromEpochDay(v: Long?): LocalDate? = v?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(d: LocalDate?): Long? = d?.toEpochDay()

    @TypeConverter
    fun fromEpochMillis(v: Long?): LocalDateTime? =
        v?.let { LocalDateTime.ofEpochSecond(it / 1000, ((it % 1000) * 1_000_000).toInt(), java.time.ZoneOffset.UTC) }

    @TypeConverter
    fun toEpochMillis(dt: LocalDateTime?): Long? =
        dt?.toInstant(java.time.ZoneOffset.UTC)?.toEpochMilli()
}
