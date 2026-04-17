package com.aleixmaro.fueltracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refuel_records")
data class RefuelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dinero: Double,
    val precioGasolina: Double,
    val litros: Double,
    val fecha: Long,
    val kmCoche: Int
)
data class RefuelStats(
    val fromDate: Long,
    val toDate: Long,
    val km: Int,
    val days: Int,
    val liters: Double,
    val euros: Double,
    val consumption: Double,
    val kmPerMonth: Double,
    val euroPerMonth: Double
)
data class GlobalAverages(
    val avgConsumption: Double,
    val avgKmPerMonth: Double,
    val avgEuroPerMonth: Double
)
data class GlobalTotals(
    val totalKm: Int,
    val totalEuros: Double,
    val totalLiters: Double,
    val totalDays: Int,
    val avgLiterPrice: Double
)
