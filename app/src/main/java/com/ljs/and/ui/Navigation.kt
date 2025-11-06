package com.ljs.and.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Inventory
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
import com.ljs.and.ui.inventory.InventoryViewModel
import com.ljs.and.ui.login.LoginScreen
import com.ljs.and.ui.login.SplashScreen
import com.ljs.and.ui.more.MoreScreen
import com.ljs.and.ui.receiving.ReceivingInspectionScreen
import com.ljs.and.ui.receiving.ReceivingScreen
import com.ljs.and.ui.receiving.ReceivingViewModel
import com.ljs.and.ui.receiving.ReceivingViewModelFactory
import com.ljs.and.ui.releasing.ReleasingPickingScreen
import com.ljs.and.ui.releasing.ReleasingScreen
import com.ljs.and.ui.releasing.ReleasingViewModel
import com.ljs.and.ui.releasing.ReleasingViewModelFactory
import com.ljs.and.ui.search.SearchResultScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Receiving : Screen("receiving")
    object ReceivingHome: Screen("receiving_home")
    object Releasing : Screen("releasing")
    object ReleasingHome : Screen("releasing_home")
    object Inventory : Screen("inventory")
    object InventoryHome : Screen("inventory_home?filter={filter}") {
        fun createRoute(filter: String? = null): String {
            return if (filter != null) "inventory_home?filter=$filter" else "inventory_home"
        }
    }
    object More : Screen("more")
    object InventoryRequestForm : Screen("inventory_request")
    object BarcodeScan : Screen("barcodescan/{flowType}?noteId={noteId}&lineId={lineId}&currentQty={currentQty}&orderedQty={orderedQty}&lineRemark={lineRemark}") {
        fun createRoute(flowType: String, noteId: Long = -1L, lineId: Long = -1L, currentQty: Int = 0, orderedQty: Int = 0, lineRemark: String? = null) = "barcodescan/$flowType?noteId=$noteId&lineId=$lineId&currentQty=$currentQty&orderedQty=$orderedQty&lineRemark=$lineRemark"
    }
    object ManualInput : Screen("manual_input/{flowType}?noteId={noteId}&lineId={lineId}&currentQty={currentQty}&orderedQty={orderedQty}&lineRemark={lineRemark}") {
        fun createRoute(flowType: String, noteId: Long, lineId: Long, currentQty: Int, orderedQty: Int, lineRemark: String?) = "manual_input/$flowType?noteId=$noteId&lineId=$lineId&currentQty=$currentQty&orderedQty=$orderedQty&lineRemark=$lineRemark"
    }
    object ReceivingInspection : Screen("receiving_inspection/{isReadOnly}") {
        fun createRoute(isReadOnly: Boolean) = "receiving_inspection/$isReadOnly"
    }
    object ReleasingPicking : Screen("releasing_picking/{noteId}?isReadOnly={isReadOnly}") {
        fun createRoute(noteId: Long, isReadOnly: Boolean) = "releasing_picking/$noteId?isReadOnly=$isReadOnly"
    }

    object SearchResult : Screen("search_result/{flowType}?query={query}") {
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
    Screen.Receiving to Pair("입고", Icons.Filled.Inventory),
    Screen.Releasing to Pair("출고", Icons.Filled.LocalShipping),
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
                Screen.Splash.route,
                Screen.Login.route,
                Screen.BarcodeScan.route,
                Screen.ManualInput.route,
                Screen.InventoryRequestForm.route,
                Screen.ReceivingInspection.route, // Hide bottom bar on inspection screen
                Screen.ReleasingPicking.route
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
            val isSelected = currentDestination?.hierarchy?.any { it.route?.startsWith(screen.route.substringBefore("?")) ?: false } == true
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { inclusive = false } // 핵심 수정
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
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController = navController) }

        navigation(startDestination = Screen.ReceivingHome.route, route = Screen.Receiving.route) {
            composable(Screen.ReceivingHome.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Receiving.route) }
                val viewModel: ReceivingViewModel = viewModel(parentEntry, factory = ReceivingViewModelFactory())
                ReceivingScreen(navController = navController, viewModel = viewModel)
            }
            composable(
                route = Screen.ReceivingInspection.route,
                arguments = listOf(navArgument("isReadOnly") { type = NavType.BoolType })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Receiving.route) }
                val viewModel: ReceivingViewModel = viewModel(parentEntry, factory = ReceivingViewModelFactory())
                val isReadOnly = backStackEntry.arguments?.getBoolean("isReadOnly") ?: false
                ReceivingInspectionScreen(navController = navController, viewModel = viewModel, isReadOnly = isReadOnly)
            }
        }

        navigation(startDestination = Screen.ReleasingHome.route, route = Screen.Releasing.route) {
            composable(Screen.ReleasingHome.route) {
                val viewModel: ReleasingViewModel = viewModel(factory = ReleasingViewModelFactory())
                ReleasingScreen(navController = navController, viewModel = viewModel)
            }
            composable(
                route = Screen.ReleasingPicking.route,
                arguments = listOf(
                    navArgument("noteId") { type = NavType.LongType },
                    navArgument("isReadOnly") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
                val isReadOnly = backStackEntry.arguments?.getBoolean("isReadOnly") ?: false
                val viewModel: ReleasingViewModel = viewModel(factory = ReleasingViewModelFactory())
                ReleasingPickingScreen(navController = navController, noteId = noteId, viewModel = viewModel, isReadOnly = isReadOnly)
            }
        }

        navigation(startDestination = Screen.InventoryHome.route, route = Screen.Inventory.route) {
            composable(
                route = Screen.InventoryHome.route,
                arguments = listOf(navArgument("filter") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Inventory.route) }
                val viewModel: InventoryViewModel = viewModel(parentEntry)
                InventoryScreen(
                    navController = navController,
                    filter = backStackEntry.arguments?.getString("filter"),
                    viewModel = viewModel
                )
            }
            composable(Screen.InventoryRequestForm.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Inventory.route) }
                val viewModel: InventoryViewModel = viewModel(parentEntry)
                InventoryRequestFormScreen(navController = navController, viewModel = viewModel)
            }
        }
        composable(Screen.More.route) { MoreScreen(navController = navController) }

        composable(
            route = Screen.BarcodeScan.route,
            arguments = listOf(
                navArgument("flowType") { type = NavType.StringType },
                navArgument("noteId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("lineId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("currentQty") { type = NavType.IntType; defaultValue = 0 },
                navArgument("orderedQty") { type = NavType.IntType; defaultValue = 0 },
                navArgument("lineRemark") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            BarcodeScanScreen(
                navController = navController,
                flowType = backStackEntry.arguments?.getString("flowType") ?: "receiving",
                noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L,
                lineId = backStackEntry.arguments?.getLong("lineId") ?: -1L,
                currentQty = backStackEntry.arguments?.getInt("currentQty") ?: 0,
                orderedQty = backStackEntry.arguments?.getInt("orderedQty") ?: 0,
                lineRemark = backStackEntry.arguments?.getString("lineRemark")
            )
        }
        composable(
            route = Screen.ManualInput.route,
            arguments = listOf(
                navArgument("flowType") { type = NavType.StringType },
                navArgument("noteId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("lineId") { type = NavType.LongType },
                navArgument("currentQty") { type = NavType.IntType },
                navArgument("orderedQty") { type = NavType.IntType },
                navArgument("lineRemark") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            ManualInputScreen(
                navController = navController,
                flowType = backStackEntry.arguments?.getString("flowType") ?: "receiving",
                noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L,
                lineId = backStackEntry.arguments?.getLong("lineId") ?: -1L,
                currentQty = backStackEntry.arguments?.getInt("currentQty") ?: 0,
                orderedQty = backStackEntry.arguments?.getInt("orderedQty") ?: 0,
                lineRemark = backStackEntry.arguments?.getString("lineRemark")
            )
        }

        composable(
            route = Screen.SearchResult.route,
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
