package com.aleixmaro.fueltracker.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun YearWheelPicker(
    selectedYear: String,
    years: List<String>,
    onYearSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    initiallyCollapsed: Boolean = true
) {
    var isCollapsed by remember { mutableStateOf(initiallyCollapsed) }
    val listState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val itemWidth = 70.dp
    
    val density = LocalDensity.current
    var expandedWidth by remember { mutableStateOf(0.dp) }
    
    // Animación suave del ancho total del componente para que el layout padre reaccione bien
    Box(
        modifier = modifier
            .animateContentSize()
            .wrapContentWidth(Alignment.End)
    ) {
        Crossfade(
            targetState = isCollapsed,
            label = "WheelCollapse"
        ) { collapsed ->
            if (collapsed) {
                // Versión colapsada: Un botón discreto
                FilledIconButton(
                    onClick = { isCollapsed = false },
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Mostrar años", modifier = Modifier.size(20.dp))
                }
            } else {
                // Versión extendida: Píldora
                Surface(
                    modifier = Modifier
                        .height(48.dp)
                        .widthIn(min = 150.dp, max = 280.dp)
                        .onGloballyPositioned { 
                            expandedWidth = with(density) { it.size.width.toDp() }
                        },
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
                    tonalElevation = 6.dp,
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón para minimizar apuntando a la derecha (hacia donde se "guarda")
                        IconButton(onClick = { isCollapsed = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Minimizar", tint = MaterialTheme.colorScheme.primary)
                        }

                        FilterChip(
                            selected = selectedYear == "Todos",
                            onClick = { onYearSelected("Todos") },
                            label = { Text("Todos", fontSize = 11.sp) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            ),
                            border = null,
                            modifier = Modifier.padding(end = 4.dp).height(32.dp)
                        )

                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))

                        // Contenedor del carrete con cálculo de espacio optimizado
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            // Calculamos el espacio real disponible para el carrete
                            // Descontamos aproximadamente 100dp de los controles fijos de la izquierda
                            val availableWheelSpace = if (expandedWidth > 100.dp) expandedWidth - 100.dp else 120.dp
                            val horizontalPadding = (availableWheelSpace / 2) - (itemWidth / 2)

                            LazyRow(
                                state = listState,
                                flingBehavior = snapFlingBehavior,
                                contentPadding = PaddingValues(horizontal = if (horizontalPadding > 0.dp) horizontalPadding else 0.dp),
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                itemsIndexed(years, key = { _, year -> year }) { index, year ->
                                    val isSelected = selectedYear == year
                                    
                                    val itemOffset = remember(index) { derivedStateOf {
                                        val layoutInfo = listState.layoutInfo
                                        val visibleItemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
                                        if (visibleItemInfo != null) {
                                            val viewPortCenter = layoutInfo.viewportEndOffset / 2f
                                            val itemCenter = visibleItemInfo.offset + visibleItemInfo.size / 2f
                                            (itemCenter - viewPortCenter) / (layoutInfo.viewportEndOffset / 2f)
                                        } else 1f
                                    } }

                                    Box(
                                        modifier = Modifier
                                            .width(itemWidth)
                                            .graphicsLayer {
                                                val offset = itemOffset.value
                                                rotationY = offset * 45f
                                                val scale = 1f - (abs(offset) * 0.3f)
                                                scaleX = scale
                                                scaleY = scale
                                                alpha = 1f - (abs(offset) * 0.7f)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        TextButton(onClick = { onYearSelected(year) }, contentPadding = PaddingValues(0.dp)) {
                                            Text(
                                                text = year,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = if (isSelected) 16.sp else 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    LaunchedEffect(selectedYear) {
        if (selectedYear != "Todos") {
            val index = years.indexOf(selectedYear)
            if (index != -1) listState.animateScrollToItem(index)
        }
    }
}
