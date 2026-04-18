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

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.ui.unit.dp

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
    val refuels = viewModel.refuelList.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                },
                actions = {
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
            FloatingActionButton(
                onClick = onAddRefuelClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(refuels.value) { item ->
                    ListItem(
                        modifier = Modifier.clickable {
                            onItemClick(item.id)
                        },
                        headlineContent = {
                            Text(
                                "${formatLiters(item.litros)} — ${formatMoney(item.dinero)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                        },
                        supportingContent = {
                            Text(
                                "${item.kmCoche}Km • Fecha: ${formatDate(item.fecha)} • ${item.precioGasolina}€/L",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}
