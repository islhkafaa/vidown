package app.vidown.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

    SharedTransitionLayout {
        Scaffold(
            bottomBar = {
                if (!isPlayerActive) {
                    Surface(
                        modifier = Modifier
                            .padding(start = 80.dp, end = 80.dp, bottom = 26.dp)
                            .fillMaxWidth(),
                        shape = CircleShape,
                        tonalElevation = 0.dp,
                        shadowElevation = 20.dp,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp, Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.02f)
                                )
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            horizontalArrangement = Arrangement.spacedBy(
                                28.dp,
                                Alignment.CenterHorizontally
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items.forEach { screen ->
                                val isSelected = currentRoute?.startsWith(screen.route) == true
                                Box(
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        modifier = Modifier.size(42.dp),
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        border = if (isSelected) BorderStroke(
                                            1.dp,
                                            Color.White.copy(alpha = 0.1f)
                                        ) else null
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.6f
                                                )
                                            )
                                        }
                                    }
                                }
                            }
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
                    HomeScreen(
                        initialSearchUrl = url,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable
                    )
                }
                composable(Screen.History.route) {
                    HistoryScreen(
                        onPlayEvent = { encodedUri ->
                            navController.navigate("player/$encodedUri")
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable
                    )
                }
                composable(Screen.Settings.route) { SettingsScreen() }
                composable(
                    route = PLAYER_ROUTE,
                    arguments = listOf(navArgument("encodedUri") { type = NavType.StringType }),
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = androidx.compose.animation.core.tween(400)
                        ) +
                                fadeIn(
                                    animationSpec = androidx.compose.animation.core.tween(400)
                                )
                    },
                    exitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = androidx.compose.animation.core.tween(400)
                        ) +
                                fadeOut(
                                    animationSpec = androidx.compose.animation.core.tween(400)
                                )
                    },
                    popEnterTransition = {
                        fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(400)
                        )
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = androidx.compose.animation.core.tween(400)
                        ) +
                                fadeOut(
                                    animationSpec = androidx.compose.animation.core.tween(400)
                                )
                    }
                ) { backStackEntry ->
                    val encodedUri = backStackEntry.arguments?.getString("encodedUri") ?: ""
                    PlayerScreen(
                        encodedUri = encodedUri,
                        onBack = { navController.popBackStack() },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable
                    )
                }
            }
        }
    }
}
