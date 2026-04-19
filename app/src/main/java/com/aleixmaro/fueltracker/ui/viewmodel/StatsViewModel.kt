package com.aleixmaro.fueltracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleixmaro.fueltracker.data.local.entity.GlobalAverages
import com.aleixmaro.fueltracker.data.local.entity.GlobalTotals
import com.aleixmaro.fueltracker.data.local.entity.RefuelEntity
import com.aleixmaro.fueltracker.data.local.entity.RefuelStats
import com.aleixmaro.fueltracker.data.repository.RefuelRepository
import com.aleixmaro.fueltracker.ui.util.toLocalYear
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

    private val _selectedYear = MutableStateFlow("Todos")
    val selectedYear: StateFlow<String> = _selectedYear.asStateFlow()

    fun updateYearFilter(year: String) {
        _selectedYear.value = year
    }

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
        combine(refuelStats, _selectedYear) { stats, year ->
            val filtered = if (year == "Todos") stats
            else {
                val target = year.toInt()
                stats.filter { target in it.fromDate.toLocalYear()..it.toDate.toLocalYear() }
            }
            calculateAverages(filtered)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            GlobalAverages(0.0, 0.0, 0.0)
        )

    /** ───────── TOTALES ───────── */
    val globalTotals: StateFlow<GlobalTotals> =
        combine(refuelStats, refuelsFlow, _selectedYear) { stats, refuels, year ->
            val (fStats, fRefuels) = if (year == "Todos") {
                stats to refuels
            } else {
                val target = year.toInt()
                val fs = stats.filter { target in it.fromDate.toLocalYear()..it.toDate.toLocalYear() }
                val fr = refuels.filter { it.fecha.toLocalYear() == target }
                fs to fr
            }
            calculateTotalsFromStats(fStats, fRefuels)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            GlobalTotals(0, 0.0, 0.0, 0, 0.0)
        )



    // ─────────────────────────────
    // LÓGICA DE NEGOCIO
    // ─────────────────────────────
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


    companion object {
        private const val MILLIS_PER_DAY = 1000L * 60 * 60 * 24
    }
}
