package com.ai.notepad.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{entryId}") {
        fun createRoute(entryId: Long) = "detail/$entryId"
    }
    object Monthly : Screen("monthly/{month}") {
        fun createRoute(month: String) = "monthly/$month"
    }
    object Settings : Screen("settings")
}
