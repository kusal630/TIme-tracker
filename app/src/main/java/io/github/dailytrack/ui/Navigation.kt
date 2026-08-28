package io.github.dailytrack.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import io.github.dailytrack.ui.dashboard.DashboardScreen
import io.github.dailytrack.ui.timer.TimerScreen
import io.github.dailytrack.ui.todo.TodoScreen
import io.github.dailytrack.ui.pomodoro.PomodoroScreen
import io.github.dailytrack.ui.insights.InsightsScreen
import io.github.dailytrack.ui.growth.GrowthScreen
import io.github.dailytrack.ui.settings.SettingsScreen
import io.github.dailytrack.ui.savedquotes.SavedQuotesScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    data object Timer : Screen("timer", "Timer", Icons.Default.Timer)
    data object Todo : Screen("todo", "Todo", Icons.Default.CheckCircle)
    data object Pomodoro : Screen("pomodoro", "Pomodoro", Icons.Default.Coffee)
    data object Growth : Screen("growth", "Growth", Icons.Default.TrendingUp)
    data object Insights : Screen("insights", "Insights", Icons.Default.Lightbulb)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object SavedQuotes : Screen("saved_quotes", "Saved Quotes", Icons.Default.Favorite)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Timer,
    Screen.Todo,
    Screen.Pomodoro,
    Screen.Insights,
)

@Composable
fun DailyTrackNavHost() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    BackHandler(enabled = currentRoute != Screen.Dashboard.route) {
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(navController) }
            composable(Screen.Timer.route) { TimerScreen(navController) }
            composable(Screen.Todo.route) { TodoScreen(navController) }
            composable(Screen.Pomodoro.route) { PomodoroScreen(navController) }
            composable(Screen.Growth.route) { GrowthScreen(navController) }
            composable(Screen.Insights.route) { InsightsScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen(navController) }
            composable(Screen.SavedQuotes.route) { SavedQuotesScreen(navController) }
        }
    }
}
