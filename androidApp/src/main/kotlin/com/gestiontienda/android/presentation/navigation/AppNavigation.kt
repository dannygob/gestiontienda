package com.gestiontienda.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gestiontienda.android.presentation.screens.dashboard.DashboardScreen
import com.gestiontienda.android.presentation.screens.inventory.InventoryScreen
import com.gestiontienda.android.presentation.screens.sales.SalesScreen
import com.gestiontienda.android.presentation.screens.orders.OrdersScreen
import com.gestiontienda.android.presentation.screens.clients.ClientsScreen
import com.gestiontienda.android.presentation.screens.providers.ProvidersScreen
import com.gestiontienda.android.presentation.screens.reports.ReportsScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Inventory : Screen("inventory")
    object Sales : Screen("sales")
    object Orders : Screen("orders")
    object Clients : Screen("clients")
    object Providers : Screen("providers")
    object Reports : Screen("reports")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.Inventory.route) {
            InventoryScreen(navController)
        }
        composable(Screen.Sales.route) {
            SalesScreen(navController)
        }
        composable(Screen.Orders.route) {
            OrdersScreen(navController)
        }
        composable(Screen.Clients.route) {
            ClientsScreen(navController)
        }
        composable(Screen.Providers.route) {
            ProvidersScreen(navController)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController)
        }
    }
} 
