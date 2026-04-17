package com.aleixmaro.fueltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.aleixmaro.fueltracker.data.local.db.DatabaseProvider
import com.aleixmaro.fueltracker.data.repository.RefuelRepository
import com.aleixmaro.fueltracker.ui.navigation.AppNavGraph
import com.aleixmaro.fueltracker.ui.theme.FuelTrackerTheme
import com.aleixmaro.fueltracker.ui.viewmodel.RefuelViewModel
import com.aleixmaro.fueltracker.ui.viewmodel.StatsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Crear repositorio usando el DatabaseProvider singleton
        val repository = RefuelRepository(DatabaseProvider.getDatabase(this).refuelDao())

        setContent {
            FuelTrackerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    val navController = rememberNavController()

                    // -------- RefuelViewModel --------
                    val refuelViewModel: RefuelViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                if (modelClass.isAssignableFrom(RefuelViewModel::class.java)) {
                                    @Suppress("UNCHECKED_CAST")
                                    return RefuelViewModel(repository) as T
                                }
                                throw IllegalArgumentException("Unknown ViewModel class")
                            }
                        }
                    )

                    // -------- StatsViewModel --------
                    val statsViewModel: StatsViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
                                    @Suppress("UNCHECKED_CAST")
                                    return StatsViewModel(repository) as T
                                }
                                throw IllegalArgumentException("Unknown ViewModel class")
                            }
                        }
                    )

                    // -------- NavGraph --------
                    AppNavGraph(
                        navController = navController,
                        viewModel = refuelViewModel,
                        statsViewModel = statsViewModel
                    )
                }
            }
        }
    }
}
