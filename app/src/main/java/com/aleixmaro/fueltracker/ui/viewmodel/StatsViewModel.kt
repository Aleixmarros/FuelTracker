package com.aleixmaro.fueltracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleixmaro.fueltracker.data.local.entity.GlobalAverages
import com.aleixmaro.fueltracker.data.local.entity.GlobalTotals
import com.aleixmaro.fueltracker.data.local.entity.RefuelEntity
import com.aleixmaro.fueltracker.data.local.entity.RefuelStats
import com.aleixmaro.fueltracker.data.repository.RefuelRepository
import com.aleixmaro.fueltracker.ui.viewmodel.model.GlobalStats
import kotlinx.coroutines.flow.*
import kotlin.math.max

class StatsViewModel(
    repository: RefuelRepository
) : ViewModel() {

    private val refuelsFlow: StateFlow<List<RefuelEntity>> =
        repository.getAllRefuels()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    /** ───────── DETALLE POR INTERVALO ───────── */
    val refuelStats: StateFlow<List<RefuelStats>> =
        refuelsFlow
            .map { calculateStats(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    /** ───────── RESUMEN MEDIO ───────── */
    val globalAverages: StateFlow<GlobalAverages> =
        refuelStats
            .map { calculateAverages(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                GlobalAverages(0.0, 0.0, 0.0)
            )

    /** ───────── TOTALES ───────── */
    val globalTotals: StateFlow<GlobalTotals> =
        combine(refuelStats, refuelsFlow) { stats, refuels ->
            calculateTotalsFromStats(stats, refuels)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            GlobalTotals(0, 0.0, 0.0, 0, 0.0)
        )



    // ─────────────────────────────
    // LÓGICA DE NEGOCIO
    // ─────────────────────────────
    private fun calculateGlobalStats(
        list: List<RefuelEntity>
    ): GlobalStats {

        if (list.size < 2) return emptyStats()

        val sorted = list.sortedBy { it.fecha }

        var totalKm = 0
        var totalLitros = 0.0
        var totalDinero = 0.0
        var totalDias = 0

        for (i in 0 until sorted.size - 1) {
            val actual = sorted[i]
            val siguiente = sorted[i + 1]

            val km = siguiente.kmCoche - actual.kmCoche
            if (km <= 0) continue

            val dias = max(
                1,
                ((siguiente.fecha - actual.fecha) / MILLIS_PER_DAY).toInt()
            )

            totalKm += km
            totalLitros += actual.litros
            totalDinero += actual.dinero
            totalDias += dias
        }

        val consumoMedio = (totalLitros / totalKm) * 100
        val kmMes = (totalKm.toDouble() / totalDias) * 30.44
        val gastoMes = (totalDinero / totalDias) * 30.44

        val precioMedioLitro =
            sorted.dropLast(1).sumOf { it.precioGasolina } / (sorted.size - 1)

        return GlobalStats(
            consumoMedio = consumoMedio,
            kmMes = kmMes,
            gastoMes = gastoMes,
            kmTotales = totalKm,
            litrosTotales = totalLitros,
            dineroTotal = totalDinero,
            diasTotales = totalDias,
            precioMedioLitro = precioMedioLitro
        )
    }
    fun calculateStats(refuels: List<RefuelEntity>): List<RefuelStats> {
        if (refuels.size < 2) return emptyList()

        val sorted = refuels.sortedBy { it.fecha }
        val result = mutableListOf<RefuelStats>()

        for (i in 1 until sorted.size) {
            val prev = sorted[i - 1]
            val curr = sorted[i]

            val km = curr.kmCoche - prev.kmCoche
            if (km <= 0) continue

            val days = max(
                1,
                ((curr.fecha - prev.fecha) / MILLIS_PER_DAY).toInt()
            )

            val consumption = (prev.litros / km) * 100
            val kmPerMonth = (km.toDouble() / days) * 30.44
            val euroPerMonth = (prev.dinero / days) * 30.44

            result.add(
                RefuelStats(
                    fromDate = prev.fecha,
                    toDate = curr.fecha - MILLIS_PER_DAY,
                    km = km,
                    days = days,
                    liters = prev.litros,
                    euros = prev.dinero,
                    consumption = consumption,
                    kmPerMonth = kmPerMonth,
                    euroPerMonth = euroPerMonth
                )
            )
        }

        return result.reversed()
    }


    fun calculateAverages(stats: List<RefuelStats>): GlobalAverages {

        if (stats.isEmpty()) {
            return GlobalAverages(0.0, 0.0, 0.0)
        }

        val totalKm = stats.sumOf { it.km }
        val totalLiters = stats.sumOf { it.liters }
        val totalEuros = stats.sumOf { it.euros }
        val totalDays = stats.sumOf { it.days }

        val avgConsumption =
            if (totalKm > 0) (totalLiters / totalKm) * 100 else 0.0

        val avgKmPerMonth =
            if (totalDays > 0) (totalKm.toDouble() / totalDays) * 30.44 else 0.0

        val avgEuroPerMonth =
            if (totalDays > 0) (totalEuros / totalDays) * 30.44 else 0.0

        return GlobalAverages(
            avgConsumption = avgConsumption,
            avgKmPerMonth = avgKmPerMonth,
            avgEuroPerMonth = avgEuroPerMonth
        )
    }

    private fun calculateTotalsFromStats(
        stats: List<RefuelStats>,
        refuels: List<RefuelEntity>
    ): GlobalTotals {

        if (stats.isEmpty() || refuels.isEmpty()) {
            return GlobalTotals(0, 0.0, 0.0, 0, 0.0)
        }

        val totalKm = stats.sumOf { it.km }
        val totalLiters = stats.sumOf { it.liters }
        val totalDays = stats.sumOf { it.days }

        // 💡 AQUÍ entra el último repostaje
        val totalEuros = refuels.sumOf { it.dinero }

        val avgLiterPrice =
            if (refuels.sumOf { it.litros } > 0)
                refuels.sumOf { it.dinero } / refuels.sumOf { it.litros }
            else 0.0

        return GlobalTotals(
            totalKm = totalKm,
            totalEuros = totalEuros,
            totalLiters = totalLiters,
            totalDays = totalDays,
            avgLiterPrice = avgLiterPrice
        )
    }



    private fun emptyStats() = GlobalStats(
        consumoMedio = 0.0,
        kmMes = 0.0,
        gastoMes = 0.0,
        kmTotales = 0,
        litrosTotales = 0.0,
        dineroTotal = 0.0,
        diasTotales = 0,
        precioMedioLitro = 0.0
    )

    companion object {
        private const val MILLIS_PER_DAY = 1000L * 60 * 60 * 24
    }
}
