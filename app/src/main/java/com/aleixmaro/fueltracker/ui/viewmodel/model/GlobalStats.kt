package com.aleixmaro.fueltracker.ui.viewmodel.model

data class GlobalStats(
    val consumoMedio: Double,
    val kmMes: Double,
    val gastoMes: Double,
    val kmTotales: Int,
    val litrosTotales: Double,
    val dineroTotal: Double,
    val diasTotales: Int,
    val precioMedioLitro: Double
)
