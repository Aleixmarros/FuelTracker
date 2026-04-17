package com.aleixmaro.fueltracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.aleixmaro.fueltracker.ui.viewmodel.RefuelViewModel
import com.aleixmaro.fueltracker.ui.util.formatDate
import com.aleixmaro.fueltracker.ui.util.formatLiters
import com.aleixmaro.fueltracker.ui.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RefuelViewModel,
    onAddRefuelClick: () -> Unit,
    onStatsClick: () -> Unit,
    onItemClick: (Long) -> Unit

) {
    val refuels = viewModel.refuelList.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fuel Tracker") },
                actions = {
                    IconButton(onClick = onStatsClick) {
                        Icon(Icons.Filled.Info, contentDescription = "Estadísticas")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRefuelClick) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn {
                items(refuels.value) { item ->
                    ListItem(
                        modifier = Modifier.clickable {
                        onItemClick(item.id)
                    },
                        headlineContent = { Text("${formatLiters(item.litros)} — ${formatMoney(item.dinero)}") },
                        supportingContent = {
                            Text("${item.kmCoche}Km • Fecha: ${formatDate(item.fecha)} • ${item.precioGasolina}€ ")
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
