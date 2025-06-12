package com.gestiontienda.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gestiontienda.android.presentation.auth.AuthScreen
import com.gestiontienda.android.presentation.screens.home.HomeScreen
import com.gestiontienda.android.presentation.screens.inventory.InventoryScreen
import com.gestiontienda.android.presentation.screens.sales.SalesScreen
import com.gestiontienda.android.presentation.screens.backup.BackupScreen
import com.gestiontienda.android.presentation.screens.alerts.AlertScreen
import com.gestiontienda.android.presentation.screens.statistics.StatisticsScreen
import com.gestiontienda.android.presentation.screens.customers.CustomersScreen
import com.gestiontienda.android.presentation.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Sales : Screen("sales")
    object Inventory : Screen("inventory")
    object Backup : Screen("backup")
    object Alerts : Screen("alerts")
    object Statistics : Screen("statistics")
    object Customers : Screen("customers")
    object CustomerDetail : Screen("customer/{customerId}") {
        fun createRoute(customerId: Long) = "customer/$customerId"
    }

    object Settings : Screen("settings")
    object LanguageSelection : Screen("language_selection")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Auth.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSales = { navController.navigate(Screen.Sales.route) },
                onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                onNavigateToBackup = { navController.navigate(Screen.Backup.route) },
                onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                onNavigateToCustomers = { navController.navigate(Screen.Customers.route) }
            )
        }

        composable(Screen.Sales.route) {
            SalesScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Inventory.route) {
            InventoryScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Backup.route) {
            BackupScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Alerts.route) {
            AlertScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Customers.route) {
            CustomersScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToCustomerDetail = { customerId ->
                    navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                }
            )
        }

        composable(Screen.CustomerDetail.route) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId")?.toLongOrNull()
                ?: return@composable
            CustomerDetailScreen(
                customerId = customerId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBackup = { navController.navigate(Screen.Backup.route) }
// TODO: Add navigation to LanguageSelectionScreen from SettingsScreen
            )
        }

        composable(Screen.LanguageSelection.route) {
            // TODO: Replace with actual LanguageSelectionScreen composable
            // LanguageSelectionScreen(onNavigateBack = { navController.navigateUp() })
        }
    }
} 
