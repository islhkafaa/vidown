package app.vidown.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.vidown.ui.screen.HomeScreen
import app.vidown.ui.screen.HistoryScreen
import app.vidown.ui.screen.PlayerScreen
import app.vidown.ui.screen.QueueScreen
import app.vidown.ui.screen.SettingsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Home : Screen("home", "Home", Icons.Rounded.Home)
    data object Queue : Screen("queue", "Downloads", Icons.AutoMirrored.Rounded.List)
    data object History : Screen("history", "History", Icons.Rounded.History)
    data object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)
    data object Player : Screen("player/{encodedUri}", "Player", Icons.Rounded.Home)
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Queue, Screen.History, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Player.route) {
                NavigationBar {
                    items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute?.startsWith("player") == true) {
                                navController.popBackStack("player/{encodedUri}", inclusive = true)
                            }
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = if (currentRoute == Screen.Player.route) Modifier else Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Queue.route) { QueueScreen() }
            composable(Screen.History.route) {
                HistoryScreen(
                    onPlayEvent = { encodedUri ->
                        navController.navigate("player/$encodedUri")
                    }
                )
            }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(
                route = Screen.Player.route,
                arguments = listOf(navArgument("encodedUri") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedUri = backStackEntry.arguments?.getString("encodedUri") ?: ""
                PlayerScreen(encodedUri = encodedUri)
            }
        }
    }
}
