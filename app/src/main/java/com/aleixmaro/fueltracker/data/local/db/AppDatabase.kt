package com.aleixmaro.fueltracker.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aleixmaro.fueltracker.data.local.dao.RefuelDao
import com.aleixmaro.fueltracker.data.local.entity.RefuelEntity

@Database(
    entities = [RefuelEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun refuelDao(): RefuelDao
}
