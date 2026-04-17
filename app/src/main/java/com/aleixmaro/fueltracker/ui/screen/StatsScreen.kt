package com.aleixmaro.fueltracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.aleixmaro.fueltracker.ui.util.formatDate
import com.aleixmaro.fueltracker.ui.viewmodel.RefuelViewModel
import com.aleixmaro.fueltracker.ui.viewmodel.StatsViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.aleixmaro.fueltracker.ui.theme.getConsumptionColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    statsViewModel: StatsViewModel,
    onBack: () -> Unit
) {
    val averages by statsViewModel.globalAverages.collectAsState()
    val totals by statsViewModel.globalTotals.collectAsState()
    val refuelStats by statsViewModel.refuelStats.collectAsState()

    // Estados del dropdown
    var expanded by remember { mutableStateOf(false) }
    var selectedYear = remember { mutableStateOf("Todos") }


    // Lista de años del 2025 al 2035
    val years = (2025..2035).map { it.toString() }

    // Filtrado de intervalos por año
    val filteredStats = if (selectedYear.value == "Todos") {
        refuelStats
    } else {
        val targetYear = selectedYear.value.toInt()
        refuelStats.filter { stat ->
            val fromYear = stat.fromDate.toLocalYear()
            val toYear = stat.toDate.toLocalYear()
            fromYear <= targetYear && toYear >= targetYear
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            /** =======================
             *  RESUMEN MEDIO GLOBAL
             *  ======================= */
            item {
                StatsSection(title = "Resumen medio global") {
                    StatRow(
                        "Consumo medio",
                        "%.2f L/100km".format(averages.avgConsumption)
                    )
                    StatRow(
                        "Kilómetros al mes",
                        "%.2f km".format(averages.avgKmPerMonth)
                    )
                    StatRow(
                        "Gasto mensual",
                        "%.2f €".format(averages.avgEuroPerMonth)
                    )
                }
            }

            item { Divider() }

            /** =======================
             *  RESUMEN DE TOTALES
             *  ======================= */
            item {
                StatsSection(title = "Totales") {
                    StatRow("Kilómetros recorridos", "${totals.totalKm} km")
                    StatRow("€ invertidos", "%.2f €".format(totals.totalEuros))
                    StatRow("Litros consumidos", "%.2f L".format(totals.totalLiters))
                    StatRow("Total de días", "${totals.totalDays} días")
                    StatRow(
                        "Precio medio del litro",
                        "%.3f €/L".format(totals.avgLiterPrice)
                    )
                }
            }

            item { Divider() }

            // =======================
// DETALLE POR INTERVALO CON FILTRO
// =======================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Detalle por intervalo",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(selectedYear.value)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = {
                                    selectedYear.value = "Todos"
                                    expanded = false
                                }
                            )
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year) },
                                    onClick = {
                                        selectedYear.value = year
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

// Mostrar intervalos filtrados o mensaje si no hay
            if (filteredStats.isEmpty()) {
                item {
                    Text(
                        "No hay intervalos para este año",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(filteredStats) { stat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = getConsumptionColor(stat.consumption)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "${formatDate(stat.fromDate)} → ${formatDate(stat.toDate)}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("${stat.km} km • ${stat.days} días")

                            Text(
                                "Litros: %.2f L — € %.2f".format(
                                    stat.liters,
                                    stat.euros
                                )
                            )

                            Text(
                                "Consumo: %.2f L/100km".format(stat.consumption)
                            )

                            Text(
                                "KM/mes: %.2f — Gasto/mes: %.2f €".format(
                                    stat.kmPerMonth,
                                    stat.euroPerMonth
                                )
                            )
                        }
                    }
                }
            }
        }
        }
}
    // Extensión para convertir timestamp Long a año
    fun Long.toLocalYear(): Int {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = this
        return calendar.get(java.util.Calendar.YEAR)
    }

    @Composable
    fun StatsSection(
        title: String,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }

    @Composable
    fun StatRow(label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }

