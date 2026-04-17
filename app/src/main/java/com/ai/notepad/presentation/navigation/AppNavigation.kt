package com.ai.notepad.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ai.notepad.presentation.detail.DiaryDetailScreen
import com.ai.notepad.presentation.home.HomeScreen
import com.ai.notepad.presentation.monthly.MonthlySummaryScreen
import com.ai.notepad.presentation.settings.SettingsScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDetail = { entryId ->
                    navController.navigate(Screen.Detail.createRoute(entryId))
                },
                onNavigateToMonthly = { month ->
                    navController.navigate(Screen.Monthly.createRoute(month))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            DiaryDetailScreen(
                entryId = entryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Monthly.route,
            arguments = listOf(navArgument("month") { type = NavType.StringType })
        ) { backStackEntry ->
            val month = backStackEntry.arguments?.getString("month") ?: ""
            MonthlySummaryScreen(
                month = month,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
