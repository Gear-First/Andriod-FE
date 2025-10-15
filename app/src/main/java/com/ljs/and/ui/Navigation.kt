package com.ljs.and.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.ljs.and.ui.receiving.ReceivingInspectionScreen
import com.ljs.and.ui.receiving.ReceivingScreen
import com.ljs.and.ui.releasing.ReleasingScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Receiving : Screen("receiving")
    object Releasing : Screen("releasing")
    object Inventory : Screen("inventory")
    object More : Screen("more")
    object BarcodeScan : Screen("barcode_scan")
    object ManualInput : Screen("manual_input")
    object ReceivingInspection : Screen("receiving_inspection/{supplier}/{date}") {
        fun createRoute(supplier: String, date: String) = "receiving_inspection/$supplier/$date"
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
            if (currentRoute != Screen.BarcodeScan.route && currentRoute != Screen.ManualInput.route) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationGraph(navController = navController)
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavItems.forEach { (screen, details) ->
            val (title, icon) = details
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
private fun NavigationGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.Receiving.route) { ReceivingScreen(navController = navController) }
        composable(Screen.Releasing.route) { ReleasingScreen(navController = navController) }
        composable(Screen.Inventory.route) { InventoryScreen() }
        composable(Screen.More.route) { MoreScreen() }

        composable(Screen.BarcodeScan.route) { BarcodeScanScreen(navController = navController) }
        composable(Screen.ManualInput.route) { ManualInputScreen(navController = navController) }

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
    }
}

// Placeholder screens
@Composable fun HomeScreen() { Box(modifier = Modifier.fillMaxSize()) { Text(text = "홈 화면") } }
@Composable fun InventoryScreen() { Box(modifier = Modifier.fillMaxSize()) { Text(text = "재고 화면") } }
@Composable fun MoreScreen() { Box(modifier = Modifier.fillMaxSize()) { Text(text = "더보기 화면") } }
