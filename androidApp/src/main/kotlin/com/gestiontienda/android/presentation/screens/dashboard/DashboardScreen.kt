package com.gestiontienda.android.presentation.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gestiontienda.android.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión Tienda") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            DashboardGrid(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardGrid(navController: NavController) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DashboardItem(
                    icon = Icons.Default.Inventory,
                    label = "Inventario",
                    onClick = { navController.navigate(Screen.Inventory.route) }
                )
                DashboardItem(
                    icon = Icons.Default.ShoppingCart,
                    label = "Ventas",
                    onClick = { navController.navigate(Screen.Sales.route) }
                )
                DashboardItem(
                    icon = Icons.Default.Assignment,
                    label = "Pedidos",
                    onClick = { navController.navigate(Screen.Orders.route) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DashboardItem(
                    icon = Icons.Default.People,
                    label = "Clientes",
                    onClick = { navController.navigate(Screen.Clients.route) }
                )
                DashboardItem(
                    icon = Icons.Default.Business,
                    label = "Proveedores",
                    onClick = { navController.navigate(Screen.Providers.route) }
                )
                DashboardItem(
                    icon = Icons.Default.BarChart,
                    label = "Reportes",
                    onClick = { navController.navigate(Screen.Reports.route) }
                )
            }
        }
    }
}

@Composable
private fun DashboardItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
} 
