package com.aleixmaro.fueltracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleixmaro.fueltracker.data.local.entity.RefuelEntity
import com.aleixmaro.fueltracker.data.repository.RefuelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RefuelViewModel(private val repository: RefuelRepository) : ViewModel() {

    // StateFlow para exponer la lista de repostajes de forma reactiva
    private val _refuelList = MutableStateFlow<List<RefuelEntity>>(emptyList())
    val refuelList: StateFlow<List<RefuelEntity>> =
        repository.getAllRefuels()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
    private val _editingRefuel = MutableStateFlow<RefuelEntity?>(null)
    val editingRefuel: StateFlow<RefuelEntity?> = _editingRefuel

    /** Insertar un nuevo repostaje */
    fun addRecord(
        dinero: Double,
        precioGasolina: Double,
        litros: Double,
        fecha: Long,
        kmCoche: Int
    ) {
        viewModelScope.launch {
            val record = RefuelEntity(
                dinero = dinero,
                precioGasolina = precioGasolina,
                litros = litros,
                fecha = fecha,
                kmCoche = kmCoche
            )

            repository.insert(record)
        }
    }

    /** Borrar un registro */
    fun deleteRecord(record: RefuelEntity) {
        viewModelScope.launch {
            repository.delete(record)
        }
    }
    fun loadRefuelForEdit(id: Long) {
        viewModelScope.launch {
            _editingRefuel.value = repository.getById(id)
        }
    }

    fun updateRecord(record: RefuelEntity) {
        viewModelScope.launch {
            repository.update(record)
        }
    }

    fun clearEditing() {
        _editingRefuel.value = null
    }

}
