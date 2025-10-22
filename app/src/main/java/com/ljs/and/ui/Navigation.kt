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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.ljs.and.ui.common.BarcodeScanScreen
import com.ljs.and.ui.common.ManualInputScreen
import com.ljs.and.ui.home.HomeScreen
import com.ljs.and.ui.inventory.InventoryRequestFormScreen
import com.ljs.and.ui.inventory.InventoryScreen
import com.ljs.and.ui.more.MoreScreen
import com.ljs.and.ui.receiving.ReceivingInspectionScreen
import com.ljs.and.ui.receiving.ReceivingScreen
import com.ljs.and.ui.receiving.ReceivingViewModel
import com.ljs.and.ui.releasing.ReleasingPickingScreen
import com.ljs.and.ui.releasing.ReleasingScreen
import com.ljs.and.ui.search.SearchResultScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Receiving : Screen("receiving") // This now represents the nested graph
    object ReceivingHome: Screen("receiving_home") // Start destination of the nested graph
    object Releasing : Screen("releasing")
    object Inventory : Screen("inventory")
    object More : Screen("more")
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
                Screen.InventoryRequestForm.route,
                Screen.ReceivingInspection.route // Hide bottom bar on inspection screen
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
            val isSelected = currentDestination?.hierarchy?.any { it.route?.startsWith(screen.route) ?: false } == true
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.Black,
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
        
        navigation(startDestination = Screen.ReceivingHome.route, route = Screen.Receiving.route) {
            composable(Screen.ReceivingHome.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Receiving.route) }
                val viewModel: ReceivingViewModel = viewModel(parentEntry)
                ReceivingScreen(navController = navController, viewModel = viewModel)
            }
            composable(
                route = Screen.ReceivingInspection.route,
                 arguments = listOf(
                    navArgument("supplier") { type = NavType.StringType },
                    navArgument("date") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Receiving.route) }
                val viewModel: ReceivingViewModel = viewModel(parentEntry)
                ReceivingInspectionScreen(navController = navController, viewModel = viewModel)
            }
        }
        
        composable(Screen.Releasing.route) { ReleasingScreen(navController = navController) }
        composable(Screen.Inventory.route) { InventoryScreen(navController = navController) }
        composable(Screen.More.route) { MoreScreen(navController = navController) }
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
