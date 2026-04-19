package com.aleixmaro.fueltracker.ui.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aleixmaro.fueltracker.data.local.entity.RefuelEntity
import com.aleixmaro.fueltracker.ui.theme.getPriceColor
import com.aleixmaro.fueltracker.ui.util.formatDate
import com.aleixmaro.fueltracker.ui.util.formatLiters
import com.aleixmaro.fueltracker.ui.util.formatMoney
import com.aleixmaro.fueltracker.ui.util.toLocalYear
import com.aleixmaro.fueltracker.ui.viewmodel.RefuelViewModel

enum class SortOption(val label: String) {
    FECHA("Fecha"),
    PRECIO("Precio/L"),
    LITROS("Litros"),
    EUROS("€ Invertidos")
}

enum class SortOrder { ASC, DESC }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    viewModel: RefuelViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onAddRefuelClick: () -> Unit,
    onStatsClick: () -> Unit,
    onItemClick: (Long) -> Unit

) {
    val refuels by viewModel.refuelList.collectAsState()

    // --- ESTADO DE ORDENACIÓN ---
    val sortOption = remember { mutableStateOf(SortOption.FECHA) }
    val sortOrder = remember { mutableStateOf(SortOrder.DESC) }
    val showSortMenu = remember { mutableStateOf(false) }

    // --- ESTADO DE FILTRADO ---
    val showFilterSheet = remember { mutableStateOf(false) }
    val filterYear = remember { mutableStateOf("Todos") }
    
    // Rangos (se inicializarán con los datos)
    val priceFilter = remember { mutableStateOf(0f..2.5f) }
    val euroFilter = remember { mutableStateOf(0f..200f) }
    val literFilter = remember { mutableStateOf(0f..100f) }
    val kmFilter = remember { mutableStateOf(0f..500000f) }
    val dateFilter = remember { mutableStateOf(0f..System.currentTimeMillis().toFloat()) }

    // Límites absolutos para los sliders
    val currentRefuels = refuels
    val absMinPrice = remember(currentRefuels) { currentRefuels?.minOfOrNull { it.precioGasolina.toFloat() } ?: 0f }
    val absMaxPrice = remember(currentRefuels) { currentRefuels?.maxOfOrNull { it.precioGasolina.toFloat() } ?: 2.5f }
    val absMinEuros = remember(currentRefuels) { currentRefuels?.minOfOrNull { it.dinero.toFloat() } ?: 0f }
    val absMaxEuros = remember(currentRefuels) { currentRefuels?.maxOfOrNull { it.dinero.toFloat() } ?: 200f }
    val absMinLiters = remember(currentRefuels) { currentRefuels?.minOfOrNull { it.litros.toFloat() } ?: 0f }
    val absMaxLiters = remember(currentRefuels) { currentRefuels?.maxOfOrNull { it.litros.toFloat() } ?: 100f }
    val absMinKm = remember(currentRefuels) { currentRefuels?.minOfOrNull { it.kmCoche.toFloat() } ?: 0f }
    val absMaxKm = remember(currentRefuels) { currentRefuels?.maxOfOrNull { it.kmCoche.toFloat() } ?: 500000f }
    val absMinDate = remember(currentRefuels) { currentRefuels?.minOfOrNull { it.fecha.toFloat() } ?: 0f }
    val absMaxDate = remember(currentRefuels) { currentRefuels?.maxOfOrNull { it.fecha.toFloat() } ?: System.currentTimeMillis().toFloat() }

    // Inicializar filtros con límites absolutos la primera vez
    val initialFiltersSet = remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(currentRefuels) {
        if (currentRefuels != null && currentRefuels.isNotEmpty() && !initialFiltersSet.value) {
            priceFilter.value = absMinPrice..absMaxPrice
            euroFilter.value = absMinEuros..absMaxEuros
            literFilter.value = absMinLiters..absMaxLiters
            kmFilter.value = absMinKm..absMaxKm
            dateFilter.value = absMinDate..absMaxDate
            initialFiltersSet.value = true
        }
    }

    // --- LÓGICA DE FILTRADO Y ORDENACIÓN ---
    val filteredAndSortedRefuels = remember(
        currentRefuels, sortOption.value, sortOrder.value, filterYear.value, 
        priceFilter.value, euroFilter.value, literFilter.value, kmFilter.value, dateFilter.value
    ) {
        currentRefuels?.filter { item ->
            val matchesYear = filterYear.value == "Todos" || item.fecha.toLocalYear().toString() == filterYear.value
            val matchesPrice = item.precioGasolina.toFloat() in priceFilter.value
            val matchesEuros = item.dinero.toFloat() in euroFilter.value
            val matchesLiters = item.litros.toFloat() in literFilter.value
            val matchesKm = item.kmCoche.toFloat() in kmFilter.value
            val matchesDate = item.fecha.toFloat() in dateFilter.value
            
            matchesYear && matchesPrice && matchesEuros && matchesLiters && matchesKm && matchesDate
        }?.let { list ->
            when (sortOption.value) {
                SortOption.FECHA -> if (sortOrder.value == SortOrder.ASC) list.sortedBy { it.fecha } else list.sortedByDescending { it.fecha }
                SortOption.PRECIO -> if (sortOrder.value == SortOrder.ASC) list.sortedBy { it.precioGasolina } else list.sortedByDescending { it.precioGasolina }
                SortOption.LITROS -> if (sortOrder.value == SortOrder.ASC) list.sortedBy { it.litros } else list.sortedByDescending { it.litros }
                SortOption.EUROS -> if (sortOrder.value == SortOrder.ASC) list.sortedBy { it.dinero } else list.sortedByDescending { it.dinero }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        with(sharedTransitionScope) {
                            AppLogo(
                                modifier = Modifier
                                    .sharedElement(
                                        rememberSharedContentState(key = "app_logo"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        boundsTransform = { _, _ -> tween(800) }
                                    )
                                    .size(32.dp),
                                backgroundColor = MaterialTheme.colorScheme.primary,
                                iconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Fuel Tracker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // ICONO ORDENAR
                    Box {
                        IconButton(onClick = { showSortMenu.value = true }) {
                            Icon(
                                Icons.Default.SwapVert, 
                                contentDescription = "Ordenar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu.value,
                            onDismissRequest = { showSortMenu.value = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(option.label)
                                            if (sortOption.value == option) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    if (sortOrder.value == SortOrder.ASC) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (sortOption.value == option) {
                                            sortOrder.value = if (sortOrder.value == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
                                        } else {
                                            sortOption.value = option
                                            sortOrder.value = SortOrder.DESC
                                        }
                                        showSortMenu.value = false
                                    }
                                )
                            }
                        }
                    }

                    // ICONO ESTADÍSTICAS
                    IconButton(onClick = onStatsClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Estadísticas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp), // Compensar el padding del sistema para que quede a la izquierda
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // FAB DE FILTRO (Izquierda)
                FloatingActionButton(
                    onClick = { showFilterSheet.value = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filtrar")
                }

                // FAB DE AÑADIR (Derecha)
                FloatingActionButton(
                    onClick = onAddRefuelClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (filteredAndSortedRefuels == null) {
                // Estado de carga inicial
            } else if (filteredAndSortedRefuels.isEmpty()) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            } else {
                val avgPrice = remember(currentRefuels) {
                    if (currentRefuels?.isNotEmpty() == true) {
                        currentRefuels.sumOf { it.precioGasolina } / currentRefuels.size
                    } else 0.0
                }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredAndSortedRefuels, key = { it.id }) { item ->
                        RefuelItem(
                            item = item,
                            avgPrice = avgPrice,
                            onEditClick = onItemClick
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // --- BOTTOM SHEET DE FILTROS ---
    if (showFilterSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet.value = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    "Filtros avanzados", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                
                // AÑO
                Text("Filtrar por Año", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    val years = remember(currentRefuels) {
                        listOf("Todos") + (currentRefuels?.map { it.fecha.toLocalYear().toString() }?.distinct()?.sortedDescending() ?: emptyList())
                    }
                    years.forEach { year ->
                        FilterChip(
                            selected = filterYear.value == year,
                            onClick = { filterYear.value = year },
                            label = { Text(year) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                // FILTROS DE RANGO (Scroll Horizontal)
                Text("Ajustar rangos", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    FilterRangeCard("Precio/L", priceFilter.value, absMinPrice..absMaxPrice, { priceFilter.value = it }, { "%.2f€".format(it) })
                    FilterRangeCard("Litros", literFilter.value, absMinLiters..absMaxLiters, { literFilter.value = it }, { "%.1fL".format(it) })
                    FilterRangeCard("€ Invertidos", euroFilter.value, absMinEuros..absMaxEuros, { euroFilter.value = it }, { "%.0f€".format(it) })
                    FilterRangeCard("Kilómetros", kmFilter.value, absMinKm..absMaxKm, { kmFilter.value = it }, { "%.0f".format(it) })
                    FilterRangeCard("Fecha", dateFilter.value, absMinDate..absMaxDate, { dateFilter.value = it }, { formatDate(it.toLong()) })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = {
                        filterYear.value = "Todos"
                        priceFilter.value = absMinPrice..absMaxPrice
                        euroFilter.value = absMinEuros..absMaxEuros
                        literFilter.value = absMinLiters..absMaxLiters
                        kmFilter.value = absMinKm..absMaxKm
                        dateFilter.value = absMinDate..absMaxDate
                    },
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                ) {
                    Text("Restablecer todos los filtros")
                }
            }
        }
    }
}

@Composable
fun FilterRangeCard(
    title: String,
    value: ClosedFloatingPointRange<Float>,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    formatValue: (Float) -> String
) {
    val safeRange = remember(range) {
        if (range.start >= range.endInclusive) {
            (range.start - 0.1f)..(range.start + 0.1f)
        } else range
    }
    
    val safeValue = remember(value, safeRange) {
        val start = value.start.coerceIn(safeRange.start, safeRange.endInclusive)
        val end = value.endInclusive.coerceIn(safeRange.start, safeRange.endInclusive)
        if (start > end) start..start else start..end
    }

    Card(
        modifier = Modifier
            .width(260.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatValue(safeValue.start), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(formatValue(safeValue.endInclusive), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            RangeSlider(
                value = safeValue,
                onValueChange = onValueChange,
                valueRange = safeRange,
                enabled = range.start < range.endInclusive,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun RefuelItem(
    item: RefuelEntity,
    avgPrice: Double,
    onEditClick: (Long) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    val backgroundColor = getPriceColor(item.precioGasolina, avgPrice).copy(alpha = 0.12f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = { expanded.value = !expanded.value }
    ) {
        Column {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = {
                    Text(
                        "${formatLiters(item.litros)} — ${formatMoney(item.dinero)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge(
                            containerColor = getPriceColor(item.precioGasolina, avgPrice),
                            contentColor = Color.White
                        ) {
                            Text(
                                "${item.precioGasolina} €/L",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (expanded.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

                if (expanded.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "${item.kmCoche} Km • ${formatDate(item.fecha)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onEditClick(item.id) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo(
            modifier = Modifier.size(120.dp),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            iconColor = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Sin repostajes todavía",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "¡Empieza a ahorrar hoy! Pulsa el botón '+' de abajo para registrar tu primer repostaje.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
