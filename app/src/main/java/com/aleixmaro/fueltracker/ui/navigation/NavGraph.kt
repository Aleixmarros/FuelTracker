package com.aleixmaro.fueltracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aleixmaro.fueltracker.ui.screen.AddRefuelScreen
import com.aleixmaro.fueltracker.ui.screen.HomeScreen
import com.aleixmaro.fueltracker.ui.screen.StatsScreen
import com.aleixmaro.fueltracker.ui.viewmodel.RefuelViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.aleixmaro.fueltracker.ui.viewmodel.StatsViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: RefuelViewModel,
    statsViewModel: StatsViewModel

) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onAddRefuelClick = {
                    navController.navigate(Routes.ADD_REFUEL)
                },
                onStatsClick = {
                    navController.navigate(Routes.STATS)
                },
                onItemClick = { id ->
                    navController.navigate("edit_refuel/$id")
                }
            )
        }

        composable(Routes.ADD_REFUEL) {
            AddRefuelScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.STATS) {
            StatsScreen(
                statsViewModel = statsViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.EDIT_REFUEL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: return@composable

            LaunchedEffect(id) {
                viewModel.loadRefuelForEdit(id)
            }

            AddRefuelScreen(
                viewModel = viewModel,
                onBack = {
                    viewModel.clearEditing()
                    navController.popBackStack()
                }
            )
        }

    }
}
