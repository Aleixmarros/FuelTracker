package com.aleixmaro.fueltracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aleixmaro.fueltracker.data.repository.RefuelRepository

class RefuelViewModelFactory(
    private val repository: RefuelRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(RefuelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RefuelViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
