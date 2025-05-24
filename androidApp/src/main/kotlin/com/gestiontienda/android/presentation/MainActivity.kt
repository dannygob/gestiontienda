@Composable
fun MainContent(
    navController: NavHostController = rememberNavController(),
) {
    NavGraph(
        navController = navController,
        startDestination = Screen.Home.route
    )

    HomeScreen(
        onNavigateToProducts = { navController.navigate(Screen.Products.route) },
        onNavigateToSales = { navController.navigate(Screen.Sales.route) },
        onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
        onNavigateToCustomers = { navController.navigate(Screen.Customers.route) },
        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
    )
} 
