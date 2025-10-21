package com.ljs.and.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ljs.and.ui.common.BarcodeScanScreen
import com.ljs.and.ui.common.ManualInputScreen
import com.ljs.and.ui.home.HomeScreen
import com.ljs.and.ui.inventory.InventoryRequestFormScreen
import com.ljs.and.ui.inventory.InventoryScreen
import com.ljs.and.ui.more.MoreScreen
import com.ljs.and.ui.pending.PendingItemsScreen
import com.ljs.and.ui.receiving.ReceivingInspectionScreen
import com.ljs.and.ui.receiving.ReceivingScreen
import com.ljs.and.ui.releasing.ReleasingPickingScreen
import com.ljs.and.ui.releasing.ReleasingScreen
import com.ljs.and.ui.search.SearchResultScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Receiving : Screen("receiving")
    object Releasing : Screen("releasing")
    object Inventory : Screen("inventory")
    object More : Screen("more")
    object PendingItems : Screen("pending_items")
    object InventoryRequestForm : Screen("inventory_request")
    object BarcodeScan : Screen("barcodescan/{type}") {
        fun createRoute(type: String) = "barcodescan/$type"
    }
    object ManualInput : Screen("manual_input/{flowType}") {
        fun createRoute(flowType: String) = "manual_input/$flowType"
    }
    object ReceivingInspection : Screen("receiving_inspection/{supplier}/{date}") {
        fun createRoute(supplier: String, date: String) = "receiving_inspection/$supplier/$date"
    }
    object ReleasingPicking : Screen("releasing_picking/{customer}/{date}") {
        fun createRoute(customer: String, date: String) = "releasing_picking/$customer/$date"
    }
    object SearchResult : Screen("search_result/{flowType}") {
        fun createRoute(flowType: String, query: String? = null): String {
            var route = "search_result/$flowType"
            if (query != null) {
                route += "?query=$query"
            }
            return route
        }
    }
}

private val bottomNavItems = listOf(
    Screen.Home to Pair("홈", Icons.Filled.Home),
    Screen.Receiving to Pair("입고", Icons.Filled.Add),
    Screen.Releasing to Pair("출고", Icons.Filled.ExitToApp),
    Screen.Inventory to Pair("재고", Icons.Filled.Search),
    Screen.More to Pair("더보기", Icons.Filled.MoreVert)
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val showBottomBar = currentRoute !in listOf(
                Screen.BarcodeScan.route,
                Screen.ManualInput.route,
                Screen.InventoryRequestForm.route
            )
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationGraph(navController = navController)
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar(
        modifier = Modifier.height(120.dp).shadow(elevation = 8.dp),
        containerColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavItems.forEach { (screen, details) ->
            val (title, icon) = details
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = screen.route != Screen.Home.route
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = Color(0xFF007BFF),
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.Black,
//                    selectedTextColor = Color(0xFF007BFF),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun NavigationGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController = navController) } 
        composable(Screen.Receiving.route) { ReceivingScreen(navController = navController) }
        composable(Screen.Releasing.route) { ReleasingScreen(navController = navController) }
        composable(Screen.Inventory.route) { InventoryScreen(navController = navController) }
        composable(Screen.More.route) { MoreScreen(navController = navController) }
        composable(Screen.PendingItems.route) { PendingItemsScreen(navController = navController) }
        composable(Screen.InventoryRequestForm.route) { InventoryRequestFormScreen(navController = navController) }

        composable(
            route = Screen.BarcodeScan.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            BarcodeScanScreen(
                navController = navController,
                flowType = backStackEntry.arguments?.getString("type") ?: "receiving"
            )
        }
        composable(
            route = Screen.ManualInput.route,
            arguments = listOf(navArgument("flowType") { type = NavType.StringType })
        ) { backStackEntry ->
            ManualInputScreen(
                navController = navController,
                flowType = backStackEntry.arguments?.getString("flowType") ?: "receiving"
            )
        }

        composable(
            route = Screen.ReceivingInspection.route,
            arguments = listOf(
                navArgument("supplier") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ReceivingInspectionScreen(
                navController = navController,
                supplier = backStackEntry.arguments?.getString("supplier") ?: "",
                date = backStackEntry.arguments?.getString("date") ?: ""
            )
        }

        composable(
            route = Screen.ReleasingPicking.route,
            arguments = listOf(
                navArgument("customer") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ReleasingPickingScreen(
                navController = navController,
                customer = backStackEntry.arguments?.getString("customer") ?: "",
                date = backStackEntry.arguments?.getString("date") ?: ""
            )
        }

        composable(
            route = "${Screen.SearchResult.route}?query={query}",
            arguments = listOf(
                navArgument("flowType") { type = NavType.StringType },
                navArgument("query") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            SearchResultScreen(
                navController = navController,
                flowType = backStackEntry.arguments?.getString("flowType") ?: "receiving",
                initialQuery = backStackEntry.arguments?.getString("query") ?: ""
            )
        }
    }
}
