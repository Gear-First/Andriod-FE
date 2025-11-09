package com.ljs.and.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
        fun createRoute(filter: String?): String {
            return "inventory_home?filter=$filter"
        }
    }
    object More : Screen("more")
    object InventoryRequestForm : Screen("inventory_request?partId={partId}&partName={partName}&partCode={partCode}&price={price}&safetyStockQty={safetyStockQty}") {
        fun createRoute(partId: Long, partName: String?, partCode: String?, price: Int, safetyStockQty: Int): String {
            val name = partName ?: ""
            val code = partCode ?: ""
            return "inventory_request?partId=$partId&partName=$name&partCode=$code&price=$price&safetyStockQty=$safetyStockQty"
        }
    }
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

@RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun NavigationGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController = navController) }

        navigation(startDestination = Screen.ReceivingHome.route, route = Screen.Receiving.route) { // ... (code unchanged) 
        }

        navigation(startDestination = Screen.ReleasingHome.route, route = Screen.Releasing.route) { // ... (code unchanged) 
        }

        navigation(startDestination = Screen.InventoryHome.route, route = Screen.Inventory.route) {
            composable(
                route = Screen.InventoryHome.route,
                arguments = listOf(navArgument("filter") { 
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Inventory.route) }
                val viewModel: InventoryViewModel = viewModel(parentEntry)
                val filter = backStackEntry.arguments?.getString("filter")
                InventoryScreen(navController = navController, viewModel = viewModel, filter = filter)
            }
            composable(
                route = Screen.InventoryRequestForm.route,
                arguments = listOf(
                    navArgument("partId") { type = NavType.LongType },
                    navArgument("partName") { type = NavType.StringType; nullable = true },
                    navArgument("partCode") { type = NavType.StringType; nullable = true },
                    navArgument("price") { type = NavType.IntType },
                    navArgument("safetyStockQty") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Inventory.route) }
                val viewModel: InventoryViewModel = viewModel(parentEntry)
                InventoryRequestFormScreen(
                    navController = navController,
                    viewModel = viewModel,
                    partId = backStackEntry.arguments?.getLong("partId") ?: 0L,
                    partName = backStackEntry.arguments?.getString("partName"),
                    partCode = backStackEntry.arguments?.getString("partCode"),
                    price = backStackEntry.arguments?.getInt("price") ?: 0,
                    safetyStockQty = backStackEntry.arguments?.getInt("safetyStockQty") ?: 0
                )
            }
        }
        composable(Screen.More.route) { MoreScreen(navController = navController) }
        
        // ... (other composables are unchanged)
    }
}
