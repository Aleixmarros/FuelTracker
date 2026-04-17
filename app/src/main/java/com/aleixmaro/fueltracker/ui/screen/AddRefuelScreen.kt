package com.aleixmaro.fueltracker.ui.screen
import com.aleixmaro.fueltracker.data.local.entity.RefuelEntity

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aleixmaro.fueltracker.ui.viewmodel.RefuelViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRefuelScreen(
    viewModel: RefuelViewModel,
    onBack: () -> Unit
) {
    var dinero by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var litros by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    var fechaText by remember {
        mutableStateOf(selectedDate.format(dateFormatter))
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val editingRefuel by viewModel.editingRefuel.collectAsState()
    LaunchedEffect(editingRefuel) {
        editingRefuel?.let {
            dinero = it.dinero.toString()
            precio = it.precioGasolina.toString()
            litros = it.litros.toString()
            km = it.kmCoche.toString()
            selectedDate = Instant.ofEpochMilli(it.fecha)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            fechaText = selectedDate.format(dateFormatter)
        }
    }

    /** VALIDACIONES */
    val isFormValid =
        dinero.isNotBlank() &&
                precio.isNotBlank() &&
                litros.isNotBlank() &&
                km.isNotBlank() &&
                fechaText.isNotBlank()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Nuevo Repostaje",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(25.dp))

            // DINERO
            OutlinedTextField(
                value = dinero,
                onValueChange = { dinero = it },
                label = { Text("Dinero (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // PRECIO GASOLINA
            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio por litro (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // LITROS
            OutlinedTextField(
                value = litros,
                onValueChange = { litros = it },
                label = { Text("Litros") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // KM COCHE
            OutlinedTextField(
                value = km,
                onValueChange = { km = it },
                label = { Text("KM del coche") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // FECHA
            OutlinedTextField(
                value = fechaText,
                onValueChange = { fechaText = it },
                label = { Text("Fecha (DD/MM/YYYY)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Calendario")
                    }
                }
            )


            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {
                    val fechaMillis = parseDate(fechaText) ?: return@Button

                    val record = RefuelEntity(
                        id = editingRefuel?.id ?: 0,
                        dinero = dinero.toDouble(),
                        precioGasolina = precio.toDouble(),
                        litros = litros.toDouble(),
                        kmCoche = km.toInt(),
                        fecha = fechaMillis
                    )

                    if (editingRefuel == null) {
                        // CREAR
                        viewModel.addRecord(
                            dinero = record.dinero,
                            precioGasolina = record.precioGasolina,
                            litros = record.litros,
                            kmCoche = record.kmCoche,
                            fecha = record.fecha
                        )
                    } else {
                        // EDITAR
                        viewModel.updateRecord(record)
                    }

                    onBack()
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
    // ───── DIALOGO (FUERA DEL SCAFFOLD, DENTRO DEL COMPOSABLE) ─────
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        fechaText = selectedDate.format(dateFormatter)
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/** Convertir DD/MM/YYYY → millis */
fun parseDate(input: String): Long? {
    return try {
        val parts = input.split("/")
        if (parts.size != 3) return null

        val day = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val year = parts[2].toInt()

        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, day)
            set(java.util.Calendar.MONTH, month)
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        cal.timeInMillis
    } catch (_: Exception) {
        null
    }
}
