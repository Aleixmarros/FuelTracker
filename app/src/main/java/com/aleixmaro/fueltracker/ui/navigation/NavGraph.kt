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
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.aleixmaro.fueltracker.ui.viewmodel.StatsViewModel

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import com.aleixmaro.fueltracker.ui.screen.SplashScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: RefuelViewModel,
    statsViewModel: StatsViewModel

) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH
        ) {

            composable(Routes.SPLASH) {
                SplashScreen(
                    animatedVisibilityScope = this,
                    onAnimationFinished = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    onAddRefuelClick = {
                        viewModel.clearEditing()
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

            composable(
                route = Routes.ADD_REFUEL,
                enterTransition = {
                    slideIntoContainer(
                        towards = androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = androidx.compose.animation.core.tween(500)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = androidx.compose.animation.core.tween(500)
                    )
                }
            ) {
                AddRefuelScreen(
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Routes.STATS,
                enterTransition = {
                    slideIntoContainer(
                        towards = androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = androidx.compose.animation.core.tween(500)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = androidx.compose.animation.core.tween(500)
                    )
                }
            ) {
                StatsScreen(
                    statsViewModel = statsViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Routes.EDIT_REFUEL,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
                enterTransition = {
                    slideIntoContainer(
                        towards = androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = androidx.compose.animation.core.tween(500)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = androidx.compose.animation.core.tween(500)
                    )
                }
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: return@composable

                LaunchedEffect(id) {
                    viewModel.loadRefuelForEdit(id)
                }

                DisposableEffect(Unit) {
                    onDispose {
                        viewModel.clearEditing()
                    }
                }

                AddRefuelScreen(
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

        }
    }
}
