package io.github.dailytrack.ui

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
import io.github.dailytrack.ui.food.FoodLogScreen
import io.github.dailytrack.ui.nutrition.NutritionScreen
import io.github.dailytrack.ui.sleep.SleepScreen
import io.github.dailytrack.ui.exercise.ExerciseScreen
import io.github.dailytrack.ui.body.BodySystemsScreen
import io.github.dailytrack.ui.growth.GrowthScreen
import io.github.dailytrack.ui.history.HistoryScreen
import io.github.dailytrack.ui.analytics.AnalyticsScreen
import io.github.dailytrack.ui.insights.InsightsScreen
import io.github.dailytrack.ui.journal.JournalScreen
import io.github.dailytrack.ui.settings.SettingsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Today", Icons.Default.Home)
    data object Timer : Screen("timer", "Timer", Icons.Default.Timer)
    data object Food : Screen("food", "Food", Icons.Default.Restaurant)
    data object Nutrition : Screen("nutrition", "Nutrition", Icons.Default.Analytics)
    data object Sleep : Screen("sleep", "Sleep", Icons.Default.Bedtime)
    data object Exercise : Screen("exercise", "Exercise", Icons.Default.FitnessCenter)
    data object Body : Screen("body", "Body", Icons.Default.MonitorHeart)
    data object Growth : Screen("growth", "Growth", Icons.Default.TrendingUp)
    data object History : Screen("history", "History", Icons.Default.CalendarMonth)
    data object Analytics : Screen("analytics", "Analytics", Icons.Default.BarChart)
    data object Insights : Screen("insights", "Insights", Icons.Default.Lightbulb)
    data object Journal : Screen("journal", "Journal", Icons.Default.MenuBook)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Timer,
    Screen.Food,
    Screen.Growth,
    Screen.Insights,
)

@Composable
fun DailyTrackNavHost() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

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
            composable(Screen.Food.route) { FoodLogScreen(navController) }
            composable(Screen.Nutrition.route) { NutritionScreen(navController) }
            composable(Screen.Sleep.route) { SleepScreen(navController) }
            composable(Screen.Exercise.route) { ExerciseScreen(navController) }
            composable(Screen.Body.route) { BodySystemsScreen(navController) }
            composable(Screen.Growth.route) { GrowthScreen(navController) }
            composable(Screen.History.route) { HistoryScreen(navController) }
            composable(Screen.Analytics.route) { AnalyticsScreen(navController) }
            composable(Screen.Insights.route) { InsightsScreen(navController) }
            composable(Screen.Journal.route) { JournalScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen(navController) }
        }
    }
}
