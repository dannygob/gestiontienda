package com.gestiontienda.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gestiontienda.android.ui.settings.LanguageSelectionScreen
import com.gestiontienda.android.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Settings : Screen("settings")
    object LanguageSelection : Screen("language_selection")
    // ... other screens ...
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Settings.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToLanguageSelection = { navController.navigate(Screen.LanguageSelection.route) },
                onNavigateToStoreSettings = { /* TODO */ },
                onNavigateToBackupSettings = { /* TODO */ }
            )
        }

        composable(Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ... other screens ...
    }
} 
