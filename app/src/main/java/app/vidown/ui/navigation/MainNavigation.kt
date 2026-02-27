package app.vidown.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.vidown.ui.screen.HistoryScreen
import app.vidown.ui.screen.HomeScreen
import app.vidown.ui.screen.PlayerScreen
import app.vidown.ui.screen.SettingsScreen

sealed class Screen(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
  data object Home : Screen("home", Icons.Rounded.Home)
  data object History : Screen("history", Icons.Rounded.History)
  data object Settings : Screen("settings", Icons.Rounded.Settings)
}

private const val PLAYER_ROUTE = "player/{encodedUri}"

@Composable
fun MainNavigation(initialUrl: String? = null) {
  val navController = rememberNavController()
  val items = listOf(Screen.Home, Screen.History, Screen.Settings)
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val isPlayerActive = currentRoute?.startsWith("player") == true

  Scaffold(
          bottomBar = {
            if (!isPlayerActive) {
              NavigationBar {
                items.forEach { screen ->
                  NavigationBarItem(
                          icon = { Icon(screen.icon, contentDescription = null) },
                          label = null,
                          selected = currentRoute == screen.route,
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
          }
  ) { innerPadding ->
    NavHost(
            navController = navController,
            startDestination =
                    if (initialUrl != null) "${Screen.Home.route}?url=$initialUrl"
                    else Screen.Home.route,
            modifier = if (isPlayerActive) Modifier else Modifier.padding(innerPadding)
    ) {
      composable(
              route = "${Screen.Home.route}?url={url}",
              arguments =
                      listOf(
                              navArgument("url") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                              }
                      )
      ) { backStackEntry ->
        val url = backStackEntry.arguments?.getString("url")
        HomeScreen(initialSearchUrl = url)
      }
      composable(Screen.History.route) {
        HistoryScreen(onPlayEvent = { encodedUri -> navController.navigate("player/$encodedUri") })
      }
      composable(Screen.Settings.route) { SettingsScreen() }
      composable(
              route = PLAYER_ROUTE,
              arguments = listOf(navArgument("encodedUri") { type = NavType.StringType }),
              enterTransition = {
                androidx.compose.animation.slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = androidx.compose.animation.core.tween(400)
                ) +
                        androidx.compose.animation.fadeIn(
                                animationSpec = androidx.compose.animation.core.tween(400)
                        )
              },
              exitTransition = {
                androidx.compose.animation.slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = androidx.compose.animation.core.tween(400)
                ) +
                        androidx.compose.animation.fadeOut(
                                animationSpec = androidx.compose.animation.core.tween(400)
                        )
              },
              popEnterTransition = {
                androidx.compose.animation.fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(400)
                )
              },
              popExitTransition = {
                androidx.compose.animation.slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = androidx.compose.animation.core.tween(400)
                ) +
                        androidx.compose.animation.fadeOut(
                                animationSpec = androidx.compose.animation.core.tween(400)
                        )
              }
      ) { backStackEntry ->
        val encodedUri = backStackEntry.arguments?.getString("encodedUri") ?: ""
        PlayerScreen(encodedUri = encodedUri, onBack = { navController.popBackStack() })
      }
    }
  }
}
