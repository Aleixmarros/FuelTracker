package com.aleixmaro.fueltracker.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aleixmaro.fueltracker.data.local.entity.RefuelStats
import com.aleixmaro.fueltracker.ui.theme.getConsumptionColor
import com.aleixmaro.fueltracker.ui.util.formatDate
import com.aleixmaro.fueltracker.ui.util.toLocalYear
import com.aleixmaro.fueltracker.ui.viewmodel.StatsViewModel

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
    val selectedYear by statsViewModel.selectedYear.collectAsState()

    // Lista de años del 2025 al 2125
    val years = (2025..2125).map { it.toString() }

    // Filtrado de intervalos por año (para la lista)
    val filteredStats = if (selectedYear == "Todos") {
        refuelStats
    } else {
        val targetYear = selectedYear.toInt()
        refuelStats.filter { stat ->
            val fromYear = stat.fromDate.toLocalYear()
            val toYear = stat.toDate.toLocalYear()
            targetYear in fromYear..toYear
        }
    }

    val yearSuffix = if (selectedYear == "Todos") "" else " $selectedYear"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh 
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /** =======================
             *  RESUMEN MEDIO GLOBAL
             *  ======================= */
            item {
                StatsCard(
                    title = "Resumen medio global$yearSuffix",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    gradient = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                        )
                    )
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatGridItem("Consumo medio", "%.2f L/100".format(averages.avgConsumption), Icons.Default.LocalGasStation)
                        StatGridItem("Km/mes", "%.0f km".format(averages.avgKmPerMonth), Icons.Default.DirectionsCar)
                        StatGridItem("Gasto/mes", "%.2f €".format(averages.avgEuroPerMonth), Icons.Default.EuroSymbol)
                    }
                }
            }

            /** =======================
             *  RESUMEN DE TOTALES
             *  ======================= */
            item {
                StatsCard(
                    title = "Resumen histórico$yearSuffix",
                    icon = Icons.Default.CalendarMonth,
                    gradient = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatGridItem("Km recorridos", "${totals.totalKm} km", Icons.Default.DirectionsCar)
                            StatGridItem("Invertido", "%.2f €".format(totals.totalEuros), Icons.Default.EuroSymbol)
                            StatGridItem("Litros", "%.1f L".format(totals.totalLiters), Icons.Default.LocalGasStation)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            StatGridItem("Total días", "${totals.totalDays} d", Icons.Default.CalendarMonth)
                            StatGridItem("Precio/L", "%.3f €".format(totals.avgLiterPrice), Icons.Default.LocalGasStation,"/",Icons.Default.EuroSymbol)
                        }
                    }
                }
            }

            /** =======================
             *  DETALLE POR INTERVALO CON FILTRO
             *  ======================= */
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Detalle por intervalo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Box {
                        AssistChip(
                            onClick = { expanded = true },
                            label = { Text(selectedYear) },
                            trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = {
                                    statsViewModel.updateYearFilter("Todos")
                                    expanded = false
                                }
                            )
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year) },
                                    onClick = {
                                        statsViewModel.updateYearFilter(year)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

// Mostrar intervalos filtrados o mensaje si no hay
            if (filteredStats.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Sin datos para $selectedYear",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredStats) { stat ->
                    IntervalCard(stat)
                }
            }
        }
    }
}

/** =======================
 *  TARJETA DE RESUMEN
 *  ======================= */
@Composable
fun StatsCard(
    title: String,
    icon: ImageVector,
    gradient: Brush,
    content: @Composable RowScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                content = content
            )
        }
    }
}

/** =======================
 *  CELDA DE DATO
 *  ======================= */
@Composable
fun RowScope.StatGridItem(
    label: String,
    value: String,
    icon: ImageVector,
    separator: String? = null,
    secondaryIcon: ImageVector? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            if (separator != null) {
                Text(
                    text = separator,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
            if (secondaryIcon != null) {
                Icon(
                    secondaryIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** =======================
 *  TARJETA DE INTERVALO
 *  ======================= */
@Composable
fun IntervalCard(stat: RefuelStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = getConsumptionColor(stat.consumption).copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${formatDate(stat.fromDate)} - ${formatDate(stat.toDate)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Badge(
                    containerColor = getConsumptionColor(stat.consumption),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text("%.2f L/100".format(stat.consumption), modifier = Modifier.padding(4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${stat.km} km", fontWeight = FontWeight.Bold)
                    Text("${stat.days} días", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("%.2f €".format(stat.euros), fontWeight = FontWeight.Bold)
                    Text("%.2f L".format(stat.liters), style = MaterialTheme.typography.bodySmall)
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "KM/mes: %.0f".format(stat.kmPerMonth),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Gasto/mes: %.2f €".format(stat.euroPerMonth),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

